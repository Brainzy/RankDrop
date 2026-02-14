package io.github.brainzy.rankdrop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class LeaderboardAlreadyExistsException extends RuntimeException {

    public LeaderboardAlreadyExistsException(String slug) {
        super("Leaderboard with slug '" + slug + "' already exists.");
    }
}