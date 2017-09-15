package com.admir.is24crawler.search

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{ContentTypes, HttpEntity, HttpMethods, HttpRequest}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.ActorMaterializer
import org.scalatest._
import spray.json._

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.concurrent.ExecutionContext.Implicits.global
import akka.http.scaladsl.marshallers.sprayjson.SprayJsonSupport
import com.admir.is24crawler.models.ResultPageLocation

class IsSearchFilterSpec extends FlatSpec with Matchers with SprayJsonSupport with DefaultJsonProtocol {
  private val firstPageReqBodyJson = io.Source.fromResource("firstPageReqBody.json").mkString.parseJson.asJsObject
  implicit val as = ActorSystem()
  implicit val mat = ActorMaterializer()

  it should "be possible to create a search filter from json" in {
    val filter = IsSearchFilter(firstPageReqBodyJson)
      .withNetAreaRange(45, 65)
      .withNumberOfRoomsRange(1.5, 5)
      .withNetRentRange(0, 600)
      .withGeoInfoNodes(1276003001014L, 1276003001031L, 1276003001020L)
      .withWbsNeeded(false)

    val filterEntity = HttpEntity(ContentTypes.`application/json`, filter.body.compactPrint)
    // TODO: Read the url from config
    val req = HttpRequest(HttpMethods.POST, "https://www.immobilienscout24.de/Suche/controller/search/change.go?sortingCode=0&otpEnabled=true", entity = filterEntity)

    val firstPageLocationFut =
      Http()
        .singleRequest(req)
        .flatMap { res =>
          Unmarshal(res).to[JsObject].map(
            _.fields("url")
              .convertTo[String]
              .replace("?enteredFrom=result_list", "")
          )
        }

    val rpl = ResultPageLocation(Await.result(firstPageLocationFut, 5.seconds))
    rpl.first shouldEqual "/Suche/S-T/Wohnung-Miete/Berlin/Berlin/Friedenau-Schoeneberg_Grunewald-Wilmersdorf_Kaulsdorf-Hellersdorf/1,50-5,00/45,00-65,00/EURO--600,00/-/-/false"
    rpl.withPageNum(42) shouldEqual "/Suche/S-T/P-42/Wohnung-Miete/Berlin/Berlin/Friedenau-Schoeneberg_Grunewald-Wilmersdorf_Kaulsdorf-Hellersdorf/1,50-5,00/45,00-65,00/EURO--600,00/-/-/false"
  }
}
