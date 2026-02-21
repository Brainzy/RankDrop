package io.github.brainzy.rankdrop.service;

import io.github.brainzy.rankdrop.entity.Player;
import io.github.brainzy.rankdrop.exception.PlayerNotFoundException;
import io.github.brainzy.rankdrop.repository.PlayerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PlayerService {

    private final PlayerRepository playerRepository;

    @Transactional
    public Player banPlayer(String playerAlias, String reason) {
        Player player = playerRepository.findByPlayerAlias(playerAlias)
                .orElseGet(() -> createPlayer(playerAlias));

        player.setBanned(true);
        player.setBannedAt(LocalDateTime.now(ZoneOffset.UTC));
        player.setBannedReason(reason);

        return playerRepository.save(player);
    }

    @Transactional
    public Player unbanPlayer(String playerAlias) {
        Player player = playerRepository.findByPlayerAlias(playerAlias).orElseThrow(() -> new PlayerNotFoundException(playerAlias));

        player.setBanned(false);
        player.setBannedAt(null);
        player.setBannedReason(null);

        return playerRepository.save(player);
    }

    @Transactional(readOnly = true)
    public boolean isPlayerBanned(String playerAlias) {
        return playerRepository.findByPlayerAlias(playerAlias)
                .map(Player::isBanned)
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public Player getPlayerByAlias(String playerAlias) {
        return playerRepository.findByPlayerAlias(playerAlias)
                .orElseThrow(() -> new PlayerNotFoundException(playerAlias));
    }

    @Transactional(readOnly = true)
    public List<Player> getAllBannedPlayers() {
        return playerRepository.findByBannedTrue();
    }

    private Player createPlayer(String playerAlias) {
        return Player.builder()
                .playerAlias(playerAlias)
                .banned(false)
                .build();
    }
}
