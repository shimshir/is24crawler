name := "is24crawler"

version := "1.0"

scalaVersion := "2.12.2"

enablePlugins(JavaAppPackaging)

libraryDependencies ++= Seq(
  "com.typesafe.akka" %% "akka-http" % "10.0.9",
  "org.slf4j" % "slf4j-api" % "1.7.25",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "com.typesafe.akka" %% "akka-slf4j" % "2.4.19",
  "net.ruippeixotog" %% "scala-scraper" % "2.0.0",
  "org.freemarker" % "freemarker" % "2.3.23"
)
