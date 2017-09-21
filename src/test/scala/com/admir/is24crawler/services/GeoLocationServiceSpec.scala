package com.admir.is24crawler.services

import akka.http.scaladsl.model.HttpRequest
import com.admir.is24crawler.Commons
import com.admir.is24crawler.models._
import com.admir.is24crawler.models.JsonProtocols._
import com.admir.is24crawler.web.HttpClient
import org.mockito.{ArgumentCaptor, ArgumentMatcher}
import org.scalatest._
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers._

import scala.concurrent.{Await, Future}
import scala.concurrent.duration._
import spray.json._

import scala.util.Try

class GeoLocationServiceSpec extends FlatSpec with Matchers with MockitoSugar {

  import Commons.ImplicitContext._

  val httpClient = new HttpClient()

  val testGeoLocationEntity =
    GeoLocationEntity(
      id = "1276003001",
      label = "Berlin",
      `type` = "city"
    )

  val testGeoLocationResult = GeoLocationResult(testGeoLocationEntity)

  val testGeoData = GeoData(x = 42, y = 24)
  val testIs24Address = Is24Address(city = Some("Berlin"), region = Some("Berlin"))

  val testGeoDataAndAddress = GeoDataAndAddress(geoData = testGeoData, address = testIs24Address)

  val testLocationEntities = Seq(testGeoLocationEntity)
  val testLocationResults = Seq(testGeoLocationResult)

  "search(locationQuery)" should "retrieve geo locations from is24 without the geoData" in {

    val mockClient = mock[HttpClient]
    val reqCaptor: ArgumentCaptor[HttpRequest] = ArgumentCaptor.forClass(classOf[HttpRequest])
    when(mockClient.executeAndConvert[Seq[GeoLocationResult]](reqCaptor.capture())(any())).thenReturn(Future.successful(Right(testLocationResults)))

    val geoLocationService = new GeoLocationService(mockClient, Commons.config)

    val geoLocationEntitiesFut = geoLocationService.search(testGeoLocationEntity.label)
    val geoLocationEntities = Await.result(geoLocationEntitiesFut, 5.seconds)
    geoLocationEntities.head shouldEqual testLocationResults.head.entity
    reqCaptor.getValue.uri.query().get("i") shouldEqual Some(testGeoLocationEntity.label)
  }

  "searchWithGeoData(locationQuery)" should "retrieve geo locations from is24 with the geoData" in {

    val locationsArgumentMatcher: ArgumentMatcher[HttpRequest] = req => {
      val matches = Try(req.uri.path.toString == "/geoautocomplete/v3/locations.json").getOrElse(false)
      if (matches) {
        req.uri.query().get("i") shouldEqual Some(testGeoLocationEntity.label)
      }
      matches
    }

    val geoDataArgumentMatcher: ArgumentMatcher[HttpRequest] = req => {
      val matches = Try(req.uri.path.toString.contains("/geoautocomplete/v3/entities/")).getOrElse(false)
      if (matches) {
        req.uri.path.reverse.head.toString shouldBe testGeoLocationEntity.id
      }
      matches
    }

    val mockClient = mock[HttpClient]
    when(mockClient.executeAndConvert[Seq[GeoLocationResult]](argThat(locationsArgumentMatcher))(any())).thenReturn(Future.successful(Right(testLocationResults)))
    when(mockClient.executeAndConvert[GeoDataAndAddress](argThat(geoDataArgumentMatcher))(any())).thenReturn(Future.successful(Right(testGeoDataAndAddress)))

    val geoLocationService = new GeoLocationService(mockClient, Commons.config)

    val withGdsFut = geoLocationService.searchWithGeoData(testGeoLocationEntity.label)
    val withGds = Await.result(withGdsFut, 5.seconds)
    withGds.head shouldBe testGeoLocationEntity.copy(geoDataAndAddress = Some(testGeoDataAndAddress))
  }
}
