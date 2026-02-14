package io.github.brainzy.rankdrop.controller;

import io.github.brainzy.rankdrop.dto.LeaderboardCreateRequest;
import io.github.brainzy.rankdrop.dto.LeaderboardUpdateRequest;
import io.github.brainzy.rankdrop.entity.Leaderboard;
import io.github.brainzy.rankdrop.service.LeaderboardService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin/leaderboards")
public class AdminController {

    private final LeaderboardService leaderboardService;

    public AdminController(LeaderboardService leaderboardService) {
        this.leaderboardService = leaderboardService;
    }

    @PostMapping
    public ResponseEntity<Leaderboard> create(@Valid @RequestBody LeaderboardCreateRequest request) {
        Leaderboard saved = leaderboardService.createNewLeaderboard(request);
        return new ResponseEntity<>(saved, HttpStatus.CREATED);
    }

    @PutMapping("/{slug}")
    public ResponseEntity<Leaderboard> update(@PathVariable String slug, @Valid @RequestBody LeaderboardUpdateRequest request) {
        return ResponseEntity.ok(
                leaderboardService.updateExistingLeaderboard(slug, request.displayName())
        );
    }

    @DeleteMapping("/{slug}")
    public ResponseEntity<Void> delete(@PathVariable String slug) {
        leaderboardService.deleteLeaderboardBySlug(slug);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public List<Leaderboard> list() {
        return leaderboardService.getAllLeaderboards();
    }
}