package io.github.brainzy.rankdrop.service;

import io.github.brainzy.rankdrop.dto.ScoreEntryResponse;
import io.github.brainzy.rankdrop.entity.Leaderboard;
import io.github.brainzy.rankdrop.entity.ScoreEntry;
import io.github.brainzy.rankdrop.entity.SortOrder;
import io.github.brainzy.rankdrop.exception.LeaderboardNotFoundException;
import io.github.brainzy.rankdrop.repository.LeaderboardRepository;
import io.github.brainzy.rankdrop.repository.ScoreEntryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScoreCacheService {

    private final ScoreEntryRepository scoreRepository;
    private final LeaderboardRepository leaderboardRepository;

    @Transactional(readOnly = true)
    @Cacheable(value = "topScores", key = "#slug")
    public List<ScoreEntryResponse> getTop100(String slug) {
        try {
            Leaderboard leaderboard = leaderboardRepository.findBySlug(slug)
                    .orElseThrow(() -> new LeaderboardNotFoundException(slug));

            Sort.Direction direction = (leaderboard.getSortOrder() == SortOrder.ASC)
                    ? Sort.Direction.ASC
                    : Sort.Direction.DESC;

            Sort sort = Sort.by(direction, "scoreValue")
                    .and(Sort.by(Sort.Direction.ASC, "submittedAt"));

            Pageable pageable = PageRequest.of(0, 100, sort);
            List<ScoreEntry> entries = scoreRepository
                    .findByLeaderboard_Slug(slug, pageable).getContent();

            List<ScoreEntryResponse> response = new ArrayList<>();
            for (int i = 0; i < entries.size(); i++) {
                response.add(ScoreEntryResponse.fromEntity(entries.get(i), i + 1));
            }
            return response;

        } catch (LeaderboardNotFoundException e) {
            throw e; // Don't swallow legitimate not found errors
        } catch (Exception e) {
            log.warn("DB unavailable for slug '{}', returning empty list. Error: {}", slug, e.getMessage());
            return List.of();
        }
    }

    @CacheEvict(value = "topScores", key = "#slug")
    public void evictTopScoresCache(String slug) {
    }
}