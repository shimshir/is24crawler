name := "is24crawler"

version := "1.0"

scalaVersion := "2.12.2"

enablePlugins(JavaAppPackaging)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.0.9",
  "com.typesafe.akka" %% "akka-http-spray-json" % "10.0.9",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.19",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "net.ruippeixotog" %% "scala-scraper" % "2.0.0",

  "org.scalatest" %% "scalatest" % "3.2.0-SNAP9" % "test",
  "org.mockito" % "mockito-core" % "2.8.47" % "test"
)
