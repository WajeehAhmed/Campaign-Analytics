package com.cipherlab.caampaign_analytics.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.cipherlab.caampaign_analytics.dto.IngestEventRequest;
import com.cipherlab.caampaign_analytics.entity.RawEvent;
import com.cipherlab.caampaign_analytics.repository.RawEventRepository;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/events")
@RequiredArgsConstructor
public class EventsController {

    private final RawEventRepository rawEventRepository;

    @PostMapping
    public ResponseEntity<?> ingestEvent(@RequestBody IngestEventRequest request) {
        // 1. Convert DTO to Entity
        RawEvent event = RawEvent.builder()
                .campaignId(request.getCampaignId())
                .eventType(request.getEventType())
                .build();

        // 2. Save the "Raw" data (The Write-Heavy part)
        rawEventRepository.save(event);

        // 3. Return a quick 202 Accepted
        return ResponseEntity.accepted().body(Map.of(
            "status", "accepted",
            "message", "Event recorded successfully"
        ));
    }
}