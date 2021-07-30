package kafka.testproject1;

import org.apache.kafka.clients.producer.*;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.concurrent.ExecutionException;

public class ProducerDemoKeys {
    public static void main(String[] args) throws ExecutionException, InterruptedException {

        Logger logger = LoggerFactory.getLogger(ProducerDemoKeys.class);

        String bootstrapServers = "127.0.0.1:9092";

        // create Producer properties
        Properties properties = new Properties();
//        properties.setProperty("bootstrap.servers", bootstrapServers);
//        properties.setProperty("key.serializer", "");
//        properties.setProperty("value.serializer", "");

        properties.setProperty(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.setProperty(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());

        // create the producer
        KafkaProducer<String, String> producer = new KafkaProducer<>(properties);

        for (int i=0; i<10; i++) {
            // create a producer record

            String topic = "first_topic";
            String value = "hello world " + Integer.toString(i);
            String key = "id_" + Integer.toString(i);

            ProducerRecord<String, String> record =
                    new ProducerRecord<>(topic, key, value);

            logger.info("Key: " + key); // log the key
            // from the logs we can see:
            // id_0 is going to partition 1
            // id_1 is partition 0
            // id_2 is partition 2
            // id_3 is partition 0
            // id_4 is partition 2
            // id_5 is partition 2
            // id_6 is partition 0
            // id_7 is partition 2
            // id_8 is partition 1
            // id_9 is partition 2
            // re-run, and will always get the same partition allocation
            // because we used key

            // - by providing the key, it is guaranteed that same key record always going to the same partition

            // send data - asynchronous (background, data is never sent only by this. Need flush)
            producer.send(record, new Callback() {
                @Override
                public void onCompletion(RecordMetadata metadata, Exception exception) {
                    // executes every time a record is sent or an exception is thrown
                    if (exception == null) {
                        // the record was successfully sent
                        logger.info("Received new metadata. \n" +
                                "Topic: " + metadata.topic() + "\n" +
                                "Partition: " + metadata.partition() + "\n" +
                                "Offset: " + metadata.offset() + "\n" +
                                "Timestamp: " + metadata.timestamp());
                    } else {
                        // handle the exception
                        logger.error("Error while producing ", exception);
                    }
                }
            }).get(); // this blocks the send, and force the method to be synchronous
            // Will add InterruptedException
            // DO NOT do this in production!
        }

        // flush data
        producer.flush();
        // flush and close producer
        producer.close();
    }
}
