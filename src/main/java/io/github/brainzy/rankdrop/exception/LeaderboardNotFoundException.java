package io.github.brainzy.rankdrop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class LeaderboardNotFoundException extends RuntimeException {

    public LeaderboardNotFoundException(String slug) {
        super("Leaderboard with slug '" + slug + "' was not found.");
    }
}