package com.vsarzhynskyi.scroreboard.demo.service.match;

import com.vsarzhynskyi.scroreboard.demo.model.MatchesScoreboardSummary;

public interface ScoreboardAwareMatchRegistrar extends MatchRegistrar {

    MatchesScoreboardSummary getMatchesScoreboardSummary();

}
