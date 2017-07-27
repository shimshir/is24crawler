package com.admir.is24crawler

import akka.event.slf4j.SLF4JLogging
import akka.stream.Materializer
import akka.stream.scaladsl.{Sink, Source}
import com.admir.is24crawler.models.Expose
import com.admir.is24crawler.services.IsService
import net.ruippeixotog.scalascraper.browser.JsoupBrowser

import scala.concurrent.Future

class Crawler(isService: IsService, browser: JsoupBrowser)(implicit materializer: Materializer) extends SLF4JLogging {

  def search(minRooms: String, minSquares: Int, maxRent: Int): Future[Seq[Expose]] = {
    Source.fromFuture(isService.getResultPagePaths(minRooms, minSquares, maxRent)).mapConcat(identity)
      .mapAsync(1)(isService.getExposeIds).mapConcat(identity)
      .mapAsync(16)(isService.createExpose).filter(_.priceValue <= maxRent)
      .fold[List[Expose]](Nil)(_ :+ _)
      .mapConcat(_.sortBy(_.priceValue))
      .runWith(Sink.seq)
  }
}
