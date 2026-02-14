package io.github.brainzy.rankdrop.controller;

import io.github.brainzy.rankdrop.dto.ScoreSubmissionRequest;
import io.github.brainzy.rankdrop.entity.ScoreEntry;
import io.github.brainzy.rankdrop.repository.LeaderboardRepository;
import io.github.brainzy.rankdrop.service.ScoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/leaderboards")
@Validated
@RequiredArgsConstructor
public class LeaderboardController {

    private final ScoreService scoreService;
    private final LeaderboardRepository leaderboardRepository;

    @GetMapping("/{slug}/top")
    public List<ScoreEntry> getTopScores(@PathVariable String slug) {
        return scoreService.getTopScores(slug);
    }

    @PostMapping("/{slug}/scores")
    public ScoreEntry submitScore(
            @PathVariable String slug,
            @Valid @RequestBody ScoreSubmissionRequest request) {

        return scoreService.submitScore(slug, request.name(), request.score());
    }
}