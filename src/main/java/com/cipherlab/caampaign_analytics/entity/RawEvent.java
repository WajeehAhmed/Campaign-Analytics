package com.cipherlab.caampaign_analytics.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "raw_events")
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class RawEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private Long campaignId;
    private String eventType; // "CLICK" or "IMPRESSION"
    
    @Column(nullable = false)
    private Instant timestamp;

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) this.timestamp = Instant.now();
    }
}