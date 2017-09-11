package com.admir.is24crawler.web

import akka.actor.ActorSystem
import akka.event.slf4j.SLF4JLogging
import akka.http.scaladsl.server.{Directive0, Directives, Route}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.HttpMethods._
import akka.http.scaladsl.model.{HttpResponse, StatusCodes}
import akka.http.scaladsl.model.headers.{`Access-Control-Allow-Credentials`, `Access-Control-Allow-Headers`, `Access-Control-Allow-Methods`, `Access-Control-Allow-Origin`}
import akka.stream.Materializer
import com.typesafe.config.Config

import scala.concurrent.ExecutionContext
import scala.util.{Failure, Success}

trait CorsSupport extends Directives {

  private def withAccessControlHeaders: Directive0 = {
    respondWithHeaders(
      `Access-Control-Allow-Origin`.*,
      `Access-Control-Allow-Credentials`(true),
      `Access-Control-Allow-Headers`("Authorization", "Content-Type", "X-Requested-With")
    )
  }

  private def preflightRequestHandler: Route =
    options {
      complete(HttpResponse(StatusCodes.OK).withHeaders(`Access-Control-Allow-Methods`(OPTIONS, POST, PUT, GET, DELETE)))
    }

  def corsHandler(route: Route): Route =
    withAccessControlHeaders {
      preflightRequestHandler ~ route
    }
}

class HttpServer(config: Config)(implicit actorSystem: ActorSystem, materializer: Materializer, ec: ExecutionContext) extends CorsSupport with SLF4JLogging {
  private val serverConfig = config.getConfig("application.server")
  private val host = serverConfig.getString("host")
  private val port = serverConfig.getInt("port")

  def start(combinedRoute: Route): Unit = {
    log.info(s"Starting server on port: $port")
    Http()
      .bindAndHandle(corsHandler(combinedRoute), host, port)
      .onComplete {
        case Success(binding) =>
          log.info(s"Started server on: ${binding.localAddress.toString}")
        case Failure(sst) =>
          log.error("Failed to start server", sst)
          actorSystem
            .terminate()
            .onComplete {
              case Success(_) =>
                log.info("Terminated actorSystem, exiting system with code 1")
                System.exit(1)
              case Failure(att) =>
                log.error("Failed to terminate actorSystem, exiting system with code 2", att)
                System.exit(2)
            }
      }
  }
}
