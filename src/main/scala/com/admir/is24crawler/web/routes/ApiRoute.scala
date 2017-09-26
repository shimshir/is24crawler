package com.admir.is24crawler.web.routes

import akka.event.slf4j.SLF4JLogging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.admir.is24crawler.Crawler
import com.admir.is24crawler.models.JsonProtocols._
import com.admir.is24crawler.models.CrawlerSearch
import com.admir.is24crawler.services.GeoLocationService
import spray.json._

class ApiRoute(crawler: Crawler, geoLocationService: GeoLocationService) extends SLF4JLogging {
  def route: Route =
    pathPrefix("api") {
      path("exposes") {
        (post & entity(as[CrawlerSearch])) { search =>
          log.info(s"Received search input:\n${search.toJson.prettyPrint}")
          onSuccess(crawler.search(search)) { exposes =>
            log.info(s"Completing request with ${exposes.size} expose results")
            complete(exposes)
          }
        }
      } ~ (path("locations") & get) {
        parameter('query) { locationQuery =>
          log.info(s"Looking for '$locationQuery' locations")
          onSuccess(geoLocationService.searchGeoLocationEntity(locationQuery)) { locationEntities =>
            log.debug(s"Completing request with ${locationEntities.size} location results")
            complete(locationEntities)
          }
        } ~ complete(StatusCodes.BadRequest, "'query' parameter is required")
      } ~ (path("locations" / LongNumber) & get) { geoNode =>
        log.info(s"Fetching location entity for geoNode: $geoNode")
        onSuccess(geoLocationService.fetchGeoLocationEntity(geoNode)) { locationEntity =>
          complete(locationEntity)
        }
      } ~ complete(StatusCodes.NotFound)
    }
}
