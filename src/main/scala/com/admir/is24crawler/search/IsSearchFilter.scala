package com.admir.is24crawler.search

import spray.json.{JsArray, JsBoolean, JsNumber, JsObject}

case class IsSearchFilter(body: JsObject) {
  def withNetAreaRange(min: Double, max: Double): IsSearchFilter = {
    IsSearchFilter(JsObject(body.fields + ("netAreaRange" -> JsObject("min" -> JsNumber(min), "max" -> JsNumber(max)))))
  }

  def withNumberOfRoomsRange(min: Double, max: Double): IsSearchFilter = {
    IsSearchFilter(JsObject(body.fields + ("numberOfRoomsRange" -> JsObject("min" -> JsNumber(min), "max" -> JsNumber(max)))))
  }

  def withNetRentRange(min: Double, max: Double): IsSearchFilter = {
    IsSearchFilter(JsObject(body.fields + ("netRentRange" -> JsObject("min" -> JsNumber(min), "max" -> JsNumber(max)))))
  }

  def withGeoInfoNodes(geoInfoNodes: Long*): IsSearchFilter = {
    IsSearchFilter(JsObject(body.fields + ("geoInfoNodes" -> JsArray(geoInfoNodes.map(JsNumber(_)): _*))))
  }

  def withWbsNeeded(needed: Boolean): IsSearchFilter = {
    IsSearchFilter(JsObject(body.fields + ("wohnberechtigungsscheinNeeded" -> JsBoolean(needed))))
  }
}


