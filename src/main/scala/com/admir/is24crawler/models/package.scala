package com.admir.is24crawler

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

package object models {

  case class CrawlerSearchFilter(
                                  maxTotalPrice: Double,
                                  minRooms: Double,
                                  minSquare: Double,
                                  locationNodes: Seq[Long]
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
    implicit val crawlerSearchFormat: RootJsonFormat[CrawlerSearchFilter] = jsonFormat4(CrawlerSearchFilter)

    implicit val priceFormat: RootJsonFormat[Price] = jsonFormat2(Price)
    implicit val addressFormat: RootJsonFormat[Address] = jsonFormat2(Address)
    implicit val exposeFormat: RootJsonFormat[Expose] = jsonFormat6(Expose)
  }

}
