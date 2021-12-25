package ru.otus.sparkml

import org.apache.spark.sql.SparkSession

object IrisStreaming {

  def main(args: Array[String]): Unit = {

    val spark = SparkSession.builder
      .appName("IrisSparkStreaming")
      .config("spark.master", "local")
      .config("spark.sql.debug.maxToStringFields", 100)
      .getOrCreate()

    try {

      val irisStreaming = new Stream(spark, "irisPipelineModel/irisRandomForestClassificationModel2/")

      irisStreaming.streaming()

    } catch {
      case e: Exception =>
        println(s"ERROR: ${e.getMessage}")
        sys.exit(-1)
    } finally {
      spark.stop()
    }
  }
}
