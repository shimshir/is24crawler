package com.admir.is24crawler.web.routes

import akka.event.slf4j.SLF4JLogging
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route


class UiRoute extends SLF4JLogging {

  private val uiFolder = "is24crawler-ui/build"

  def route: Route =
    getFromResourceDirectory(uiFolder) ~ getFromResource(s"$uiFolder/index.html")
}
