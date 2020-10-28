package com.wind.kafka;

import com.typesafe.config.Config;
import com.typesafe.config.ConfigFactory;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

public class ClientConsumer {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final Config config;

    public ClientConsumer() {
        this.config = ConfigFactory.load().getConfig("kafka");
    }

    public void startConsumer(boolean running) {
        Properties properties = this.buildProperties();
        KafkaConsumer<String, String> consumer = new KafkaConsumer<>(properties);
        consumer.subscribe(config.getStringList("topics"));
        try {
            while (running) {
                long start = System.currentTimeMillis();
                logger.info("Start fetch {}", start);
                ConsumerRecords<String, String> records = consumer.poll(Duration.ofMillis(1000 * 30));
                for (TopicPartition partition : records.partitions()) {
                    logger.info("Partition {}", ToStringBuilder.reflectionToString(partition));
                    List<ConsumerRecord<String, String>> partitionRecords = records.records(partition);
                    partitionRecords.forEach(record -> {
                        logger.info("Record offset {}. Record value {}", record.offset(), record.value());
                    });
                    long lastOffset = partitionRecords.get(partitionRecords.size() - 1).offset();
                    logger.info("Last offset {}", lastOffset);
                    consumer.commitSync(Collections.singletonMap(partition, new OffsetAndMetadata(lastOffset + 1)));

                }
                logger.info("Total time to fetch {}", System.currentTimeMillis() - start);
            }
        } finally {
            logger.info("Request close consumer {}", consumer.groupMetadata());
        }
    }

    private Properties buildProperties() {
        Properties props = new Properties();
        props.setProperty("bootstrap.servers", config.getString("bootstrap.servers"));
        props.setProperty("group.id", config.getString("group-id"));
        props.setProperty("enable.auto.commit", "false");
        props.setProperty("key.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        props.setProperty("value.deserializer", "org.apache.kafka.common.serialization.StringDeserializer");
        return props;
    }
}
