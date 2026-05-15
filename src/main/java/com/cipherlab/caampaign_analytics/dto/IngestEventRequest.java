package com.cipherlab.caampaign_analytics.dto;

import lombok.Data;

@Data
public class IngestEventRequest {
    private Long campaignId;
    private String eventType; // CLICK or IMPRESSION
}
