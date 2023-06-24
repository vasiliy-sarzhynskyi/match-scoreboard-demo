package com.vsarzhynskyi.scroreboard.demo.exception;

import static java.lang.String.format;

public class TeamAlreadyRegisteredException extends RuntimeException {

    public TeamAlreadyRegisteredException(String teamName) {
        super(format("team name '%s' already registered", teamName));
    }

}
