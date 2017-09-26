package com.admir.is24crawler

import akka.actor.ActorSystem
import akka.event.slf4j.SLF4JLogging
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.admir.is24crawler.services.{GeoLocationService, Is24Service}
import com.admir.is24crawler.web.HttpServer
import com.typesafe.config.{Config, ConfigFactory}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import akka.http.scaladsl.server.Directives._
import com.admir.is24crawler.web.routes.{ApiRoute, UiRoute}
import com.admir.is24crawler.web.HttpClient

import scalacache._
import guava._


object Main extends App with SLF4JLogging {
  implicit val actorSystem = ActorSystem("is24crawler")
  val decider: Supervision.Decider = { e =>
    log.error("Unhandled exception in stream", e)
    Supervision.Stop
  }
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(actorSystem).withSupervisionStrategy(decider))
  implicit val ec = actorSystem.dispatcher
  implicit val scalaCache = ScalaCache(GuavaCache())

  val config: Config = ConfigFactory.load(s"application.conf")

  val jsoupBrowser = JsoupBrowser.typed()

  val httpServer = new HttpServer(config)

  val httpClient = new HttpClient()
  val geoLocationService = new GeoLocationService(httpClient, config)
  val isService = new Is24Service(httpClient, geoLocationService, jsoupBrowser, config)
  val crawler = new Crawler(isService)
  val apiRoute = new ApiRoute(crawler, geoLocationService)
  val uiRoute = new UiRoute()
  httpServer.start(apiRoute.route ~ uiRoute.route)
}
