package com.admir.is24crawler.web.routes

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

class IndexRoute {
  def route: Route =
    get {
      pathSingleSlash {
        complete(StatusCodes.OK, "Alive")
      }
    }
}
