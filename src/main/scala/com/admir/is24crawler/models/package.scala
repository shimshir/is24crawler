package com.admir.is24crawler

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.JsonParser.ParsingException
import spray.json.{DefaultJsonProtocol, JsObject, JsString, JsValue, RootJsonFormat}

package object models {

  abstract class LocationSearch(val `type`: String)

  case class ByPlaceSearch(geoNodes: List[Long]) extends LocationSearch("byPlace")

  case class CrawlerSearch(
                            maxTotalPrice: Double,
                            minRooms: Double,
                            minSquare: Double,
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

  object JsonProtocols extends SprayJsonSupport with DefaultJsonProtocol {

    implicit val locationSearchFormat: RootJsonFormat[LocationSearch] = new RootJsonFormat[LocationSearch] {
      private val byPlaceFormat = jsonFormat1(ByPlaceSearch)

      def write(obj: LocationSearch): JsValue = {
        val baseJson = obj match {
          case byPlace: ByPlaceSearch => byPlaceFormat.write(byPlace).asJsObject
          case _ => throw new ParsingException(s"Could not write object to json\n$obj")
        }
        JsObject(baseJson.fields + ("type" -> JsString(obj.`type`)))
      }

      def read(json: JsValue): LocationSearch = {
        val jsonObject = json.asJsObject
        jsonObject.fields("type").convertTo[String] match {
          case "byPlace" => byPlaceFormat.read(json)
          case typ => throw new ParsingException(s"Could not read LocationSearch for type: '$typ', supported types are: ['byPlace']")
        }
      }
    }
    implicit val crawlerSearchFormat: RootJsonFormat[CrawlerSearch] = jsonFormat4(CrawlerSearch)

    implicit val priceFormat: RootJsonFormat[Price] = jsonFormat2(Price)
    implicit val addressFormat: RootJsonFormat[Address] = jsonFormat2(Address)
    implicit val exposeFormat: RootJsonFormat[Expose] = jsonFormat6(Expose)
  }

}
