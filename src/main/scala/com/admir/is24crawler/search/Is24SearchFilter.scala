package com.admir.is24crawler.search

import com.admir.is24crawler.models._
import com.admir.is24crawler.models.JsonProtocols._
import spray.json._

case class Is24SearchFilter(body: JsObject) {
  def withLocationSearch(locationSearch: Is24LocationSearch): Is24SearchFilter = locationSearch match {
    case Is24ByPlaceSearch(geoNodes) =>
      withLocationSelectionType("GEO_HIERARCHY")
        .withGeoInfoNodes(geoNodes)
    case Is24ByDistanceSearch(geoNode, radius, geoData, address) =>
      withLocationSelectionType("VICINITY")
        .withGeoInfoNodes(Seq(geoNode))
        .withCenterOfSearchAddress(address)
        .withCenterX(geoData.x)
        .withCenterY(geoData.y)
        .withRadius(radius)
  }

  def withCenterOfSearchAddress(address: Is24Address): Is24SearchFilter = {
    Is24SearchFilter(JsObject(body.fields + ("centerOfSearchAddress" -> address.toJson)))
  }

  def withCenterX(centerX: Int): Is24SearchFilter = {
    Is24SearchFilter(JsObject(body.fields + ("centerX" -> JsNumber(centerX))))
  }

  def withCenterY(centerY: Int): Is24SearchFilter = {
    Is24SearchFilter(JsObject(body.fields + ("centerY" -> JsNumber(centerY))))
  }

  def withRadius(radius: Int): Is24SearchFilter = {
    Is24SearchFilter(JsObject(body.fields + ("radius" -> JsNumber(radius))))
  }

  def withLocationSelectionType(selectionType: String): Is24SearchFilter = {
    Is24SearchFilter(JsObject(body.fields + ("locationSelectionType" -> JsString(selectionType))))
  }

  private def withMinMaxFilter(minMaxFilter: MinMaxFilter, fieldName: String): Is24SearchFilter = {
    val minJs = minMaxFilter.min.map(JsNumber(_)).getOrElse(JsNull)
    val maxJs = minMaxFilter.max.map(JsNumber(_)).getOrElse(JsNull)
    Is24SearchFilter(JsObject(body.fields + (fieldName -> JsObject("min" -> minJs, "max" -> maxJs))))
  }

  def withNetRentRange: MinMaxFilter => Is24SearchFilter = withMinMaxFilter(_, "netRentRange")

  def withNetAreaRange: MinMaxFilter => Is24SearchFilter = withMinMaxFilter(_, "netAreaRange")

  def withNumberOfRoomsRange: MinMaxFilter => Is24SearchFilter = withMinMaxFilter(_, "numberOfRoomsRange")

  def withGeoInfoNodes(geoInfoNodes: Seq[Long]): Is24SearchFilter = {
    Is24SearchFilter(JsObject(body.fields + ("geoInfoNodes" -> JsArray(geoInfoNodes.map(JsNumber(_)): _*))))
  }

  def withWbsNeeded(needsWbs: Boolean): Is24SearchFilter = {
    Is24SearchFilter(JsObject(body.fields + ("wohnberechtigungsscheinNeeded" -> JsBoolean(needsWbs))))
  }

  def withOnlyWithKitchen(hasKitchen: Boolean): Is24SearchFilter = {
    Is24SearchFilter(JsObject(body.fields + ("onlyWithKitchen" -> JsBoolean(hasKitchen))))
  }
}

object Is24SearchFilter {
  private val firstPageReqBodyJson = io.Source.fromResource("firstPageReqBody.json").mkString.parseJson.asJsObject
  private val geoLocationsCsvBerlin = io.Source.fromResource("geoLocations-Berlin.csv").getLines
  lazy val geoLocationsBerlin: Map[Long, String] = geoLocationsCsvBerlin.map(_.split(',')).map(splitLine => (splitLine.head.toLong, splitLine.last)).toMap

  def allLocations: Map[Long, String] = {
    geoLocationsBerlin
  }

  def apply(): Is24SearchFilter = {
    Is24SearchFilter(firstPageReqBodyJson)
  }
}


