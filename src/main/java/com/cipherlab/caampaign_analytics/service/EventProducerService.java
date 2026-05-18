package com.cipherlab.caampaign_analytics.service;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import com.cipherlab.caampaign_analytics.dto.CampaignEventMessage;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventProducerService {

    private final KafkaTemplate<String, CampaignEventMessage> kafkaTemplate;
    private static final String TOPIC = "campaign-events-raw";

    public void sendEvent(CampaignEventMessage message) {
        // We use the CampaignId as the 'key' to ensure all events for the same 
        // campaign land in the same Kafka partition (order matters!).
        String key = message.getCampaignId().toString();
        
        kafkaTemplate.send(TOPIC, key, message);
        log.info("Sent event {} to Kafka topic {}", message.getEventId(), TOPIC);
    }
}