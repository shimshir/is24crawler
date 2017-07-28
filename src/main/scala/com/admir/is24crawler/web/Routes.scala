package com.admir.is24crawler.web

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.admir.is24crawler.models.JsonProtocols._
import com.admir.is24crawler.Crawler


class Routes(crawler: Crawler) {
  def results: Route =
    get {
      path("api" / "exposes") {
        parameters(
          'minSquares.as[Int],
          'minRooms.as[Double],
          'maxRent.as[Int]
        ) { (sqMeters, rooms, rent) =>
          onSuccess(crawler.search(rooms.toString.replace('.', ','), sqMeters, rent)) { results =>
            complete(results)
          }
        }
      } ~ path("results") {
        complete(StatusCodes.Gone)
      }
    }
}
