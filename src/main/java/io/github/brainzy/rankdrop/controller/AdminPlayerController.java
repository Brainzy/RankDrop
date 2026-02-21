package io.github.brainzy.rankdrop.controller;

import io.github.brainzy.rankdrop.entity.Player;
import io.github.brainzy.rankdrop.service.PlayerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/admin")
@Tag(name = "Admin — Players", description = "Player ban management")
public class AdminPlayerController {

    private final PlayerService playerService;

    public AdminPlayerController(PlayerService playerService) {
        this.playerService = playerService;
    }

    @PostMapping("/players/{playerAlias}/ban")
    @Operation(summary = "Ban a player globally", description = "Bans a player from submitting scores to all leaderboards.")
    @ApiResponse(responseCode = "200", description = "Player banned successfully")
    public ResponseEntity<Player> banPlayer(
            @Parameter(description = "The player's alias", example = "PlayerOne")
            @PathVariable String playerAlias,
            @Parameter(description = "Reason for banning the player", example = "Cheating detected")
            @RequestParam(required = false) String reason
    ) {
        Player bannedPlayer = playerService.banPlayer(playerAlias, reason);
        return ResponseEntity.ok(bannedPlayer);
    }

    @DeleteMapping("/players/{playerAlias}/ban")
    @Operation(summary = "Unban a player globally", description = "Removes a ban from a player, allowing them to submit scores again.")
    @ApiResponse(responseCode = "200", description = "Player unbanned successfully")
    @ApiResponse(responseCode = "404", description = "Player not found", content = @Content(schema = @Schema(hidden = true)))
    public ResponseEntity<Player> unbanPlayer(
            @Parameter(description = "The player's alias", example = "PlayerOne")
            @PathVariable String playerAlias) {
        Player unbannedPlayer = playerService.unbanPlayer(playerAlias);
        return ResponseEntity.ok(unbannedPlayer);
    }

    @GetMapping("/players/banned")
    @Operation(summary = "List all banned players", description = "Returns a list of all currently banned players.")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved list of banned players")
    public List<Player> getAllBannedPlayers() {
        return playerService.getAllBannedPlayers();
    }
}
