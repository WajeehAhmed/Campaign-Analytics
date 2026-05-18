package com.cipherlab.caampaign_analytics.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cipherlab.caampaign_analytics.dto.CampaignEventMessage;
import com.cipherlab.caampaign_analytics.dto.IngestEventRequest;
import com.cipherlab.caampaign_analytics.service.EventProducerService;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventsController {

    private final EventProducerService producerService;

    @PostMapping
    public ResponseEntity<?> ingestEvent(@RequestBody IngestEventRequest request) {
        CampaignEventMessage message = CampaignEventMessage.builder()
                        .eventId(UUID.randomUUID())
                        .campaignId(request.getCampaignId())
                        .eventType(request.getEventType())
                        .timestamp(java.time.Instant.now())
                        .build();                

        producerService.sendEvent(message);

        // 3. Return a quick 202 Accepted
        return ResponseEntity.accepted().body(java.util.Map.of("status", "queued"));
    }
}