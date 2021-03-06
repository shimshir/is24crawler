package com.admir.is24crawler

import akka.event.slf4j.SLF4JLogging
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.admir.is24crawler.models.{Expose, CrawlerSearch}
import com.admir.is24crawler.services.Is24Service

import scala.concurrent.Future

class Crawler(isService: Is24Service)(implicit materializer: Materializer) extends SLF4JLogging {

  def search(search: CrawlerSearch): Future[Seq[Expose]] = {
    Source.fromFuture(isService.getResultPagePaths(search)).mapConcat(identity)
      .mapAsync(4)(isService.getExposeIds).mapConcat(identity)
      .mapAsync(16)(isService.createExpose).filter(expose => search.priceFilter.flatMap(_.max).forall(maxPrice => expose.price.value <= maxPrice))
      .fold[List[Expose]](Nil)(_ :+ _)
      .mapConcat(_.sortBy(_.price.value))
      .runWith(Sink.seq)
  }
}
