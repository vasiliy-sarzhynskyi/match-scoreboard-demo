package com.vsarzhynskyi.scroreboard.demo.model;

import lombok.Builder;
import lombok.Value;

import static java.lang.String.format;

@Value
@Builder
public class MatchScoreboardPresentation {

    private static final String MATCH_PRESENTATION_TEMPLATE = "%d. %s %d - %s %d";

    int matchId;
    int matchScoreboardRank;
    Team homeTeam;
    int homeTeamScore;
    Team awayTeam;
    int awayTeamScore;

    @Override
    public String toString() {
        return format(MATCH_PRESENTATION_TEMPLATE, matchScoreboardRank, homeTeam.getName(), homeTeamScore, awayTeam.getName(), awayTeamScore);
    }

}
