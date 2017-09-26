package com.admir.is24crawler.services

import akka.event.slf4j.SLF4JLogging
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpRequest, StatusCodes, Uri}
import akka.http.scaladsl.unmarshalling.Unmarshal
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.admir.is24crawler.models.{GeoLocationEntity, GeoLocationResult}
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

  def searchGeoNodes(locationQuery: String): Future[Seq[Long]] = {
    val fullUri = Uri(geoAutocompleteEndpoint).withQuery(Query("i" -> locationQuery))
    val req = HttpRequest(uri = fullUri)
    httpClient.executeAndConvert[Seq[GeoLocationResult]](req).map {
      case Left(t) =>
        log.error("Could not fetch locations", t)
        Nil
      case Right(locations) =>
        locations.map(_.entity.id.toLong)
    }
  }

  def searchGeoLocationEntity(locationQuery: String): Future[Seq[GeoLocationEntity]] = {
    Source.fromFuture(searchGeoNodes(locationQuery))
      .mapConcat(_.toVector)
      .mapAsync(4) { geoNode =>
        fetchGeoLocationEntity(geoNode).map {
          case Left(t) =>
            log.error("Could not fetch geoData", t)
            Left(t)
          case Right(geoLocationEntityOpt) =>
            Right(geoLocationEntityOpt)
        }
      }
      .collect {
        case Right(Some(entityWithGeoData)) =>
          entityWithGeoData
      }
      .runWith(Sink.seq)
  }

  def fetchGeoLocationEntity(geoNodeId: Long): Future[Throwable Either Option[GeoLocationEntity]] = memoize(None) {
    val endpoint = geoDataEndpoint(geoNodeId.toString)
    val req = HttpRequest(uri = endpoint)
    httpClient.execute(req).flatMap {
      case res if res.status == StatusCodes.NotFound =>
        Future.successful(Right(None))
      case res =>
        Unmarshal(res).to[GeoLocationEntity].map(entity => Right(Some(entity))).recover { case t => Left(t) }
    }
  }
}
