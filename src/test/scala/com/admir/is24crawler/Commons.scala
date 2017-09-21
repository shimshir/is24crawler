package com.admir.is24crawler

import akka.actor.ActorSystem
import akka.stream.{ActorMaterializer, Materializer}
import com.typesafe.config.{Config, ConfigFactory}

import scala.concurrent.ExecutionContext
import scalacache.ScalaCache
import scalacache.guava.GuavaCache
import scalacache.serialization.InMemoryRepr

object Commons {

  object ImplicitContext {
    implicit val actorSystem: ActorSystem = ActorSystem("test-actorSystem")
    implicit val mat: Materializer = ActorMaterializer()
    implicit val ec: ExecutionContext = actorSystem.dispatcher
    implicit val scalaCache: ScalaCache[InMemoryRepr] = ScalaCache(GuavaCache())
  }

  val config: Config = ConfigFactory.load()
}
