package com.admir.is24crawler.web

import akka.actor.ActorSystem
import akka.event.slf4j.SLF4JLogging
import akka.http.scaladsl.server.Route
import akka.http.scaladsl.Http
import akka.http.scaladsl.Http.ServerBinding
import akka.stream.Materializer
import com.typesafe.config.Config

import scala.concurrent.{ExecutionContext, Future}

class HttpServer(config: Config)(implicit actorSystem: ActorSystem, materializer: Materializer, ec: ExecutionContext) extends SLF4JLogging {
  private val serverConfig = config.getConfig("application.server")
  private val host = serverConfig.getString("host")
  private val port = serverConfig.getInt("port")

  def start(combinedRoute: Route): Future[ServerBinding] = {
    Http().bindAndHandle(combinedRoute, host, port).map { binding =>
      log.info(s"Started server on: ${binding.localAddress.toString}")
      binding
    }
  }
}
