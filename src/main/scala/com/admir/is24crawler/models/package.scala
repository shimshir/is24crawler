package com.admir.is24crawler

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.JsonParser.ParsingException
import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsValue, RootJsonFormat}

package object models {

  abstract class LocationSearch(val `type`: String)
  case class ByPlaceSearch(geoNodes: Seq[Long]) extends LocationSearch("byPlace")
  case class ByDistanceSearch(geoNode: Long, geoDataAndAddress: Option[GeoDataAndAddress], radius: Int) extends LocationSearch("byDistance")

  abstract class Is24LocationSearch(val locationSelectionType: String)
  case class Is24ByPlaceSearch(geoNodes: Seq[Long]) extends Is24LocationSearch("GEO_HIERARCHY")
  case class Is24ByDistanceSearch(geoNode: Long, geoDataAndAddress: GeoDataAndAddress, radius: Int) extends Is24LocationSearch("VICINITY")

  case class MinMaxFilter(min: Option[Double], max: Option[Double])

  case class CrawlerSearch(
                            priceFilter: Option[MinMaxFilter],
                            roomAmountFilter: Option[MinMaxFilter],
                            surfaceFilter: Option[MinMaxFilter],
                            locationSearch: Option[LocationSearch]
                          )

  case class Expose(
                     price: Price,
                     surface: Float,
                     roomAmount: Float,
                     pageLink: String,
                     imageLinks: Seq[String],
                     address: Address
                   )

  case class Price(value: Double, string: String)

  case class Address(region: String, street: String)

  case class ResultPagePath(first: String) {
    def withPageNum(num: Int): String = {
      first.splitAt(10) match {
        case (prefix, suffix) => s"$prefix/P-$num$suffix"
      }
    }
  }

  case class GeoData(x: Int, y: Int)
  case class Is24Address(city: Option[String], region: Option[String] = None, quarter: Option[String] = None)
  case class GeoDataAndAddress(geoData: GeoData, address: Is24Address)
  case class GeoLocationEntity(id: String, label: String, `type`: String, geoDataAndAddress: Option[GeoDataAndAddress] = None)
  case class GeoLocationResult(entity: GeoLocationEntity)

  object JsonProtocols extends SprayJsonSupport with DefaultJsonProtocol {

    implicit lazy val locationSearchFormat: RootJsonFormat[LocationSearch] = new RootJsonFormat[LocationSearch] {
      private val byPlaceFormat = jsonFormat1(ByPlaceSearch)
      private val byDistanceFormat = jsonFormat3(ByDistanceSearch)

      def write(obj: LocationSearch): JsValue = {
        val baseJson = obj match {
          case byPlace: ByPlaceSearch => byPlaceFormat.write(byPlace).asJsObject
          case byDistance: ByDistanceSearch => byDistanceFormat.write(byDistance).asJsObject
          case _ => throw new ParsingException(s"Could not write object to json\n$obj")
        }
        JsObject(baseJson.fields + ("type" -> JsString(obj.`type`)))
      }

      def read(json: JsValue): LocationSearch = {
        val jsonObject = json.asJsObject
        jsonObject.fields("type").convertTo[String] match {
          case "byPlace" => byPlaceFormat.read(json)
          case "byDistance" => byDistanceFormat.read(json)
          case typ => throw new ParsingException(s"Could not read LocationSearch for type: '$typ', supported types are: ['byPlace']")
        }
      }
    }


    implicit lazy val minMaxFilterFormat: RootJsonFormat[MinMaxFilter] = jsonFormat2(MinMaxFilter)
    implicit lazy val crawlerSearchFormat: RootJsonFormat[CrawlerSearch] = jsonFormat4(CrawlerSearch)

    implicit lazy val priceFormat: RootJsonFormat[Price] = jsonFormat2(Price)
    implicit lazy val addressFormat: RootJsonFormat[Address] = jsonFormat2(Address)
    implicit lazy val exposeFormat: RootJsonFormat[Expose] = jsonFormat6(Expose)

    implicit lazy val is24AddressFormat: RootJsonFormat[Is24Address] = jsonFormat3(Is24Address)
    implicit lazy val geoDataFormat: RootJsonFormat[GeoData] = jsonFormat2(GeoData)
    implicit lazy val geoDataAndAddressFormat: RootJsonFormat[GeoDataAndAddress] = jsonFormat2(GeoDataAndAddress)
    implicit lazy val geoLocationEntityFormat: RootJsonFormat[GeoLocationEntity] = jsonFormat4(GeoLocationEntity)
    implicit lazy val geoLocationResult: RootJsonFormat[GeoLocationResult] = jsonFormat1(GeoLocationResult)
  }

}
