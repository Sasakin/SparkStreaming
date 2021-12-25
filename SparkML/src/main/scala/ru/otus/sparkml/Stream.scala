package ru.otus.sparkml

import org.apache.spark.ml.PipelineModel
import org.apache.spark.sql.SparkSession
import org.apache.spark.sql.functions.{col, concat_ws, from_csv, udf}
import org.apache.spark.sql.types.{DoubleType, StringType, StructField, StructType}


class Stream (private val spark: SparkSession, private val modelPath: String) {
  private val IRIS_LABELS =
    Map(0.0 -> "setosa", 1.0 -> "versicolor", 2.0 -> "virginica")

  private val PREDICTION_VALUE_UDF =
    udf((col: Double) => IRIS_LABELS(col))

  val IRIS_SCHEMA = StructType(
    StructField("SepalLengthCm", DoubleType, nullable = true) ::
      StructField("SepalWidthCm", DoubleType, nullable = true) ::
      StructField("PetalLengthCm", DoubleType, nullable = true) ::
      StructField("PetalWidthCm", DoubleType, nullable = true) ::
      Nil
  )

  def streaming() = {

    val model = PipelineModel.load(modelPath)

    import spark.implicits._

    val input = spark
      .readStream
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("failOnDataLoss", "false")
      .option("subscribe", "input")
      .load()
      .select($"value".cast(StringType))
      .withColumn("struct", from_csv($"value", IRIS_SCHEMA, Map("sep" -> ",")))
      .withColumn("SepalLengthCm", $"struct".getField("SepalLengthCm"))
      .withColumn("SepalWidthCm", $"struct".getField("SepalWidthCm"))
      .withColumn("PetalLengthCm", $"struct".getField("PetalLengthCm"))
      .withColumn("PetalWidthCm", $"struct".getField("PetalWidthCm"))
      .drop("value", "struct")


    val prediction = model.transform(input)

    val query = prediction
      .withColumn(
        "predictedLabel",
        PREDICTION_VALUE_UDF(col("prediction"))
      )
      .select(
        $"predictedLabel".as("key"),
        concat_ws(",", $"SepalLengthCm", $"SepalWidthCm", $"PetalLengthCm", $"PetalWidthCm", $"predictedLabel").as("value")
      )
      .writeStream
      .outputMode("append")
      .format("kafka")
      .option("kafka.bootstrap.servers", "localhost:9092")
      .option("checkpointLocation", "checkpoint/")
      .option("topic", "prediction")
      .start()

    query.awaitTermination()
  }
}
