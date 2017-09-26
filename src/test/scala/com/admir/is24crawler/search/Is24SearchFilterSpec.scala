package com.admir.is24crawler.search

import org.scalatest._
import spray.json._

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.admir.is24crawler.models._

class Is24SearchFilterSpec extends FlatSpec with Matchers with SprayJsonSupport with DefaultJsonProtocol {

  it should "be possible to search byDistance" in {
    val geoData = GeoData(229459, 2511140)
    val is24Address = Is24Address(city = Some("Berlin"), region = Some("Berlin"))

    val locSearch = Is24ByDistanceSearch(
      geoNode = 1276003001,
      geoDataAndAddress = GeoDataAndAddress(geoData, is24Address),
      radius = 5
    )

    val filter = Is24SearchFilter().withLocationSearch(locSearch)
    val filterJsonFields = filter.body.fields

    filterJsonFields("locationSelectionType") shouldBe JsString("VICINITY")
    filterJsonFields("geoInfoNodes") shouldBe JsArray(JsNumber(locSearch.geoNode))
    filterJsonFields("centerOfSearchAddress") shouldBe JsObject("city" -> JsString(is24Address.city.get), "region" -> JsString(is24Address.region.get))
    filterJsonFields("radius") shouldBe JsNumber(locSearch.radius)
    filterJsonFields("centerX") shouldBe JsNumber(geoData.x)
    filterJsonFields("centerY") shouldBe JsNumber(geoData.y)
  }
}
