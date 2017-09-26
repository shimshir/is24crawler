package com.admir.is24crawler.services

import akka.event.slf4j.SLF4JLogging
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpRequest, Uri}
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.admir.is24crawler.models.{GeoDataAndAddress, GeoLocationEntity, GeoLocationResult}
import com.admir.is24crawler.models.JsonProtocols._
import com.admir.is24crawler.web.HttpClient
import com.typesafe.config.Config

import scala.concurrent.{ExecutionContext, Future}
import scalacache.{NoSerialization, ScalaCache}
import scalacache._
import memoization._

class GeoLocationService(httpClient: HttpClient, config: Config)(implicit ec: ExecutionContext, mat: Materializer, scalaCache: ScalaCache[NoSerialization]) extends SLF4JLogging {

  val geoAutocompleteEndpoint: String = config.getString("is24.geo-autocomplete-endpoint")
  private val geoDataEndpointTemplate = config.getString("is24.geo-data-endpoint-template")

  def geoDataEndpoint(locationId: String): String = String.format(geoDataEndpointTemplate, locationId)

  def search(locationQuery: String): Future[Seq[GeoLocationEntity]] = {
    val fullUri = Uri(geoAutocompleteEndpoint).withQuery(Query("i" -> locationQuery))
    val req = HttpRequest(uri = fullUri)
    httpClient.executeAndConvert[Seq[GeoLocationResult]](req).map {
      case Left(t) =>
        log.error("Could not fetch locations", t)
        Nil
      case Right(locations) =>
        locations.map(_.entity)
    }
  }

  def searchWithGeoData(locationQuery: String): Future[Seq[GeoLocationEntity]] = {
    Source.fromFuture(search(locationQuery))
      .mapConcat(_.toVector)
      .mapAsync(4) { entity =>
        fetchGeoDataAndAddress(entity.id).map {
          case Left(t) =>
            log.error("Could not fetch geoData", t)
            Left(t)
          case Right(gdaa) =>
            Right(entity.copy(geoDataAndAddress = Some(gdaa)))
        }
      }
      .collect {
        case Right(entityWithGeoData) =>
          entityWithGeoData
      }
      .runWith(Sink.seq)
  }

  def fetchGeoDataAndAddress(geoNodeId: String): Future[Throwable Either GeoDataAndAddress] = memoize(None) {
    val endpoint = geoDataEndpoint(geoNodeId)
    val req = HttpRequest(uri = endpoint)
    httpClient.executeAndConvert[GeoDataAndAddress](req)
  }
}
