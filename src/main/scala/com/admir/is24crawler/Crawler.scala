package com.admir.is24crawler

import akka.event.slf4j.SLF4JLogging
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.admir.is24crawler.models.{Expose, Search}
import com.admir.is24crawler.services.IsService

import scala.concurrent.Future

class Crawler(isService: IsService)(implicit materializer: Materializer) extends SLF4JLogging {

  def search(search: Search): Future[Seq[Expose]] = {
    Source.fromFuture(isService.getResultPagePaths(search)).mapConcat(identity).map { rpPath =>
      log.debug(rpPath)
      rpPath
    }
      .mapAsync(2)(isService.getExposeIds).mapConcat(identity)
      .mapAsync(16)(isService.createExpose).filter(_.price.value <= search.maxTotalPrice)
      .fold[List[Expose]](Nil)(_ :+ _)
      .mapConcat(_.sortBy(_.price.value))
      .runWith(Sink.seq)
  }
}
