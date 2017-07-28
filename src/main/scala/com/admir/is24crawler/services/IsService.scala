package com.admir.is24crawler.services

import akka.event.slf4j.SLF4JLogging
import com.admir.is24crawler.models.{Address, Expose, Price}
import com.typesafe.config.Config
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._
import net.ruippeixotog.scalascraper.model.Document

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Failure, Success, Try}

class IsService(browser: JsoupBrowser, config: Config)(implicit ec: ExecutionContext) extends SLF4JLogging {

  val host: String = config.getString("application.is.host")

  def getResultPagePaths(minRooms: String, minSquares: Int, maxRent: Int): Future[List[String]] = {

    val firstResultPagePath = s"/Suche/S-T/Wohnung-Miete/Berlin/Berlin/-/$minRooms-/$minSquares,00-/EURO--$maxRent,00/-/-/false/true"

    Future(browser.get(host + firstResultPagePath)).map { firstResultPageDoc =>
      val firstResultPageHasResults = (firstResultPageDoc >?> elementList("article.result-list-entry")).isDefined
      val selectElementOpt = firstResultPageDoc >?> element("#pageSelection > select")
      (firstResultPageHasResults, selectElementOpt) match {
        case (false, None) =>
          Nil
        case (true, None) =>
          List(firstResultPagePath)
        case (true, Some(selectElement)) =>
          selectElement >> elementList("option") >> attr("value")
        case _ =>
          log.error("Unexpected result after requesting first page")
          Nil
      }
    }
  }

  def getExposeIds(resultPagePath: String): Future[List[String]] =
    Future(browser.get(host + resultPagePath)).map { resultPageDoc =>
      Try(resultPageDoc >> elementList("article.result-list-entry") >> attr("data-obid")) match {
        case Failure(t) =>
          log.error(s"Could not find exposes on result page: $resultPagePath", t)
          Nil
        case Success(exposeIds) =>
          exposeIds
      }
    }

  def createExpose(exposeId: String): Future[Expose] = {

    val pageLink = s"$host/expose/$exposeId"

    Future(browser.get(pageLink)).map { doc =>
      val price = extractPrice(doc)
      val imageLinks = extractImageLinks(doc)
      val address = extractAddress(doc)
      Expose(price, pageLink, imageLinks, address)
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
