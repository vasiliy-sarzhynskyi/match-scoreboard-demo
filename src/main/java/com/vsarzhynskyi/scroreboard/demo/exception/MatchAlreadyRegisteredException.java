package com.vsarzhynskyi.scroreboard.demo.exception;

import static java.lang.String.format;

public class MatchAlreadyRegisteredException extends RuntimeException {

    public MatchAlreadyRegisteredException(String homeTeamName, String awayTeamName) {
        super(format("match between teams '%s' and '%s' already registered", homeTeamName, awayTeamName));
    }

}
