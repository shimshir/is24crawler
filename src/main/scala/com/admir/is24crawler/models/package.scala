package com.admir.is24crawler

import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import spray.json.{DefaultJsonProtocol, RootJsonFormat}

package object models {

  case class Search(
                     maxTotalPrice: Double,
                     minRooms: Double,
                     minSquare: Int
                   )

  case class Expose(
                     price: Price,
                     pageLink: String,
                     imageLinks: Seq[String],
                     address: Address
                   )

  case class Price(value: Double, string: String)

  case class Address(region: String, street: String)

  object JsonProtocols extends SprayJsonSupport with DefaultJsonProtocol {
    implicit val searchFormat: RootJsonFormat[Search] = jsonFormat3(Search)

    implicit val priceFormat: RootJsonFormat[Price] = jsonFormat2(Price)
    implicit val addressFormat: RootJsonFormat[Address] = jsonFormat2(Address)
    implicit val exposeFormat: RootJsonFormat[Expose] = jsonFormat4(Expose)
  }

}
