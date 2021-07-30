package kafka.testproject1;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Arrays;
import java.util.Properties;
import java.util.concurrent.CountDownLatch;

public class ConsumerDemoWithThread {

    public static void main(String[] args) {
        new ConsumerDemoWithThread().run();
    }

    private ConsumerDemoWithThread(){

    }

    private void run(){
        Logger logger = LoggerFactory.getLogger(ConsumerDemoWithThread.class.getName());

        String bootstrapServers = "127.0.0.1:9092";
        String groupId = "my-sixth-application";
        String topic = "first_topic";

        CountDownLatch latch = new CountDownLatch(1);

        logger.info("Creating the consumer thread");
        Runnable myConsumerRunnable = new ConsumerRunnable(
                bootstrapServers,
                groupId,
                topic,
                latch);

        // start the thread
        Thread myThread = new Thread(myConsumerRunnable);
        myThread.start();

        // add a shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread( () -> {
            logger.info("Caught shutdown hook");
            ((ConsumerRunnable) myConsumerRunnable).shutdown();
            try {
                latch.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            logger.info("Application has exited");
        }
        ));

        try {
            latch.await();
        } catch (InterruptedException e) {
            logger.error("Application got interrupted ", e);
        } finally {
            logger.info("Application is closing");
        }
    }

    // Will create a new class to do the threads things
    public class ConsumerRunnable implements Runnable {

        // 3. need to construct a countdown latch, which handles concurrency并发 in java
        // also create consumer and logger inside ConsumerThread
        private CountDownLatch latch;
        private KafkaConsumer<String, String> consumer;
        private Logger logger = LoggerFactory.getLogger(ConsumerRunnable.class.getName());

        public ConsumerRunnable(String bootstrapServers,
                              String groupId,
                              String topic,
                              CountDownLatch latch) {
            this.latch = latch;

            // create consumer properties
            Properties properties = new Properties();
            properties.setProperty(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
            properties.setProperty(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
            properties.setProperty(ConsumerConfig.GROUP_ID_CONFIG, groupId);
            properties.setProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

            // initialize the consumer
            consumer = new KafkaConsumer<>(properties);
            // subscribe consumer to our topic(s)
            // use Collections.singleton means only subscribe to 1 topic
//        consumer.subscribe(Collections.singleton(topic));
            // or use Arrays.asList to subscribe to many topics
//        consumer.subscribe(Arrays.asList("first_topic", "second_topic", "other_topics"));
            // for now, we use it this way:
            consumer.subscribe(Arrays.asList(topic));
        }

        // need to move the while(true) loop to inside of run():
        @Override
        public void run() {
            // poll for new data
            // wrap the while(true) loop inside a try-catch
            try {
                while (true) {
                    // consumer.poll(100);// poll(timeout 100) is deprecated, need to use poll(Duration)
                    // consumer.poll(Duration.ofMillis(100)); // new in kafka 2.0.0
                    ConsumerRecords<String, String> records =
                            consumer.poll(Duration.ofMillis(100)); // new in kafka 2.0.0

                    // look into the records
                    for (ConsumerRecord<String, String> record : records) {
                        logger.info("Key " + record.key() + ", Value: " + record.value());
                        logger.info("Partition: " + record.partition() + ", Offset: " + record.offset());
                    }
                }
            } catch (WakeupException e){
                // don't need to print out this e, because it is an expected exception
                logger.info("Received shutdown signal!");
            } finally {
                consumer.close();
                // tell our main code we're down with the consumer
                latch.countDown();
            }
        }

        // on top of run, create a shutdown, to shutdown the consumer thread
        // and throw an exception to above to catch
        public void shutdown(){
            // the wakeup() method is a special method to interrupt consumer.poll()
            // it will throw the exception WakeUpException (goes to above catch)
            consumer.wakeup();
        }
    }
}
