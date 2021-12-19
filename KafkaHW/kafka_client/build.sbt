name := "kafka_client"

version := "0.1"

scalaVersion := "2.13.4"

libraryDependencies ++= Seq(
  "org.apache.kafka" % "kafka-clients" % "2.6.0",
  "com.fasterxml.jackson.core" % "jackson-databind" % "2.4.0",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "org.apache.commons" % "commons-csv" % "1.9.0",
  /*"com.github.agourlay" % "json-2-csv_2.13" % "0.5.7",*/
  "org.scala-lang.modules" %% "scala-parser-combinators" % "1.1.2"
)
