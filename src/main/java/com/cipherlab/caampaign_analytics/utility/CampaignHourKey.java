package com.cipherlab.caampaign_analytics.utility;
import java.time.Instant;
public record CampaignHourKey(Long campaignId, Instant hour) {}
