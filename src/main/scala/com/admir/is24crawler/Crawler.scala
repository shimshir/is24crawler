package com.admir.is24crawler

import java.text.{DecimalFormat, DecimalFormatSymbols}

import akka.event.slf4j.SLF4JLogging
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.admir.is24crawler.models.Expose
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Try

class Crawler(implicit materializer: Materializer, ec: ExecutionContext) extends SLF4JLogging {
  val df = new DecimalFormat()
  val sfs = new DecimalFormatSymbols
  sfs.setGroupingSeparator('.')
  sfs.setDecimalSeparator(',')
  df.setDecimalFormatSymbols(sfs)

  val browser = JsoupBrowser()

  val host = "https://www.immobilienscout24.de"

  def search(minRooms: String, minSquares: Int, maxRent: Int): Future[Seq[Expose]] = {
    val firstPagePath = s"/Suche/S-T/Wohnung-Miete/Berlin/Berlin/-/$minRooms-/$minSquares,00-/EURO--$maxRent,00/-/-/false/true"

    Source.single(browser.get(host + firstPagePath))
      .map(doc => Try((doc >> element("#pageSelection > select") >> elementList("option")) >> attr("value")).toOption)
      .collect {
        case Some(values) =>
          log.info(s"Result has following pages:\n${values.reduceLeft((acc, link) => s"$acc\n$link")}")
          values
      }
      .mapConcat(identity)
      .map(pagePath => {
        val url = host + pagePath
        log.info(s"Requesting result page: $url")
        browser.get(url)
      })
      .mapConcat(_ >> elementList("article.result-list-entry") >> attr("data-obid"))
      .mapAsyncUnordered(16)(objId => {
        val url = s"$host/expose/$objId"
        log.info(s"Requesting detail page: $url")
        Future((browser.get(url), objId))
      })
      .map { case (doc, objId) => (doc >> text("dd.is24qa-gesamtmiete"), s"$host/expose/$objId") }
      .map {
        case (priceStr, link) =>
          val euroIdx = priceStr.indexOf("€")
          val priceNumStr = priceStr.substring(0, euroIdx).trim
          Expose(df.parse(priceNumStr).doubleValue(), priceStr, link)
      }
      .filter(_.price <= maxRent)
      .fold[List[Expose]](Nil)((acc, elem) => acc :+ elem)
      .mapConcat(_.sortBy(_.price))
      .runWith(Sink.seq)
  }
}
