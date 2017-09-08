package com.admir.is24crawler.web

import java.io.BufferedInputStream

import akka.event.slf4j.SLF4JLogging
import akka.http.scaladsl.model.{ContentType, ContentTypes, HttpEntity, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import com.admir.is24crawler.models.JsonProtocols._
import com.admir.is24crawler.Crawler
import com.admir.is24crawler.models.Search
import org.apache.tika.Tika

import scala.util.control.NonFatal
import spray.json._

class Routes(crawler: Crawler) extends SLF4JLogging {

  val tika = new Tika()

  def exposes: Route =
    pathPrefix("api") {
      path("exposes") {
        (post & entity(as[Search])) { search =>
          log.info(s"Received search input:\n${search.toJson.prettyPrint}")
          onSuccess(crawler.search(search)) { exposes =>
            log.info(s"Completing request with ${exposes.size} expose results")
            complete(exposes)
          }
        }
      }
    }

  def ui: Route =
    get {
      pathEndOrSingleSlash {
        completeWithUiResource("index.html", ContentTypes.`text/html(UTF-8)`)
      } ~ path(RemainingPath) { path =>
        val mime = tika.detect(path.toString)
        ContentType.parse(mime) match {
          case Left(errorInfos) =>
            log.error(s"Errors while parsing mime: $mime, errors: $errorInfos")
            complete(StatusCodes.InternalServerError)
          case Right(contentType) =>
            completeWithUiResource(path.toString, contentType)
        }
      }
    }

  def completeWithUiResource(path: String, contentType: ContentType): Route = {
    val innerPath = s"/is24crawler-ui/build/$path"
    readResource(innerPath) match {
      case Left(t) =>
        log.warn(s"Could not read resource: $innerPath, returning index.html")
        log.trace(s"Could not read resource: $innerPath", t)
        // TODO: Differentiate between /static and other paths
        completeWithUiResource("index.html", ContentTypes.`text/html(UTF-8)`)
      case Right(resource) =>
        complete(HttpEntity(contentType, resource))
    }
  }

  def readResource(classpath: String): Either[Throwable, Array[Byte]] = {
    val bis = new BufferedInputStream(getClass.getResourceAsStream(classpath))
    try Right(Stream.continually(bis.read).takeWhile(_ != -1).map(_.toByte).toArray)
    catch {
      case NonFatal(t) =>
        Left(t)
    }
    finally bis.close()
  }
}
