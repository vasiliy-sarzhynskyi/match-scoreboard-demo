package com.vsarzhynskyi.scroreboard.demo.exception;

public class MatchInvalidUpdateException extends RuntimeException {

    public MatchInvalidUpdateException(String failureReason) {
        super(failureReason);
    }

}
