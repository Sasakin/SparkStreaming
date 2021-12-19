import org.apache.kafka.clients.admin.Admin
import org.apache.kafka.clients.consumer.{ConsumerConfig, KafkaConsumer}
import org.apache.kafka.common.TopicPartition
import org.apache.kafka.common.serialization.StringDeserializer

import scala.jdk.CollectionConverters._
import java.util.{Collections, Properties}
import java.time.Duration

object Consumer extends App {

  val props = new Properties()
  /*props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
  props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, "org.apache.kafka.common.serialization.StringDeserializer")
  props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")*/
  props.put("bootstrap.servers", "localhost:29092")
  props.put("group.id", "consumer1")

  val topic = "books"

  val consumer = new KafkaConsumer(props, new StringDeserializer, new StringDeserializer)



  //consumer.subscribe(List(topic).asJavaCollection)

  println("Polling")


  val partitions = getPartitions(topic, props).asJava
  consumer.assign(partitions)
  consumer.seekToEnd(partitions)

  partitions.forEach(p =>  {
    val last = consumer.position(p)
    consumer.seek(p, last - 5)
  })


  val records = consumer.poll(Duration.ofSeconds(1))
  records.forEach(record => {
    val key = record.key()
    val value = record.value()
    print("PartNum: "+ record.partition() + "  offset: " + record.offset() + "   Value:\n" + value)
  })

  consumer.close()


  def getPartitions(topic: String, clusterConfig: Properties) = {
    val topicDesc = Admin.create(clusterConfig)
      .describeTopics(Collections.singletonList(topic))
      .values()
      .get(topic)
      .get()
    topicDesc.partitions().asScala
      .map(partitionInfo => new TopicPartition(topic, partitionInfo.partition()))
      .toList
  }

}
