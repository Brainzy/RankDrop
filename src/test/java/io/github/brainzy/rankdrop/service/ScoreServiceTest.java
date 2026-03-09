package io.github.brainzy.rankdrop.service;

import io.github.brainzy.rankdrop.dto.ScoreEntryResponse;
import io.github.brainzy.rankdrop.dto.ScoreSubmitResponse;
import io.github.brainzy.rankdrop.entity.Leaderboard;
import io.github.brainzy.rankdrop.entity.ScoreEntry;
import io.github.brainzy.rankdrop.entity.ScoreStrategy;
import io.github.brainzy.rankdrop.entity.SortOrder;
import io.github.brainzy.rankdrop.exception.LeaderboardNotFoundException;
import io.github.brainzy.rankdrop.exception.PlayerBannedException;
import io.github.brainzy.rankdrop.exception.PlayerNotFoundException;
import io.github.brainzy.rankdrop.repository.LeaderboardRepository;
import io.github.brainzy.rankdrop.repository.ScoreEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.SliceImpl;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ScoreServiceTest {

    @Mock
    private ScoreEntryRepository scoreRepository;

    @Mock
    private LeaderboardRepository leaderboardRepository;

    @Mock
    private ScoreCacheService scoreCacheService;

    @Mock
    private PlayerService playerService;

    @Mock
    private WebhookService webhookService;

    @InjectMocks
    private ScoreService scoreService;

    private Leaderboard testLeaderboard;
    private ScoreEntry testScoreEntry;

    @BeforeEach
    void setUp() {
        testLeaderboard = Leaderboard.builder()
                .id(1L)
                .slug("test-leaderboard")
                .displayName("Test Leaderboard")
                .sortOrder(SortOrder.DESC)
                .scoreStrategy(ScoreStrategy.BEST_ONLY)
                .minScore(0.0)
                .maxScore(1000000.0)
                .build();

        testScoreEntry = ScoreEntry.builder()
                .id(1L)
                .leaderboard(testLeaderboard)
                .playerAlias("TestPlayer")
                .scoreValue(1500.0)
                .submittedAt(LocalDateTime.now(ZoneOffset.UTC))
                .metadata("Test metadata")
                .build();
    }

    @Test
    void submitScore_ValidScore_ReturnsScoreSubmitResponse() {
        when(leaderboardRepository.findBySlug("test-leaderboard")).thenReturn(Optional.of(testLeaderboard));
        when(playerService.isPlayerBanned("TestPlayer")).thenReturn(false);
        when(scoreRepository.updateIfHigherScore(anyLong(), anyString(), anyDouble(), any())).thenReturn(0);
        when(scoreRepository.findByLeaderboardIdAndPlayerAlias(anyLong(), anyString())).thenReturn(Optional.empty());
        when(scoreRepository.save(any(ScoreEntry.class))).thenReturn(testScoreEntry);
        when(scoreRepository.countBetterScoresDesc(anyLong(), anyDouble(), any())).thenReturn(5L);

        ScoreSubmitResponse response = scoreService.submitScore("test-leaderboard", "TestPlayer", 1500.0, "Test metadata");

        assertThat(response.getScore()).isEqualTo(1500.0);
        assertThat(response.getRank()).isEqualTo(6);
        verify(scoreCacheService).evictTopScoresCache("test-leaderboard");
        verify(webhookService).fireTopScoreWebhookIfEligible("test-leaderboard", "TestPlayer", 1500.0, 6);
    }

    @Test
    void submitScore_LeaderboardNotFound_ThrowsLeaderboardNotFoundException() {
        when(leaderboardRepository.findBySlug("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scoreService.submitScore("non-existent", "TestPlayer", 1500.0, "Test metadata"))
                .isInstanceOf(LeaderboardNotFoundException.class);
    }

    @Test
    void submitScore_BannedPlayer_ThrowsPlayerBannedException() {
        when(leaderboardRepository.findBySlug("test-leaderboard")).thenReturn(Optional.of(testLeaderboard));
        when(playerService.isPlayerBanned("BannedPlayer")).thenReturn(true);

        assertThatThrownBy(() -> scoreService.submitScore("test-leaderboard", "BannedPlayer", 1500.0, "Test metadata"))
                .isInstanceOf(PlayerBannedException.class);
    }

    @Test
    void submitScore_ScoreBelowMinimum_ThrowsIllegalArgumentException() {
        testLeaderboard.setMinScore(1000.0);
        when(leaderboardRepository.findBySlug("test-leaderboard")).thenReturn(Optional.of(testLeaderboard));
        when(playerService.isPlayerBanned("TestPlayer")).thenReturn(false);

        assertThatThrownBy(() -> scoreService.submitScore("test-leaderboard", "TestPlayer", 500.0, "Test metadata"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("below the minimum allowed value");
    }

    @Test
    void submitScore_ScoreAboveMaximum_ThrowsIllegalArgumentException() {
        testLeaderboard.setMaxScore(2000.0);
        when(leaderboardRepository.findBySlug("test-leaderboard")).thenReturn(Optional.of(testLeaderboard));
        when(playerService.isPlayerBanned("TestPlayer")).thenReturn(false);

        assertThatThrownBy(() -> scoreService.submitScore("test-leaderboard", "TestPlayer", 2500.0, "Test metadata"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("exceeds the maximum allowed value");
    }

    @Test
    void submitScore_BestOnlyStrategy_UpdatesExistingScoreIfHigher() {
        testLeaderboard.setScoreStrategy(ScoreStrategy.BEST_ONLY);
        ScoreEntry existingEntry = ScoreEntry.builder()
                .id(1L)
                .leaderboard(testLeaderboard)
                .playerAlias("TestPlayer")
                .scoreValue(1000.0)
                .submittedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        when(leaderboardRepository.findBySlug("test-leaderboard")).thenReturn(Optional.of(testLeaderboard));
        when(playerService.isPlayerBanned("TestPlayer")).thenReturn(false);
        when(scoreRepository.updateIfHigherScore(anyLong(), anyString(), anyDouble(), any())).thenReturn(1);
        when(scoreRepository.findByLeaderboardIdAndPlayerAlias(anyLong(), anyString())).thenReturn(Optional.of(existingEntry));
        when(scoreRepository.countBetterScoresDesc(anyLong(), anyDouble(), any())).thenReturn(2L);

        ScoreSubmitResponse response = scoreService.submitScore("test-leaderboard", "TestPlayer", 1500.0, "Test metadata");

        assertThat(response.getScore()).isEqualTo(1500.0);
        assertThat(response.getRank()).isEqualTo(6);
    }

    @Test
    void submitScore_CumulativeStrategy_IncrementsExistingScore() {
        testLeaderboard.setScoreStrategy(ScoreStrategy.CUMULATIVE);
        ScoreEntry existingEntry = ScoreEntry.builder()
                .id(1L)
                .leaderboard(testLeaderboard)
                .playerAlias("TestPlayer")
                .scoreValue(1000.0)
                .submittedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        when(leaderboardRepository.findBySlug("test-leaderboard")).thenReturn(Optional.of(testLeaderboard));
        when(playerService.isPlayerBanned("TestPlayer")).thenReturn(false);
        when(scoreRepository.incrementScore(anyLong(), anyString(), anyDouble(), any())).thenReturn(1);
        when(scoreRepository.findByLeaderboardIdAndPlayerAlias(anyLong(), anyString())).thenReturn(Optional.of(existingEntry));
        when(scoreRepository.countBetterScoresDesc(anyLong(), anyDouble(), any())).thenReturn(2L);

        ScoreSubmitResponse response = scoreService.submitScore("test-leaderboard", "TestPlayer", 500.0, "Test metadata");

        assertThat(response.getScore()).isEqualTo(1500.0);
        assertThat(response.getRank()).isEqualTo(6);
        verify(scoreRepository).incrementScore(testLeaderboard.getId(), "TestPlayer", 500.0, any());
    }

    @Test
    void submitScore_MultipleEntriesStrategy_CreatesNewEntry() {
        testLeaderboard.setScoreStrategy(ScoreStrategy.MULTIPLE_ENTRIES);
        when(leaderboardRepository.findBySlug("test-leaderboard")).thenReturn(Optional.of(testLeaderboard));
        when(playerService.isPlayerBanned("TestPlayer")).thenReturn(false);
        when(scoreRepository.save(any(ScoreEntry.class))).thenReturn(testScoreEntry);
        when(scoreRepository.countBetterScoresDesc(anyLong(), anyDouble(), any())).thenReturn(5L);

        ScoreSubmitResponse response = scoreService.submitScore("test-leaderboard", "TestPlayer", 1500.0, "Test metadata");

        assertThat(response.getScore()).isEqualTo(1500.0);
        assertThat(response.getRank()).isEqualTo(6);
        verify(scoreRepository).save(any(ScoreEntry.class));
    }

    @Test
    void submitScore_DuplicatePlayer_UpsertsCorrectly() {
        testLeaderboard.setScoreStrategy(ScoreStrategy.BEST_ONLY);
        ScoreEntry existingEntry = ScoreEntry.builder()
                .id(1L)
                .leaderboard(testLeaderboard)
                .playerAlias("TestPlayer")
                .scoreValue(1000.0)
                .submittedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        when(leaderboardRepository.findBySlug("test-leaderboard")).thenReturn(Optional.of(testLeaderboard));
        when(playerService.isPlayerBanned("TestPlayer")).thenReturn(false);
        when(scoreRepository.updateIfHigherScore(anyLong(), anyString(), anyDouble(), any())).thenReturn(0);
        when(scoreRepository.findByLeaderboardIdAndPlayerAlias(anyLong(), anyString())).thenReturn(Optional.of(existingEntry));
        when(scoreRepository.countBetterScoresDesc(anyLong(), anyDouble(), any())).thenReturn(2L);

        ScoreSubmitResponse response = scoreService.submitScore("test-leaderboard", "TestPlayer", 800.0, "Test metadata");

        assertThat(response.getScore()).isEqualTo(1000.0);
        assertThat(response.getRank()).isEqualTo(6);
        verify(scoreRepository, never()).save(any(ScoreEntry.class));
    }

    @Test
    void getTopScores_ReturnsLimitedScores() {
        List<ScoreEntryResponse> mockTopScores = List.of(
                ScoreEntryResponse.fromEntity(testScoreEntry, 1),
                ScoreEntryResponse.fromEntity(testScoreEntry, 2)
        );

        when(scoreCacheService.getTop100("test-leaderboard")).thenReturn(mockTopScores);

        List<ScoreEntryResponse> result = scoreService.getTopScores("test-leaderboard", 5);

        assertThat(result).hasSize(2);
        verify(scoreCacheService).getTop100("test-leaderboard");
    }

    @Test
    void getPlayerScoreWithSurrounding_ValidPlayer_ReturnsPlayerWithSurrounding() {
        when(leaderboardRepository.findBySlug("test-leaderboard")).thenReturn(Optional.of(testLeaderboard));
        when(scoreRepository.findByLeaderboardIdAndPlayerAlias(anyLong(), anyString())).thenReturn(Optional.of(testScoreEntry));
        when(scoreRepository.countBetterScoresDesc(anyLong(), anyDouble(), any())).thenReturn(5L);
        when(scoreRepository.findHigherScores(anyLong(), anyDouble(), any(), any())).thenReturn(new SliceImpl<>(List.of()));
        when(scoreRepository.findLowerScores(anyLong(), anyDouble(), any(), any())).thenReturn(new SliceImpl<>(List.of()));

        List<ScoreEntryResponse> result = scoreService.getPlayerScoreWithSurrounding("test-leaderboard", "TestPlayer", 2);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getPlayerAlias()).isEqualTo("TestPlayer");
        assertThat(result.getFirst().getRank()).isEqualTo(6);
    }

    @Test
    void getPlayerScoreWithSurrounding_PlayerNotFound_ThrowsPlayerNotFoundException() {
        when(leaderboardRepository.findBySlug("test-leaderboard")).thenReturn(Optional.of(testLeaderboard));
        when(scoreRepository.findByLeaderboardIdAndPlayerAlias(anyLong(), anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scoreService.getPlayerScoreWithSurrounding("test-leaderboard", "NonExistentPlayer", 2))
                .isInstanceOf(PlayerNotFoundException.class);
    }

    @Test
    void getPlayerScoreWithSurrounding_LeaderboardNotFound_ThrowsLeaderboardNotFoundException() {
        when(leaderboardRepository.findBySlug("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scoreService.getPlayerScoreWithSurrounding("non-existent", "TestPlayer", 2))
                .isInstanceOf(LeaderboardNotFoundException.class);
    }

    @Test
    void getPlayerScoreWithSurrounding_WithSurroundingPlayers_ReturnsCorrectOrder() {
        ScoreEntry higherScore = ScoreEntry.builder()
                .id(2L)
                .leaderboard(testLeaderboard)
                .playerAlias("HigherPlayer")
                .scoreValue(2000.0)
                .submittedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        ScoreEntry lowerScore = ScoreEntry.builder()
                .id(3L)
                .leaderboard(testLeaderboard)
                .playerAlias("LowerPlayer")
                .scoreValue(1000.0)
                .submittedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        when(leaderboardRepository.findBySlug("test-leaderboard")).thenReturn(Optional.of(testLeaderboard));
        when(scoreRepository.findByLeaderboardIdAndPlayerAlias(anyLong(), anyString())).thenReturn(Optional.of(testScoreEntry));
        when(scoreRepository.countBetterScoresDesc(anyLong(), anyDouble(), any())).thenReturn(1L);
        when(scoreRepository.findHigherScores(anyLong(), anyDouble(), any(), any())).thenReturn(new SliceImpl<>(List.of(higherScore)));
        when(scoreRepository.findLowerScores(anyLong(), anyDouble(), any(), any())).thenReturn(new SliceImpl<>(List.of(lowerScore)));

        List<ScoreEntryResponse> result = scoreService.getPlayerScoreWithSurrounding("test-leaderboard", "TestPlayer", 2);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getPlayerAlias()).isEqualTo("HigherPlayer");
        assertThat(result.get(0).getRank()).isEqualTo(1);
        assertThat(result.get(1).getPlayerAlias()).isEqualTo("TestPlayer");
        assertThat(result.get(1).getRank()).isEqualTo(2);
        assertThat(result.get(2).getPlayerAlias()).isEqualTo("LowerPlayer");
        assertThat(result.get(2).getRank()).isEqualTo(3);
    }

    @Test
    void getPlayerScoreWithSurrounding_AscendingOrder_ReturnsCorrectOrder() {
        testLeaderboard.setSortOrder(SortOrder.ASC);
        ScoreEntry higherScore = ScoreEntry.builder()
                .id(2L)
                .leaderboard(testLeaderboard)
                .playerAlias("HigherPlayer")
                .scoreValue(2000.0)
                .submittedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        ScoreEntry lowerScore = ScoreEntry.builder()
                .id(3L)
                .leaderboard(testLeaderboard)
                .playerAlias("LowerPlayer")
                .scoreValue(1000.0)
                .submittedAt(LocalDateTime.now(ZoneOffset.UTC))
                .build();

        when(leaderboardRepository.findBySlug("test-leaderboard")).thenReturn(Optional.of(testLeaderboard));
        when(scoreRepository.findByLeaderboardIdAndPlayerAlias(anyLong(), anyString())).thenReturn(Optional.of(testScoreEntry));
        when(scoreRepository.countBetterScoresAsc(anyLong(), anyDouble(), any())).thenReturn(1L);
        when(scoreRepository.findHigherScores(anyLong(), anyDouble(), any(), any())).thenReturn(new SliceImpl<>(List.of(lowerScore)));
        when(scoreRepository.findLowerScores(anyLong(), anyDouble(), any(), any())).thenReturn(new SliceImpl<>(List.of(higherScore)));

        List<ScoreEntryResponse> result = scoreService.getPlayerScoreWithSurrounding("test-leaderboard", "TestPlayer", 2);

        assertThat(result).hasSize(3);
        assertThat(result.get(0).getPlayerAlias()).isEqualTo("LowerPlayer");
        assertThat(result.get(0).getRank()).isEqualTo(1);
        assertThat(result.get(1).getPlayerAlias()).isEqualTo("TestPlayer");
        assertThat(result.get(1).getRank()).isEqualTo(2);
        assertThat(result.get(2).getPlayerAlias()).isEqualTo("HigherPlayer");
        assertThat(result.get(2).getRank()).isEqualTo(3);
    }

    @Test
    void getAllScoresForLeaderboard_ValidLeaderboard_ReturnsPaginatedScores() {
        List<ScoreEntry> scoreEntries = List.of(testScoreEntry);
        Page<ScoreEntry> scorePage = new PageImpl<>(scoreEntries);

        when(leaderboardRepository.findBySlug("test-leaderboard")).thenReturn(Optional.of(testLeaderboard));
        when(scoreRepository.findByLeaderboardSlug(anyString(), any(Pageable.class))).thenReturn(scorePage);

        List<ScoreEntryResponse> result = scoreService.getAllScoresForLeaderboard("test-leaderboard", 0, 10);

        assertThat(result).hasSize(1);
        assertThat(result.getFirst().getPlayerAlias()).isEqualTo("TestPlayer");
        assertThat(result.getFirst().getRank()).isEqualTo(1);
    }

    @Test
    void getAllScoresForLeaderboard_LeaderboardNotFound_ThrowsLeaderboardNotFoundException() {
        when(leaderboardRepository.findBySlug("non-existent")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scoreService.getAllScoresForLeaderboard("non-existent", 0, 10))
                .isInstanceOf(LeaderboardNotFoundException.class);
    }

    @Test
    void getAllScoresForLeaderboard_LargeSize_LimitsTo1000() {
        when(leaderboardRepository.findBySlug("test-leaderboard")).thenReturn(Optional.of(testLeaderboard));
        when(scoreRepository.findByLeaderboardSlug(anyString(), any(Pageable.class))).thenReturn(new PageImpl<>(List.of()));

        scoreService.getAllScoresForLeaderboard("test-leaderboard", 0, 2000);

        verify(scoreRepository).findByLeaderboardSlug(eq("test-leaderboard"), any(Pageable.class));
    }

    @Test
    void removeScore_ValidScoreId_RemovesScoreAndEvictsCache() {
        when(scoreRepository.findById(1L)).thenReturn(Optional.of(testScoreEntry));

        scoreService.removeScore(1L);

        verify(scoreRepository).delete(testScoreEntry);
        verify(scoreCacheService).evictTopScoresCache("test-leaderboard");
    }

    @Test
    void removeScore_ScoreNotFound_ThrowsIllegalArgumentException() {
        when(scoreRepository.findById(999L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> scoreService.removeScore(999L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Score entry not found");
    }

    @Test
    void submitScore_EvictsCacheWhenRankIsUnder100() {
        when(leaderboardRepository.findBySlug("test-leaderboard")).thenReturn(Optional.of(testLeaderboard));
        when(playerService.isPlayerBanned("TestPlayer")).thenReturn(false);
        when(scoreRepository.updateIfHigherScore(anyLong(), anyString(), anyDouble(), any())).thenReturn(0);
        when(scoreRepository.findByLeaderboardIdAndPlayerAlias(anyLong(), anyString())).thenReturn(Optional.empty());
        when(scoreRepository.save(any(ScoreEntry.class))).thenReturn(testScoreEntry);
        when(scoreRepository.countBetterScoresDesc(anyLong(), anyDouble(), any())).thenReturn(50L);

        scoreService.submitScore("test-leaderboard", "TestPlayer", 1500.0, "Test metadata");

        verify(scoreCacheService).evictTopScoresCache("test-leaderboard");
    }

    @Test
    void submitScore_DoesNotEvictCacheWhenRankIsOver100() {
        when(leaderboardRepository.findBySlug("test-leaderboard")).thenReturn(Optional.of(testLeaderboard));
        when(playerService.isPlayerBanned("TestPlayer")).thenReturn(false);
        when(scoreRepository.updateIfHigherScore(anyLong(), anyString(), anyDouble(), any())).thenReturn(0);
        when(scoreRepository.findByLeaderboardIdAndPlayerAlias(anyLong(), anyString())).thenReturn(Optional.empty());
        when(scoreRepository.save(any(ScoreEntry.class))).thenReturn(testScoreEntry);
        when(scoreRepository.countBetterScoresDesc(anyLong(), anyDouble(), any())).thenReturn(150L);

        scoreService.submitScore("test-leaderboard", "TestPlayer", 1500.0, "Test metadata");

        verify(scoreCacheService, never()).evictTopScoresCache("test-leaderboard");
    }
}
