package com.vsarzhynskyi.scroreboard.demo.service.match;

import com.vsarzhynskyi.scroreboard.demo.model.MatchDetails;
import com.vsarzhynskyi.scroreboard.demo.model.MatchScoreboardPresentation;
import com.vsarzhynskyi.scroreboard.demo.model.MatchesScoreboardSummary;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.IntStream;

public class DefaultScoreboardAwareMatchRegistrar implements ScoreboardAwareMatchRegistrar {

    private final MatchRegistrar matchRegistrar;
    private final Comparator<MatchDetails> scoreboardMatchComparator;
    private volatile MatchesScoreboardSummary calculatedScoreboardSummary;

    public DefaultScoreboardAwareMatchRegistrar(MatchRegistrar matchRegistrar,
                                                Comparator<MatchDetails> scoreboardMatchComparator) {
        this.matchRegistrar = matchRegistrar;
        this.scoreboardMatchComparator = scoreboardMatchComparator;
        calculatedScoreboardSummary = new MatchesScoreboardSummary(List.of());
    }

    public DefaultScoreboardAwareMatchRegistrar(MatchRegistrar matchRegistrar) {
        this(matchRegistrar, new DefaultScoreboardMatchComparator());
    }

    @Override
    public MatchDetails registerMatch(int homeTeamId, int awayTeamId) {
        return matchRegistrar.registerMatch(homeTeamId, awayTeamId);
    }

    @Override
    public MatchDetails registerMatch(String homeTeamName, String awayTeamName) {
        return matchRegistrar.registerMatch(homeTeamName, awayTeamName);
    }

    @Override
    public MatchDetails startMatch(int matchId) {
        var matchDetails = matchRegistrar.startMatch(matchId);
        updateMatchesScoreboardSummary();
        return matchDetails;
    }

    @Override
    public MatchDetails startMatch(int homeTeamId, int awayTeamId) {
        var matchDetails = matchRegistrar.startMatch(homeTeamId, awayTeamId);
        updateMatchesScoreboardSummary();
        return matchDetails;
    }

    @Override
    public MatchDetails startMatch(String homeTeamName, String awayTeamName) {
        var matchDetails = matchRegistrar.startMatch(homeTeamName, awayTeamName);
        updateMatchesScoreboardSummary();
        return matchDetails;
    }

    @Override
    public MatchDetails updateMatchScore(int matchId, int homeTeamScore, int awayTeamScore) {
        var matchDetails = matchRegistrar.updateMatchScore(matchId, homeTeamScore, awayTeamScore);
        updateMatchesScoreboardSummary();
        return matchDetails;
    }

    @Override
    public MatchDetails updateMatchScore(int homeTeamId, int homeTeamScore, int awayTeamId, int awayTeamScore) {
        var matchDetails = matchRegistrar.updateMatchScore(homeTeamId, homeTeamScore, awayTeamId, awayTeamScore);
        updateMatchesScoreboardSummary();
        return matchDetails;
    }

    @Override
    public MatchDetails updateMatchScore(String homeTeamName, int homeTeamScore, String awayTeamName, int awayTeamScore) {
        var matchDetails = matchRegistrar.updateMatchScore(homeTeamName, homeTeamScore, awayTeamName, awayTeamScore);
        updateMatchesScoreboardSummary();
        return matchDetails;
    }

    @Override
    public MatchDetails finishMatch(int matchId) {
        var matchDetails = matchRegistrar.finishMatch(matchId);
        updateMatchesScoreboardSummary();
        return matchDetails;
    }

    @Override
    public MatchDetails finishMatch(int homeTeamId, int awayTeamId) {
        var matchDetails = matchRegistrar.finishMatch(homeTeamId, awayTeamId);
        updateMatchesScoreboardSummary();
        return matchDetails;
    }

    @Override
    public MatchDetails finishMatch(String homeTeamName, String awayTeamName) {
        var matchDetails = matchRegistrar.finishMatch(homeTeamName, awayTeamName);
        updateMatchesScoreboardSummary();
        return matchDetails;
    }

    @Override
    public MatchDetails unregisterMatch(int matchId) {
        return matchRegistrar.unregisterMatch(matchId);
    }

    @Override
    public MatchDetails unregisterMatch(int homeTeamId, int awayTeamId) {
        return matchRegistrar.unregisterMatch(homeTeamId, awayTeamId);
    }

    @Override
    public MatchDetails unregisterMatch(String homeTeamName, String awayTeamName) {
        return matchRegistrar.unregisterMatch(homeTeamName, awayTeamName);
    }

    @Override
    public List<MatchDetails> getAllMatches() {
        return matchRegistrar.getAllMatches();
    }

    @Override
    public List<MatchDetails> getActiveMatches() {
        return matchRegistrar.getActiveMatches();
    }

    @Override
    public boolean isMatchRegistered(int matchId) {
        return matchRegistrar.isMatchRegistered(matchId);
    }

    @Override
    public boolean isMatchRegistered(int homeTeamId, int awayTeamId) {
        return matchRegistrar.isMatchRegistered(homeTeamId, awayTeamId);
    }

    @Override
    public boolean isMatchRegistered(String homeTeamName, String awayTeamName) {
        return matchRegistrar.isMatchRegistered(homeTeamName, awayTeamName);
    }

    @Override
    public MatchesScoreboardSummary getMatchesScoreboardSummary() {
        return calculatedScoreboardSummary;
    }

    private void updateMatchesScoreboardSummary() {
        var activeMatches = new ArrayList<>(getActiveMatches());
        activeMatches.sort(scoreboardMatchComparator);

        var matchScoreboardPresentations = IntStream.range(0, activeMatches.size())
                .mapToObj(order -> convertMatchDetailsIntoScoreboardPresentation(activeMatches.get(order), order + 1))
                .toList();
        calculatedScoreboardSummary = new MatchesScoreboardSummary(matchScoreboardPresentations);
    }

    private MatchScoreboardPresentation convertMatchDetailsIntoScoreboardPresentation(MatchDetails match, int matchRank) {
        return MatchScoreboardPresentation.builder()
                .matchId(match.getMatchId())
                .matchScoreboardRank(matchRank)
                .homeTeam(match.getHomeTeam())
                .homeTeamScore(match.getHomeTeamScore())
                .awayTeam(match.getAwayTeam())
                .awayTeamScore(match.getAwayTeamScore())
                .build();
    }

}
