package com.vsarzhynskyi.scroreboard.demo.exception;

import static java.lang.String.format;

public class MatchNotRegisteredException extends RuntimeException {

    public MatchNotRegisteredException(int matchId) {
        super(format("match ID '%d' not registered", matchId));
    }

    public MatchNotRegisteredException(String homeTeamName, String awayTeamName) {
        super(format("match between teams '%s' and '%s' not registered", homeTeamName, awayTeamName));
    }

}
