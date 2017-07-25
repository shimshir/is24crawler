package com.admir.is24crawler.web

import akka.http.scaladsl.model.{ContentTypes, HttpEntity}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.admir.is24crawler.{Crawler, FreemarkerEngine}

import scala.collection.JavaConverters._

class Routes(crawler: Crawler, freemarkerEngine: FreemarkerEngine) {
  def results: Route =
    get {
      path("results") {
        parameters(
          'minSquares.as[Int],
          'minRooms.as[Double],
          'maxRent.as[Int]
        ) { (sqMeters, rooms, rent) =>
          onSuccess(crawler.search(rooms.toString.replace('.', ','), sqMeters, rent)) { results =>
            val out = freemarkerEngine.renderUri("results.ftl", Map("exposes" -> results.asJava))
            val entity = HttpEntity(ContentTypes.`text/html(UTF-8)`, out)
            complete(entity)
          }
        }
      }
    }
}
