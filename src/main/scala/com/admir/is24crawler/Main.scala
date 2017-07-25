package com.admir.is24crawler

import akka.actor.ActorSystem
import akka.event.slf4j.SLF4JLogging
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import com.admir.is24crawler.web.{HttpServer, Routes}
import com.typesafe.config.{Config, ConfigFactory}


object Main extends App with SLF4JLogging {
  implicit val actorSystem = ActorSystem("is24crawler")
  val decider: Supervision.Decider = { e =>
    log.error("Unhandled exception in stream", e)
    Supervision.Stop
  }
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(actorSystem).withSupervisionStrategy(decider))
  implicit val ec = actorSystem.dispatcher
  val config: Config = ConfigFactory.load(s"application.conf")

  val httpServer = new HttpServer(config)
  val crawler = new Crawler()
  val freemarkerEngine = new FreemarkerEngine()
  val routes = new Routes(crawler, freemarkerEngine)
  httpServer.start(routes.results)
}
