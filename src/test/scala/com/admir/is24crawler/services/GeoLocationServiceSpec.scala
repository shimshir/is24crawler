package com.admir.is24crawler.services

import akka.http.scaladsl.marshalling.Marshal
import akka.http.scaladsl.model.{HttpRequest, HttpResponse}
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

import scala.util.Try

class GeoLocationServiceSpec extends FlatSpec with Matchers with MockitoSugar {

  import Commons.ImplicitContext._

  val httpClient = new HttpClient()

  val testGeoData = GeoData(x = 42, y = 24)
  val testIs24Address = Is24Address(city = Some("Berlin"), region = Some("Berlin"))

  val testGeoLocationEntity =
    GeoLocationEntity(
      id = "1276003001",
      label = "Berlin",
      `type` = "city",
      geoData = testGeoData,
      address = testIs24Address
    )

  val testLocationResultEntity = GeoLocationResultEntity(testGeoLocationEntity.id)
  val testLocationResults = Seq(GeoLocationResult(testLocationResultEntity))

  "searchGeoNodes(locationQuery)" should "retrieve geo node ids from is24" in {

    val mockClient = mock[HttpClient]
    val reqCaptor: ArgumentCaptor[HttpRequest] = ArgumentCaptor.forClass(classOf[HttpRequest])
    when(mockClient.executeAndConvert[Seq[GeoLocationResult]](reqCaptor.capture())(any())).thenReturn(Future.successful(Right(testLocationResults)))

    val geoLocationService = new GeoLocationService(mockClient, Commons.config)

    val geoNodeIdsFut = geoLocationService.searchGeoNodes(testGeoLocationEntity.label)
    val geoNodeIds = Await.result(geoNodeIdsFut, 5.seconds)
    geoNodeIds.head shouldEqual testLocationResultEntity.id.toLong
    reqCaptor.getValue.uri.query().get("i") shouldEqual Some(testGeoLocationEntity.label)
  }

  "searchGeoLocationEntity(locationQuery)" should "retrieve geo locations entities from is24" in {

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
    when(mockClient.execute(argThat(geoDataArgumentMatcher))).thenReturn(Marshal(testGeoLocationEntity).to[HttpResponse])

    val geoLocationService = new GeoLocationService(mockClient, Commons.config)

    val withGdsFut = geoLocationService.searchGeoLocationEntity(testGeoLocationEntity.label)
    val withGds = Await.result(withGdsFut, 5.seconds)
    withGds.head shouldBe testGeoLocationEntity
  }
}
