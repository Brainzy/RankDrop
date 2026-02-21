package io.github.brainzy.rankdrop.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.FORBIDDEN)
public class PlayerBannedException extends RuntimeException {

    public PlayerBannedException(String playerAlias) {
        super("Player '" + playerAlias + "' was banned, but tried to submit a score.");
    }
}