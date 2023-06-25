package com.vsarzhynskyi.scroreboard.demo.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder(toBuilder = true)
public class MatchDetails {
    int matchId;
    Team homeTeam;
    int homeTeamScore;
    Team awayTeam;
    int awayTeamScore;
    MatchStatus matchStatus;
    Instant matchStartTimestamp;
    Instant lastUpdatedTimestamp;
}
