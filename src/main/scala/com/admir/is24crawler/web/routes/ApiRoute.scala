package com.admir.is24crawler.web.routes

import akka.event.slf4j.SLF4JLogging
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.admir.is24crawler.Crawler
import com.admir.is24crawler.models.JsonProtocols._
import com.admir.is24crawler.models.CrawlerSearch
import com.admir.is24crawler.search.Is24SearchFilter
import spray.json._
import com.admir.is24crawler.web.mapToKeyValueJsArray

class ApiRoute(crawler: Crawler) extends SLF4JLogging {
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
      } ~ path("locations") {
        get {
          parameter('region.?) {
            case Some(region) => region.toLowerCase match {
              case "berlin" =>
                complete(mapToKeyValueJsArray(Is24SearchFilter.geoLocationsBerlin))
              case _ =>
                complete(StatusCodes.NotFound)
            }
            case None =>
              complete(mapToKeyValueJsArray(Is24SearchFilter.allLocations))
          }
        }
      }
    }
}
