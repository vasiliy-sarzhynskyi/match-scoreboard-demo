package com.vsarzhynskyi.scroreboard.demo.service.match;

import com.vsarzhynskyi.scroreboard.demo.model.MatchDetails;

import java.util.Comparator;

public class DefaultScoreboardMatchComparator implements Comparator<MatchDetails> {

    @Override
    public int compare(MatchDetails match1, MatchDetails match2) {
        int deltaScores = getMatchTotalScore(match2) - getMatchTotalScore(match1);
        if (deltaScores != 0) {
            return deltaScores;
        }
        return match2.getMatchStartTimestamp().compareTo(match1.getMatchStartTimestamp());
    }

    private int getMatchTotalScore(MatchDetails matchDetails) {
        return matchDetails.getHomeTeamScore() + matchDetails.getAwayTeamScore();
    }

}
