package io.github.brainzy.rankdrop.service;

import io.github.brainzy.rankdrop.dto.WebhookPayload;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class WebhookService {

    private final SystemSettingService systemSettingService;
    private final RestClient restClient;

    @Async
    public void fireTopScoreWebhookIfEligible(String slug, String playerAlias, double score, int rank) {
        String webhookUrl = systemSettingService.getSetting("WEBHOOK_URL");
        if (webhookUrl == null || webhookUrl.isBlank()) {
            return;
        }

        int topN = Integer.parseInt(systemSettingService.getSetting("WEBHOOK_TOP_N", "10"));
        if (rank > topN) {
            return;
        }

        if (!isCooldownExpired()) {
            return;
        }

        try {
            WebhookPayload payload = new WebhookPayload("NEW_TOP_SCORE", slug,
                    playerAlias, score, rank, LocalDateTime.now().toString());
            restClient.post().uri(webhookUrl).body(payload).retrieve().toBodilessEntity();
            systemSettingService.setSetting("WEBHOOK_LAST_FIRED", LocalDateTime.now().toString());
        } catch (Exception e) {
            log.warn("Webhook failed: {}", e.getMessage());
        }
    }

    private boolean isCooldownExpired() {
        String lastFired = systemSettingService.getSetting("WEBHOOK_LAST_FIRED");
        if (lastFired == null) {
            return true;
        }

        long cooldown = Long.parseLong(systemSettingService.getSetting("WEBHOOK_COOLDOWN_MS", "10000"));
        return LocalDateTime.parse(lastFired).plusNanos(cooldown * 1_000_000L).isBefore(LocalDateTime.now());
    }
}
