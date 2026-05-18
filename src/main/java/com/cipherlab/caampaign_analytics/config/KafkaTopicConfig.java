package com.cipherlab.caampaign_analytics.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    @Bean
    public NewTopic campaignEventsTopic() {
        return TopicBuilder.name("campaign-events-raw")
                .partitions(3) // Matches our scaling roadmap!
                .replicas(1)   // Local single-broker setup
                .build();
    }
}