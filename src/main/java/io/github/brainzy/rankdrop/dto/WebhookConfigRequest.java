package io.github.brainzy.rankdrop.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request object for configuring webhook settings")
public class WebhookConfigRequest {
    @Schema(description = "Webhook URL for notifications", example = "https://discord.com/api/webhooks/1234567890/abcdefghijk")
    private String webhookUrl;
    
    @Schema(description = "Top N scores that trigger webhook", example = "10")
    private Integer topN;
    
    @Schema(description = "Cooldown period between webhook calls in milliseconds", example = "300000")
    private Long cooldownMs;
}
