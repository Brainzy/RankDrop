package io.github.brainzy.rankdrop;

import io.github.brainzy.rankdrop.entity.Leaderboard;
import io.github.brainzy.rankdrop.entity.ScoreStrategy;
import io.github.brainzy.rankdrop.entity.SortOrder;
import io.github.brainzy.rankdrop.entity.SystemSetting;
import io.github.brainzy.rankdrop.repository.LeaderboardRepository;
import io.github.brainzy.rankdrop.repository.SystemSettingRepository;
import io.github.brainzy.rankdrop.service.PlayerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = RankDropApplication.class)
@Testcontainers
class LeaderboardIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("rankdrop_test")
            .withUsername("rankdrop")
            .withPassword("rankdrop");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
    }

    @Autowired
    private WebApplicationContext context;

    private MockMvc mockMvc;
    @Autowired
    private LeaderboardRepository leaderboardRepository;
    @Autowired
    private SystemSettingRepository systemSettingRepository;
    @Autowired
    private PlayerService playerService;

    private static final String SLUG = "integration-test-leaderboard";
    private static final String BASE = "/api/v1/leaderboards/" + SLUG;
    private static final String GAME_KEY = "test-game-key-integration";

    @BeforeEach
    void setUp() {
        // Ensure interceptor passes with a known key
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        SystemSetting gameKey = new SystemSetting();
        gameKey.setKey("GAME_SECRET");
        gameKey.setValue(GAME_KEY);
        systemSettingRepository.save(gameKey);

        // Clean leaderboard state before each test
        leaderboardRepository.findBySlug(SLUG).ifPresent(leaderboardRepository::delete);

        leaderboardRepository.save(Leaderboard.builder()
                .slug(SLUG)
                .displayName("Integration Test Leaderboard")
                .sortOrder(SortOrder.DESC)
                .scoreStrategy(ScoreStrategy.BEST_ONLY)
                .minScore(0.0)
                .maxScore(1_000_000.0)
                .build());
    }

    @Test
    void submitScore_ValidScore_ReturnsRankAndScore() throws Exception {
        mockMvc.perform(post(BASE + "/scores")
                        .header("X-Game-Key", GAME_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "playerAlias": "Alice",
                                  "scoreValue": 5000,
                                  "metadata": "first run"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(5000.0))
                .andExpect(jsonPath("$.rank").value(1));
    }

    @Test
    void submitScore_ScoreBelowMinimum_Returns400() throws Exception {
        mockMvc.perform(post(BASE + "/scores")
                        .header("X-Game-Key", GAME_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "playerAlias": "Alice",
                                  "scoreValue": -1
                                }
                                """))
                .andExpect(status().isBadRequest());
    }

    // ── 2. GET TOP SCORES ─────────────────────────────────────────────────

    @Test
    void getTopScores_MultiplePlayersSubmitted_ReturnsInRankOrder() throws Exception {
        submitScoreDirect("Alice", 5000);
        submitScoreDirect("Bob", 8000);
        submitScoreDirect("Charlie", 3000);

        mockMvc.perform(get(BASE + "/top")
                        .header("X-Game-Key", GAME_KEY)
                        .param("limit", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scores.length()").value(3))
                .andExpect(jsonPath("$.scores[0].n").value("Bob"))
                .andExpect(jsonPath("$.scores[1].n").value("Alice"))
                .andExpect(jsonPath("$.scores[2].n").value("Charlie"));
    }

    @Test
    void getTopScores_LimitRespected_ReturnsOnlyRequestedCount() throws Exception {
        submitScoreDirect("Alice", 5000);
        submitScoreDirect("Bob", 8000);
        submitScoreDirect("Charlie", 3000);

        mockMvc.perform(get(BASE + "/top")
                        .header("X-Game-Key", GAME_KEY)
                        .param("limit", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.scores.length()").value(2))
                .andExpect(jsonPath("$.scores[0].n").value("Bob"))
                .andExpect(jsonPath("$.scores[1].n").value("Alice"));
    }

    // ── 3. PLAYER RANK WITH SURROUNDING ──────────────────────────────────

    @Test
    void getPlayerScore_WithSurrounding_ReturnsCorrectNeighbours() throws Exception {
        submitScoreDirect("Alice", 9000); // rank 1
        submitScoreDirect("Bob", 7000); // rank 2
        submitScoreDirect("Charlie", 5000); // rank 3
        submitScoreDirect("Dave", 3000); // rank 4
        submitScoreDirect("Eve", 1000); // rank 5

        mockMvc.perform(get(BASE + "/players/Charlie")
                        .header("X-Game-Key", GAME_KEY)
                        .param("surrounding", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.startRank").value(2))
                .andExpect(jsonPath("$.scores.length()").value(3))
                .andExpect(jsonPath("$.scores[0].n").value("Bob"))
                .andExpect(jsonPath("$.scores[1].n").value("Charlie"))
                .andExpect(jsonPath("$.scores[2].n").value("Dave"));
    }

    @Test
    void getPlayerScore_PlayerNotFound_Returns404() throws Exception {
        mockMvc.perform(get(BASE + "/players/NonExistent")
                        .header("X-Game-Key", GAME_KEY))
                .andExpect(status().isNotFound());
    }

    // ── 4. COMBINED ENDPOINT ─────────────────────────────────────────────

    @Test
    void getCombined_ValidRequest_ReturnsTopScoresAndPlayerContext() throws Exception {
        submitScoreDirect("Alice", 9000);
        submitScoreDirect("Bob", 7000);
        submitScoreDirect("Charlie", 5000);

        mockMvc.perform(get(BASE + "/combined")
                        .header("X-Game-Key", GAME_KEY)
                        .param("topLimit", "10")
                        .param("playerAlias", "Charlie")
                        .param("surrounding", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.topScores.length()").value(3))
                .andExpect(jsonPath("$.topScores[0].n").value("Alice"))
                .andExpect(jsonPath("$.playerScore.startRank").value(2))
                .andExpect(jsonPath("$.topScores[1].n").value("Bob"));
    }

    // ── 5. DUPLICATE PLAYER UPSERT ───────────────────────────────────────

    @Test
    void submitScore_DuplicatePlayer_BestOnlyKeepsHigherScore() throws Exception {
        submitScoreDirect("Alice", 5000);

        // Lower score — should be ignored, response returns existing best
        mockMvc.perform(post(BASE + "/scores")
                        .header("X-Game-Key", GAME_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "playerAlias": "Alice",
                                  "scoreValue": 3000
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(5000.0))
                .andExpect(jsonPath("$.rank").value(1));

        // Only one entry on the board, still the higher score
        mockMvc.perform(get(BASE + "/top").header("X-Game-Key", GAME_KEY))
                .andExpect(jsonPath("$.scores.length()").value(1))
                .andExpect(jsonPath("$.scores[0].s").value(5000.0));
    }

    @Test
    void submitScore_DuplicatePlayer_NewHighScoreMovesUpRank() throws Exception {
        submitScoreDirect("Alice", 3000);
        submitScoreDirect("Bob", 5000);

        mockMvc.perform(post(BASE + "/scores")
                        .header("X-Game-Key", GAME_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "playerAlias": "Alice",
                                  "scoreValue": 8000
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.score").value(8000.0))
                .andExpect(jsonPath("$.rank").value(1));
    }

    // ── 6. BANNED PLAYER ─────────────────────────────────────────────────

    @Test
    void submitScore_BannedPlayer_Returns403() throws Exception {
        playerService.banPlayer("BannedPlayer", "Hacking");

        mockMvc.perform(post(BASE + "/scores")
                        .header("X-Game-Key", GAME_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "playerAlias": "BannedPlayer",
                                  "scoreValue": 9999
                                }
                                """))
                .andExpect(status().isForbidden());
    }

    @Test
    void submitScore_UnbannedPlayer_CanSubmitAgain() throws Exception {
        playerService.banPlayer("TempBanned", "Hacking");
        playerService.unbanPlayer("TempBanned");

        mockMvc.perform(post(BASE + "/scores")
                        .header("X-Game-Key", GAME_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "playerAlias": "TempBanned",
                                  "scoreValue": 1000
                                }
                                """))
                .andExpect(status().isOk());
    }

    // ── HELPER ───────────────────────────────────────────────────────────

    private void submitScoreDirect(String playerAlias, double score) throws Exception {
        mockMvc.perform(post(BASE + "/scores")
                        .header("X-Game-Key", GAME_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(String.format("""
                                {
                                  "playerAlias": "%s",
                                  "scoreValue": %s
                                }
                                """, playerAlias, score)))
                .andExpect(status().isOk());
    }
}