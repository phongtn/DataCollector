package com.wind;

import com.wind.kafka.ClientConsumer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Application {

    private final static Logger logger = LoggerFactory.getLogger(Application.class);

    public static void main(String[] args) {
        ClientConsumer consumer = new ClientConsumer();
        consumer.startConsumer(true);
    }
}
