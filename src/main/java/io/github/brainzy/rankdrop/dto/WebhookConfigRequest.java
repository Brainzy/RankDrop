package io.github.brainzy.rankdrop.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Request object for configuring webhook settings")
public record WebhookConfigRequest(
        @Schema(description = "Webhook URL for notifications", example = "https://discord.com/api/webhooks/1234567890/abcdefghijk")
        String webhookUrl,
        
        @Schema(description = "Top N scores that trigger webhook", example = "10")
        Integer topN,
        
        @Schema(description = "Cooldown period between webhook calls in milliseconds", example = "300000")
        Long cooldownMs
) {}
