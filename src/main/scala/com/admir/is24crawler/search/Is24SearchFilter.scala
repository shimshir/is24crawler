package com.admir.is24crawler.search

import com.admir.is24crawler.models.{ByPlaceSearch, LocationSearch}
import spray.json._

case class Is24SearchFilter(body: JsObject) {
  def withLocationSearch(locationSearch: Option[LocationSearch]): Is24SearchFilter = locationSearch match {
    case Some(ByPlaceSearch(geoNodes)) => withGeoInfoNodes(geoNodes)
    case None => this
    case _ => ???
  }

  def withNetAreaRange(min: Double, max: Double): Is24SearchFilter = {
    Is24SearchFilter(JsObject(body.fields + ("netAreaRange" -> JsObject("min" -> JsNumber(min), "max" -> JsNumber(max)))))
  }

  def withNumberOfRoomsRange(min: Double, max: Double): Is24SearchFilter = {
    Is24SearchFilter(JsObject(body.fields + ("numberOfRoomsRange" -> JsObject("min" -> JsNumber(min), "max" -> JsNumber(max)))))
  }

  def withNetRentRange(min: Double, max: Double): Is24SearchFilter = {
    Is24SearchFilter(JsObject(body.fields + ("netRentRange" -> JsObject("min" -> JsNumber(min), "max" -> JsNumber(max)))))
  }

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


