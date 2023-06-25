package com.vsarzhynskyi.scroreboard.demo.model;

import lombok.Value;

import java.util.List;

@Value
public class MatchesScoreboardSummary {
    List<MatchScoreboardPresentation> matches;

    @Override
    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        for (var match: matches) {
            stringBuilder.append(match).append('\n');
        }
        if (stringBuilder.length() != 0) {
            stringBuilder.deleteCharAt(stringBuilder.length() - 1);
        }
        return stringBuilder.toString();
    }
}
