package com.admir.is24crawler

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes}
import akka.http.scaladsl.unmarshalling.{Unmarshal, Unmarshaller}
import akka.stream.Materializer
import spray.json.JsonWriter

import scala.concurrent.{ExecutionContext, Future}
import spray.json._

package object web {

  def mapToKeyValueJsArray[K, V](locationsMap: Map[K, V])(implicit keyWriter: JsonWriter[K], valueWriter: JsonWriter[V]): Iterable[Map[String, JsValue]] = {
    locationsMap.map{case (key, value) => Map("key" -> key.toJson, "value" -> value.toJson)}
  }

  type ReqToRes = HttpRequest => Future[HttpResponse]

  class HttpClient()(implicit actorSystem: ActorSystem, mat: Materializer, ec: ExecutionContext) {
    val akkaHttp = Http()

    def execute(req: HttpRequest): Future[HttpResponse] = {
      akkaHttp.singleRequest(req).recover { case _ => HttpResponse(StatusCodes.ServiceUnavailable) }
    }

    def executeAndConvert[T](req: HttpRequest)
                            (implicit unmarshaller: Unmarshaller[HttpResponse, T]): Future[Throwable Either T] = {
      execute(req).flatMap { res =>
        Unmarshal(res).to[T].map(Right(_)).recover { case t => Left(t) }
      }
    }
  }

}
