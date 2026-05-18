package com.cipherlab.caampaign_analytics.dto;

import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CampaignEventMessage {
    private UUID eventId;
    private Long campaignId;
    private String eventType;
    private Instant timestamp;
}