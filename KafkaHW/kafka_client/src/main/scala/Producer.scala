import org.apache.kafka.clients.admin.{AdminClient, NewTopic}
import org.apache.kafka.clients.producer.{Callback, KafkaProducer, ProducerRecord, RecordMetadata}
import org.apache.kafka.common.errors.TopicExistsException
import org.apache.kafka.common.serialization.StringSerializer

import java.util.{Collections, Properties}
import scala.io.{Codec, Source}
import scala.util.Try
import scala.util.parsing.json.{JSONArray, JSONObject, JSONType}

object Producer extends App {

  val props = new Properties()
  props.put("bootstrap.servers", "localhost:29092")

  val topic = "books";

  createTopic(topic, props)

  val producer = new KafkaProducer(props, new StringSerializer, new StringSerializer)

  // каллбакк если пригодится что-то выполнить после отправки
  val callback = new Callback {
    override def onCompletion(metadata: RecordMetadata, exception: Exception): Unit = {
      Option(exception) match {
        case Some(err) => println(s"Failed to produce: $err")
        case None =>  println(s"Produced record at $metadata")
      }
    }
  }


  val lines = Source.fromFile("src/main/resources/data/bestsellers with categories.csv")(Codec.UTF8).getLines()

  val columns = "Name,Author,User Rating,Reviews,Price,Year,Genre".split(",")

  // сериализация в json
  val json = buildJson(lines, columns)

  json.asInstanceOf[JSONArray].list.foreach { m =>
    producer.send(new ProducerRecord(topic, m.toString, m.toString), callback) //books"
  }

  producer.flush()

  producer.close()

  def buildJson(lines: Iterator[String], columns: Seq[String]): JSONType = {
    val apps = lines map {
      line =>
        val fields = splitLines(line)
        val zipped = columns zip fields

        JSONObject(zipped.toMap)
    }

    JSONArray(apps.toList)
  }


  def splitLines(line: String): Seq[String] = {
    line.split(",") map (_.trim)
  }

  def createTopic(topic: String, clusterConfig: Properties): Unit = {
    val newTopic = new NewTopic(topic,3, new Integer(1).shortValue());
    val adminClient = AdminClient.create(clusterConfig)
    Try (adminClient.createTopics(Collections.singletonList(newTopic)).all.get).recover {
      case e :Exception =>
        // Ignore if TopicExistsException, which may be valid if topic exists
        if (!e.getCause.isInstanceOf[TopicExistsException]) throw new RuntimeException(e)
    }
    adminClient.close()
  }

}
