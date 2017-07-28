package com.admir.is24crawler.services

import com.typesafe.config.Config
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.browser.JsoupBrowser.JsoupDocument
import org.scalatest._
import org.scalatest.mockito.MockitoSugar
import org.mockito.Mockito.when
import org.mockito.ArgumentMatchers.anyString

import scala.concurrent.duration._
import scala.concurrent.Await
import scala.concurrent.ExecutionContext.Implicits.global

class IsServiceSpec extends FlatSpec with Matchers with MockitoSugar {
  "getExposeIds" should "extract expose Ids from a result page" in {
    val resultPageHtml =
      """
        | <div>
        |   <article class="result-list-entry" data-obid="1">
        |   </article>
        |   <article class="result-list-entry" data-obid="2">
        |   </article>
        |   <article class="result-list-entry" data-obid="3">
        |   </article>
        | </div>
      """.stripMargin
    val mockJsoupBrowser = mock[JsoupBrowser]
    when(mockJsoupBrowser.get(anyString())) thenReturn JsoupBrowser().parseString(resultPageHtml).asInstanceOf[JsoupDocument]
    val mockConfig = mock[Config]
    val isService = new IsService(mockJsoupBrowser, mockConfig)
    val exposeIds = Await.result(isService.getExposeIds(""), 1.second)
    exposeIds should contain only("1", "2", "3")
  }
}