package ru.otus.sparkml

import org.apache.spark.ml.PipelineModel
import org.apache.spark.sql.{SaveMode, SparkSession}
import org.apache.spark.sql.functions.{col, concat_ws, udf}

object IrisSparkStreaming {
  def main(args: Array[String]): Unit = {

    val IRIS_LABELS =
      Map(0.0 -> "setosa", 1.0 -> "versicolor", 2.0 -> "virginica")

    val PREDICTION_VALUE_UDF =
      udf((col: Double) => IRIS_LABELS(col))

    val spark = SparkSession.builder
      .appName("SparkML")
      .config("spark.master", "local")
      .config("spark.sql.debug.maxToStringFields", 100)
      .getOrCreate()

    import spark.implicits._

    try {
      val model = PipelineModel.load("irisPipelineModel/irisRandomForestClassificationModel2/")

      val data = spark.read
        .option("header", "true")
        .option("inferSchema", "true")
        .csv("data/Iris.csv")

      val prediction = model.transform(data)

      prediction
        .withColumn(
          "predictedLabel",
          PREDICTION_VALUE_UDF(col("prediction"))
        )
        .select(
          $"predictedLabel".as("key"),
          concat_ws(",", $"SepalLengthCm", $"SepalWidthCm", $"PetalLengthCm", $"PetalWidthCm", $"predictedLabel").as("value")
        )
        .repartition(1)
        .write
        .mode(SaveMode.Overwrite)
        .csv("result")

    } catch {
      case e: Exception =>
        println(s"ERROR: ${e.getMessage}")
        sys.exit(-1)
    } finally {
      spark.stop()
    }
  }
}
