package com.admir.is24crawler.services

import akka.actor.ActorSystem
import akka.event.slf4j.SLF4JLogging
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest}
import com.admir.is24crawler.models._
import com.admir.is24crawler.search.Is24SearchFilter
import com.admir.is24crawler.web.HttpClient
import com.typesafe.config.Config
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.model.Document
import spray.json.{DefaultJsonProtocol, JsObject}

import scala.concurrent.duration._
import scala.concurrent.{Await, ExecutionContext, Future}
import scala.util.{Failure, Success, Try}
import scala.language.postfixOps
import scalacache._
import memoization._

class Is24Service(httpClient: HttpClient, geoLocationService: GeoLocationService, browser: JsoupBrowser, config: Config)
                 (implicit actorSystem: ActorSystem, ec: ExecutionContext, scalaCache: ScalaCache[NoSerialization]) extends SLF4JLogging with DefaultJsonProtocol with SprayJsonSupport {

  val is24Host: String = config.getString("is24.host")
  val is24SearchApi: String = config.getString("is24.search-endpoint")

  def fetchResultPagePath(filter: Is24SearchFilter): Future[Throwable Either ResultPagePath] = {
    val filterEntity = HttpEntity(ContentTypes.`application/json`, filter.body.compactPrint)
    val req = HttpRequest(HttpMethods.POST, is24SearchApi, entity = filterEntity)
    httpClient.executeAndConvert[JsObject](req).map { resObjectEither =>
      resObjectEither.right.flatMap(resObject =>
        Try(resObject.fields("url").convertTo[String].replace("?enteredFrom=result_list", "")).toEither
      ).map(url => ResultPagePath(url))
    }
  }

  def createIs24SearchFilter(search: CrawlerSearch): Is24SearchFilter = {
    val is24Filter = Is24SearchFilter()
    val is24PriceFilter = search.priceFilter.map(is24Filter.withNetRentRange).getOrElse(is24Filter)
    val is24RoomAmountFilter = search.roomAmountFilter.map(is24PriceFilter.withNumberOfRoomsRange).getOrElse(is24PriceFilter)
    val is24AreaFilter = search.surfaceFilter.map(is24RoomAmountFilter.withNetAreaRange).getOrElse(is24RoomAmountFilter)

    val is24LocationSearchOpt = search.locationSearch.flatMap {
      case ByPlaceSearch(geoNodes) =>
        Some(Is24ByPlaceSearch(geoNodes))
      case ByDistanceSearch(geoNode, radius) =>
        val geoDataAndAddressFut = geoLocationService.fetchGeoLocationEntity(geoNode)
        val is24ByDistanceSearchFut = geoDataAndAddressFut.map {
          case Right(geoLocationEntity) =>
            Some(Is24ByDistanceSearch(geoNode, radius, geoLocationEntity.geoData, geoLocationEntity.address))
          case Left(t) =>
            log.error(s"Could not get geoDataAndAddress for geoNode: $geoNode", t)
            throw t
        }

        Await.result(is24ByDistanceSearchFut.recover[Option[Is24LocationSearch]] { case _ => None }, 3.seconds)
    }
    val finalFilter = is24LocationSearchOpt.map(is24AreaFilter.withLocationSearch).getOrElse(is24AreaFilter)
    finalFilter
  }

  def getResultPagePaths(search: CrawlerSearch): Future[List[String]] = memoize(1 hour) {
    val filter = createIs24SearchFilter(search)

    log.debug(s"Searching is24 with filter:\n${filter.body.prettyPrint}")

    fetchResultPagePath(filter).map {
      case Right(resultPagePath) =>
        val firstResultPageDoc = browser.get(is24Host + resultPagePath.first)
        val firstResultPageHasResults = (firstResultPageDoc >?> elementList("article.result-list-entry")).isDefined
        val selectElementOpt = firstResultPageDoc >?> element("#pageSelection > select")
        (firstResultPageHasResults, selectElementOpt) match {
          case (false, None) =>
            Nil
          case (true, None) =>
            List(resultPagePath.first)
          case (true, Some(selectElement)) =>
            val lastPageNumber = (selectElement >> elementList("option") >> attr("value")).last.toInt
            log.info(s"Found total of $lastPageNumber result pages")
            1 to lastPageNumber map resultPagePath.withPageNum toList
          case _ =>
            log.error("Unexpected result after requesting first page")
            Nil
        }
      case Left(t) =>
        log.error("Could not retrieve first page path", t)
        Nil
    }
  }

  def getExposeIds(resultPagePath: String): Future[List[String]] = memoize(1 hour) {
    log.debug(s"Fetching exposes from result page: $resultPagePath")
    Future(browser.get(is24Host + resultPagePath)).map { resultPageDoc =>
      Try(resultPageDoc >> elementList("article.result-list-entry") >> attr("data-obid")) match {
        case Failure(t) =>
          log.error(s"Could not find exposes on result page: $resultPagePath", t)
          Nil
        case Success(exposeIds) =>
          exposeIds
      }
    }
  }

  def createExpose(exposeId: String): Future[Expose] = memoize(1 day) {
    val pageLink = s"$is24Host/expose/$exposeId"
    log.debug(s"Fetching expose page for id: $exposeId")
    Future(browser.get(pageLink)).map { doc =>
      val price = extractPrice(doc)
      val surface = extractSurface(doc)
      val roomAmount = extractRoomAmount(doc)
      val imageLinks = extractImageLinks(doc)
      val address = extractAddress(doc)
      Expose(price, surface, roomAmount, pageLink, imageLinks, address)
    }
  }

  private def extractPrice(exposePageDoc: Document): Price = {
    val priceStr = exposePageDoc >> text("dd.is24qa-gesamtmiete")
    val priceNumStr = priceStr.substring(0, priceStr.indexOf("€")).trim.replace(".", "").replace(",", ".")

    Try(priceNumStr.toDouble) match {
      case Success(priceValue) =>
        Price(priceValue, priceStr)
      case Failure(t) =>
        log.error(s"Could not parse price for price: (str: $priceStr, strNum: $priceNumStr)", t)
        Price(-1.0, priceStr + " (invalid)")
    }
  }

  private def extractSurface(exposePageDoc: Document): Float = {
    val surfaceString = exposePageDoc >> text("div.is24qa-flaeche")
    surfaceString.filter(isValidNumberSymbol).replace(',', '.').toFloat
  }

  private def extractRoomAmount(exposePageDoc: Document): Float = {
    val roomAmountString = exposePageDoc >> text("div.is24qa-zi")
    roomAmountString.filter(isValidNumberSymbol).replace(',', '.').toFloat
  }

  private def isValidNumberSymbol(symbol: Char) = symbol.isDigit || symbol == ',' || symbol == '.' || symbol == '-'

  private def extractImageLinks(exposePageDoc: Document) = exposePageDoc >> elementList("img.sp-image") >> attr("data-src")

  private def extractAddress(exposePageDoc: Document) = {
    val addressElement = (exposePageDoc >> elementList("div.address-block")).head
    val innerAddressContainer = addressElement >> element("div:nth-child(2)")
    val spans = innerAddressContainer >> elementList("span")
    spans.partition(spanElem => (spanElem >/~ validator(attr("class"))(_ contains "zip-region-and-country")).isRight) match {
      case (regionElements, streetElements) =>
        val regionText = regionElements.head >> text
        val streetTextWithMap = streetElements.head >> text
        val lastCommaIndex = streetTextWithMap.lastIndexOf(",")
        val cutIndex = if (lastCommaIndex == -1) streetTextWithMap.length else lastCommaIndex
        val streetText = streetTextWithMap.substring(0, cutIndex)
        Address(regionText, streetText)
    }
  }
}
