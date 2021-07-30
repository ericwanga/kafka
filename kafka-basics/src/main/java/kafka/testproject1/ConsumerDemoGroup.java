package kafka.testproject1;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;

public class ConsumerDemoGroup {
    public static void main(String[] args) {
        // test
//        System.out.println("test");

        Logger logger = LoggerFactory.getLogger(ConsumerDemoGroup.class.getName());

        String bootstrapServers = "127.0.0.1:9092";
        String groupId = "my-fourth-application";
        String topic = "first_topic";

        // create consumer properties
        Properties properties = new Properties();
        properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        // create consumer
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);

        // subscribe consumer to our topic(s)
        // use Collections.singleton means only subscribe to 1 topic
//        consumer.subscribe(Collections.singleton(topic));
        // or use Arrays.asList to subscribe to many topics
//        consumer.subscribe(Arrays.asList("first_topic", "second_topic", "other_topics"));
        // for now, we use it this way:
        consumer.subscribe(Arrays.asList(topic));

        // poll轮询 for new data
        while (true){
//            consumer.poll(100);// poll(timeout 100) is deprecated, need to use poll(Duration)
//            consumer.poll(Duration.ofMillis(100)); // new in kafka 2.0.0
            ConsumerRecords<String, String> records =
                    consumer.poll(Duration.ofMillis(100)); // new in kafka 2.0.0

            // look into the records
            for (ConsumerRecord<String, String> record : records){
                logger.info("Key " + record.key() + ", Value: " + record.value());
                logger.info("Partition: " + record.partition() + ", Offset: " + record.offset());
            }
        }
    }
}
