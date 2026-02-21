package io.github.brainzy.rankdrop.repository;

import io.github.brainzy.rankdrop.entity.Player;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PlayerRepository extends JpaRepository<Player, Long> {
    
    Optional<Player> findByPlayerAlias(String playerAlias);
    
    boolean existsByPlayerAlias(String playerAlias);
}
