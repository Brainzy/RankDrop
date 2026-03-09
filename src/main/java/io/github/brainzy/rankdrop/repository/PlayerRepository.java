package io.github.brainzy.rankdrop.repository;

import io.github.brainzy.rankdrop.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    @Query("SELECT p FROM Player p WHERE p.playerAlias = :playerAlias")
    Optional<Player> findByPlayerAlias(@Param("playerAlias") String playerAlias);

    @Query("SELECT p FROM Player p WHERE p.banned = true")
    List<Player> findByBannedTrue();
}
