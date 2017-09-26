package com.admir.is24crawler.services

import com.admir.is24crawler.Commons
import com.admir.is24crawler.models._
import com.admir.is24crawler.search.Is24SearchFilter
import com.admir.is24crawler.web.HttpClient
import com.typesafe.config.Config
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupDocument
import org.scalatest._
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.anyString
import com.admir.is24crawler.Commons.ImplicitContext._

import scala.concurrent.duration._
import scala.concurrent.Await
import scalacache._
import guava._

class Is24ServiceSpec extends FlatSpec with Matchers with MockitoSugar {

  implicit val scalaCache = ScalaCache(GuavaCache())

  "getExposeIds" should "extract expose Ids from a result page" in {
    val resultPageHtml =
      """
        | <div>
        |   <article class="result-list-entry" data-obid="1">
        |   </article>
        |   <article class="result-list-entry" data-obid="2">
        |   </article>
        |   <article class="result-list-entry" data-obid="3">
        |   </article>
        | </div>
      """.stripMargin
    val mockHttpClient = mock[HttpClient]
    val mockGeoLocationService = mock[GeoLocationService]
    val mockJsoupBrowser = mock[JsoupBrowser]
    when(mockJsoupBrowser.get(anyString())) thenReturn JsoupBrowser().parseString(resultPageHtml).asInstanceOf[JsoupDocument]
    val mockConfig = mock[Config]

    val isService = new Is24Service(mockHttpClient, mockGeoLocationService, mockJsoupBrowser, mockConfig)
    val exposeIds = Await.result(isService.getExposeIds(""), 1.second)
    exposeIds should contain only("1", "2", "3")
  }

  "fetchResultPagePath byDistance" should "return a url for a byDistanceSearch" in {

    val httpClient = new HttpClient()
    val mockGeoLocationService = mock[GeoLocationService]
    val isService = new Is24Service(httpClient, mockGeoLocationService, mock[JsoupBrowser], Commons.config)

    val geoData = GeoData(229459, 2511140)
    val is24Address = Is24Address(city = Some("Berlin"), region = Some("Berlin"))

    val locSearch = Is24ByDistanceSearch(
      geoNode = 1276003001,
      geoData = geoData,
      address = is24Address,
      radius = 5
    )

    val filter = Is24SearchFilter().withLocationSearch(locSearch)

    val resultEither = Await.result(isService.fetchResultPagePath(filter), 5.seconds)

    resultEither.foreach(println)
  }
}
