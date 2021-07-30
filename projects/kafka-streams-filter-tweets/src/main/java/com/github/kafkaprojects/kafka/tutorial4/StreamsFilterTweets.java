package com.github.kafkaprojects.kafka.tutorial4;

import com.google.gson.JsonParser;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.KStream;

import java.util.Properties;

public class StreamsFilterTweets {

    public static void main(String[] args) {

        String bootstrapServers = "127.0.0.1:9092";

        // create properties
        Properties properties = new Properties();
        properties.setProperty(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        properties.setProperty(StreamsConfig.APPLICATION_ID_CONFIG, "demo-kafka-streams");
        properties.setProperty(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        properties.setProperty(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.StringSerde.class.getName());
        // create a topology
        StreamsBuilder streamsBuilder = new StreamsBuilder();
        // input topic
        // so this stream takes input from "twitter_topic", then stream to "important_tweets"
        KStream<String, String> inputTopic = streamsBuilder.stream("twitter_topic");
        KStream<String, String> filterStream = inputTopic.filter(
                // java8 // filter for tweets which have a user of over 10000 followers
                (k, jsonTweet) -> extractUserFollowerInTweet(jsonTweet) > 10000
        );
        filterStream.to("important_tweets");
        // build the topology (by passing in the topology and properties)
        KafkaStreams kafkaStreams = new KafkaStreams(
                streamsBuilder.build(),
                properties
        );
        // start our streams application
        kafkaStreams.start();
        }

        private static JsonParser jsonParser = new JsonParser();
        private static int extractUserFollowerInTweet(String tweetJson){
            // gson library
            try {
                return jsonParser.parse(tweetJson)
                        .getAsJsonObject()
                        .get("user")
                        .getAsJsonObject()
                        .get("followers_count")
                        .getAsInt();
            } catch (NullPointerException e){
                return 0;
            }

    }
}
