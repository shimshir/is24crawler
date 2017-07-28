package com.admir.is24crawler

import akka.actor.ActorSystem
import akka.event.slf4j.SLF4JLogging
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.admir.is24crawler.services.IsService
import com.admir.is24crawler.web.{HttpServer, Routes}
import com.typesafe.config.{Config, ConfigFactory}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser


object Main extends App with SLF4JLogging {
  implicit val actorSystem = ActorSystem("is24crawler")
  val decider: Supervision.Decider = { e =>
    log.error("Unhandled exception in stream", e)
    Supervision.Stop
  }
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(actorSystem).withSupervisionStrategy(decider))
  implicit val ec = actorSystem.dispatcher
  val config: Config = ConfigFactory.load(s"application.conf")

  val jsoupBrowser = JsoupBrowser.typed()

  val httpServer = new HttpServer(config)
  val isService = new IsService(jsoupBrowser, config)
  val crawler = new Crawler(isService, jsoupBrowser)
  val routes = new Routes(crawler)
  httpServer.start(routes.results)
}
