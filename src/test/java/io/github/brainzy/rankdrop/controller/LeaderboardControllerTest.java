package io.github.brainzy.rankdrop.controller;

import io.github.brainzy.rankdrop.dto.ScoreEntryResponse;
import io.github.brainzy.rankdrop.dto.ScoreSubmissionRequest;
import io.github.brainzy.rankdrop.dto.ScoreSubmitResponse;
import io.github.brainzy.rankdrop.exception.LeaderboardNotFoundException;
import io.github.brainzy.rankdrop.exception.PlayerBannedException;
import io.github.brainzy.rankdrop.service.ScoreService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LeaderboardControllerTest {

    @Mock
    private ScoreService scoreService;

    @InjectMocks
    private LeaderboardController leaderboardController;

    @Test
    void getTopScores_ValidRequest_ReturnsTopScores() {
        List<ScoreEntryResponse> mockScores = List.of(
                new ScoreEntryResponse(1L, "Player1", 1500.0, 1, null, null),
                new ScoreEntryResponse(2L, "Player2", 1200.0, 2, null, null)
        );

        when(scoreService.getTopScores("test-leaderboard", 10)).thenReturn(mockScores);

        var result = leaderboardController.getTopScores("test-leaderboard", 10);

        assertThat(result.getScores()).hasSize(2);
        assertThat(result.getScores().get(0).getN()).isEqualTo("Player1");
        assertThat(result.getScores().get(0).getS()).isEqualTo(1500.0);

        verify(scoreService).getTopScores("test-leaderboard", 10);
    }

    @Test
    void getTopScores_LeaderboardNotFound_ThrowsLeaderboardNotFoundException() {
        when(scoreService.getTopScores(anyString(), anyInt()))
                .thenThrow(new LeaderboardNotFoundException("test-leaderboard"));

        assertThatThrownBy(() -> leaderboardController.getTopScores("non-existent", 10))
                .isInstanceOf(LeaderboardNotFoundException.class);
    }

    @Test
    void submitScore_ValidRequest_ReturnsScoreSubmitResponse() {
        ScoreSubmissionRequest request = new ScoreSubmissionRequest("TestPlayer", 1500.0, "Test metadata");
        ScoreSubmitResponse mockResponse = new ScoreSubmitResponse(1, 1500.0);

        when(scoreService.submitScore("test-leaderboard", "TestPlayer", 1500.0, "Test metadata"))
                .thenReturn(mockResponse);

        var result = leaderboardController.submitScore("test-leaderboard", request);

        assertThat(result.getScore()).isEqualTo(1500.0);
        assertThat(result.getRank()).isEqualTo(1);

        verify(scoreService).submitScore("test-leaderboard", "TestPlayer", 1500.0, "Test metadata");
    }

    @Test
    void submitScore_BannedPlayer_ReturnsPlayerBannedException() {
        ScoreSubmissionRequest request = new ScoreSubmissionRequest("BannedPlayer", 1500.0, "Test metadata");

        when(scoreService.submitScore(anyString(), anyString(), anyDouble(), anyString()))
                .thenThrow(new PlayerBannedException("BannedPlayer"));

        assertThatThrownBy(() -> leaderboardController.submitScore("test-leaderboard", request))
                .isInstanceOf(PlayerBannedException.class);
    }

    @Test
    void submitScore_LeaderboardNotFound_ReturnsLeaderboardNotFoundException() {
        ScoreSubmissionRequest request = new ScoreSubmissionRequest("TestPlayer", 1500.0, "Test metadata");

        when(scoreService.submitScore(anyString(), anyString(), anyDouble(), anyString()))
                .thenThrow(new LeaderboardNotFoundException("non-existent"));

        assertThatThrownBy(() -> leaderboardController.submitScore("non-existent", request))
                .isInstanceOf(LeaderboardNotFoundException.class);
    }


}
