package com.vsarzhynskyi.scroreboard.demo.service;

import com.vsarzhynskyi.scroreboard.demo.model.MatchDetails;

import java.util.List;

public interface MatchRegistrar {

    MatchDetails registerMatch(String homeTeamName, String awayTeamName);
    MatchDetails startMatch(int matchId);
    MatchDetails updateMatchScore(int matchId, int homeTeamScore, int awayTeamScore);
    MatchDetails finishMatch(int matchId);
    MatchDetails unregisterMatch(int matchId);
    List<MatchDetails> getAllMatches();
    List<MatchDetails> getActiveMatches();
    List<MatchDetails> getMatchesScoreboard();

}
