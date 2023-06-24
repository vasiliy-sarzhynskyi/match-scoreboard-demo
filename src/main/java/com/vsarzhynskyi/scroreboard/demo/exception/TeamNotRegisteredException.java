package com.vsarzhynskyi.scroreboard.demo.exception;

import static java.lang.String.format;

public class TeamNotRegisteredException extends RuntimeException {

    public TeamNotRegisteredException(String teamName) {
        super(format("team name '%s' not registered", teamName));
    }

    public TeamNotRegisteredException(int teamId) {
        super(format("team ID '%d' not registered", teamId));
    }

}
