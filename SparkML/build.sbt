name := "SparkML"

version := "1.0"

scalaVersion := "2.12.12"
lazy val sparkVersion = "3.1.2"

libraryDependencies ++= Seq(
  "org.apache.spark" %% "spark-sql"   % sparkVersion % "provided",
  "org.apache.spark" %% "spark-mllib" % sparkVersion % "provided",
  "com.typesafe" % "config" % "1.2.1",
  "org.apache.spark" % "spark-sql-kafka-0-10_2.12" % "3.1.1",
  "org.apache.kafka" % "kafka-clients" % "2.8.0",
 // "org.apache.spark" % "spark-streaming-kafka-0-10_2.11" % "2.1.0"
)
