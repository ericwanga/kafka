# create topic
kafka-topics --zookeeper 127.0.0.1:2181 --create --topic my_topic --partitions 6 --replication-factor 1

# list and describe topics
kafka-topics --zookeeper 127.0.0.1:2181 --list
kafka-topics --zookeeper 127.0.0.1:2181 --topic my_topic --describe

# delete a topic on Windows
# connect to zookeeper instance:
zookeeper-shell.bat localhost:2181
rmr /brokers/topics/your_topic
(deleteall /brokers/topics/your_topic)
# exit zookeeper instance
Ctrl+C

# console producer
kafka-console-producer --bootstrap-server 127.0.0.1:9092 --topic my_topic
kafka-console-producer --bootstrap-server 127.0.0.1:9092 --topic my_topic --producer-properties acks=all



# console consumer 






# console consumer
kafka-console-consumer 

