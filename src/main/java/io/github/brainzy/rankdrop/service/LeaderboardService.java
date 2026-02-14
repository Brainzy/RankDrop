package io.github.brainzy.rankdrop.service;

import io.github.brainzy.rankdrop.entity.Leaderboard;
import io.github.brainzy.rankdrop.exception.LeaderboardAlreadyExistsException;
import io.github.brainzy.rankdrop.exception.LeaderboardNotFoundException;
import io.github.brainzy.rankdrop.repository.LeaderboardRepository;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class LeaderboardService {
    private final LeaderboardRepository leaderboardRepository;

    public LeaderboardService(LeaderboardRepository leaderboardRepository) {
        this.leaderboardRepository = leaderboardRepository;
    }

    public Leaderboard createNewLeaderboard(String slug, String displayName) {
        if (leaderboardRepository.findBySlug(slug).isPresent()) {
            throw new LeaderboardAlreadyExistsException(slug);
        }
        Leaderboard b = new Leaderboard();
        b.setSlug(slug);
        b.setDisplayName(displayName != null ? displayName : slug);
        return leaderboardRepository.save(b);
    }

    public Leaderboard updateExistingLeaderboard(String slug, String displayName) {
        Leaderboard board = leaderboardRepository.findBySlug(slug)
                .orElseThrow(() -> new LeaderboardNotFoundException(slug));

        board.setDisplayName(displayName);
        return leaderboardRepository.save(board);
    }

    public void deleteLeaderboardBySlug(String slug) {
        Leaderboard board = leaderboardRepository.findBySlug(slug)
                .orElseThrow(() -> new LeaderboardNotFoundException(slug));
        leaderboardRepository.delete(board);
    }

    public List<Leaderboard> getAllLeaderboards() {
        return leaderboardRepository.findAll();
    }
}