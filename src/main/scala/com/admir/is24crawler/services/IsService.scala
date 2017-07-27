package com.admir.is24crawler.services

import akka.event.slf4j.SLF4JLogging
import com.admir.is24crawler.models.Expose
import com.typesafe.config.Config
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._

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
      val priceStr = doc >> text("dd.is24qa-gesamtmiete")
      val priceNumStr = priceStr.substring(0, priceStr.indexOf("€")).trim.replace(".", "").replace(",", ".")

      val imageLinks = doc >> elementList("img.sp-image") >> attr("src")

      Try(priceNumStr.toDouble) match {
        case Success(priceValue) =>
          Expose(priceValue, priceStr, pageLink, imageLinks)
        case Failure(t) =>
          log.error(s"Expose Id: $exposeId, priceStr: $priceStr, priceNumStr: $priceNumStr", t)
          Expose(-1.0, priceStr + " (invalid)", pageLink, imageLinks)
      }
    }
  }
}
