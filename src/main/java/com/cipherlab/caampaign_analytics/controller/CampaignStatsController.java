package com.cipherlab.caampaign_analytics.controller;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.cipherlab.caampaign_analytics.entity.CampaignStats;
import com.cipherlab.caampaign_analytics.repository.CampaignStatsRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/stats")
@RequiredArgsConstructor
public class CampaignStatsController {

    private final CampaignStatsRepository statsRepository;

    @GetMapping("/{campaignId}")
    public ResponseEntity<List<CampaignStats>> getDashboardStats(
            @PathVariable Long campaignId,
            @RequestParam(required = false) Instant start,
            @RequestParam(required = false) Instant end) {

        // Default to last 24 hours if no range is provided
        Instant startTime = (start != null) ? start : Instant.now().minus(24, ChronoUnit.HOURS);
        Instant endTime = (end != null) ? end : Instant.now();

        List<CampaignStats> stats = statsRepository
            .findByCampaignIdAndHourBucketBetweenOrderByHourBucketAsc(campaignId, startTime, endTime);
                
        return ResponseEntity.ok(stats);
    }
}