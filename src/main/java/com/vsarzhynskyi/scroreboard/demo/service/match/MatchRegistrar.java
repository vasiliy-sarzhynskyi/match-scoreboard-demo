package com.vsarzhynskyi.scroreboard.demo.service.match;

import com.vsarzhynskyi.scroreboard.demo.model.MatchDetails;

import java.util.List;

public interface MatchRegistrar {

    MatchDetails registerMatch(int homeTeamId, int awayTeamId);
    MatchDetails registerMatch(String homeTeamName, String awayTeamName);
    MatchDetails startMatch(int matchId);
    MatchDetails startMatch(int homeTeamId, int awayTeamId);
    MatchDetails startMatch(String homeTeamName, String awayTeamName);
    MatchDetails updateMatchScore(int matchId, int homeTeamScore, int awayTeamScore);
    MatchDetails updateMatchScore(int homeTeamId, int homeTeamScore, int awayTeamId, int awayTeamScore);
    MatchDetails updateMatchScore(String homeTeamName, int homeTeamScore, String awayTeamName, int awayTeamScore);
    MatchDetails finishMatch(int matchId);
    MatchDetails finishMatch(int homeTeamId, int awayTeamId);
    MatchDetails finishMatch(String homeTeamName, String awayTeamName);
    MatchDetails unregisterMatch(int matchId);
    MatchDetails unregisterMatch(int homeTeamId, int awayTeamId);
    MatchDetails unregisterMatch(String homeTeamName, String awayTeamName);
    List<MatchDetails> getAllMatches();
    List<MatchDetails> getActiveMatches();
    boolean isMatchRegistered(int matchId);
    boolean isMatchRegistered(int homeTeamId, int awayTeamId);
    boolean isMatchRegistered(String homeTeamName, String awayTeamName);

}
