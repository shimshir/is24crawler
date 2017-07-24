package com.admir.is24crawler

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols

import akka.actor.ActorSystem
import akka.event.slf4j.SLF4JLogging
import akka.stream.{ActorMaterializer, ActorMaterializerSettings, Supervision}
import akka.stream.scaladsl.{Sink, Source}
import net.ruippeixotog.scalascraper.browser.JsoupBrowser
import net.ruippeixotog.scalascraper.dsl.DSL._
import net.ruippeixotog.scalascraper.dsl.DSL.Extract._

import scala.concurrent.Future
import scala.util.Try
import scala.io._


object Main extends App with SLF4JLogging {
  implicit val actorSystem = ActorSystem("is24crawler")
  val decider: Supervision.Decider = { e =>
    log.error("Unhandled exception in stream", e)
    Supervision.Stop
  }
  implicit val materializer = ActorMaterializer(ActorMaterializerSettings(actorSystem).withSupervisionStrategy(decider))
  implicit val ec = actorSystem.dispatcher

  val df = new DecimalFormat()
  val sfs = new DecimalFormatSymbols
  sfs.setGroupingSeparator('.')
  sfs.setDecimalSeparator(',')
  df.setDecimalFormatSymbols(sfs)

  println("Min. square meters:")
  val minQuadrature = StdIn.readLine().toInt
  println("Min. rooms:")
  val minRooms = StdIn.readLine().replace('.', ',')
  println("Max. rent (€):")
  val maxRent = StdIn.readLine().toInt
  println("Searching...")

  val browser = JsoupBrowser()

  val host = "https://www.immobilienscout24.de"
  val firstPagePath = s"/Suche/S-T/Wohnung-Miete/Berlin/Berlin/-/$minRooms-/$minQuadrature,00-/EURO--$maxRent,00/-/-/false/true"

  val graph: Future[Seq[(Double, String, String)]] = Source.single(browser.get(host + firstPagePath))
    .map(doc => Try((doc >> element("#pageSelection > select") >> elementList("option")) >> attr("value")).toOption)
    .collect { case Some(values) => values }.mapConcat(identity)
    .mapAsync(10)(pagePath => Future(browser.get(host + pagePath)))
    .mapConcat(_ >> elementList("article.result-list-entry") >> attr("data-obid"))
    .mapAsync(100)(objId => Future((browser.get(s"$host/expose/$objId"), objId)))
    .map { case (doc, objId) => (doc >> text("dd.is24qa-gesamtmiete"), s"$host/expose/$objId") }
    .map {
      case (priceStr, link) =>
        val euroIdx = priceStr.indexOf("€")
        val priceNumStr = priceStr.substring(0, euroIdx).trim
        (df.parse(priceNumStr).doubleValue(), priceStr, link)
    }
    .filter { case (price, _, _) => price <= maxRent }
    .fold[Seq[(Double, String, String)]](Nil)((acc, elem) => acc :+ elem)
    .mapConcat(_.sortBy(_._1).toList)
    .runWith(Sink.seq)

  graph.foreach { elems =>
    elems.foreach(elem => println(elem._2, elem._3))
    actorSystem.terminate()
  }
}
