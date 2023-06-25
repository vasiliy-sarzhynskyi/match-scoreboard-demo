package com.vsarzhynskyi.scroreboard.demo.exception;

import static java.lang.String.format;

public class TeamNameInvalidException extends RuntimeException {

    public TeamNameInvalidException(String teamName) {
        super(format("provided team name '%s' is invalid", teamName));
    }

}
