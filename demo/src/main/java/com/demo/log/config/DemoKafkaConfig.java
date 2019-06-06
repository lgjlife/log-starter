package com.demo.log.config;

import com.log.kafka.KafkaConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class DemoKafkaConfig extends KafkaConfig {

    @Override
    public Map<String, Object> config() {
        Map<String, Object> kafkaCfg = new HashMap<>();

        kafkaCfg.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG,"localhost:9092,localhost:9093");

        return kafkaCfg;
    }

    @Override
    public String appName() {
        return "DemoApplication";
    }

    @Override
    public String topic() {
        return "application-log";
    }
}
