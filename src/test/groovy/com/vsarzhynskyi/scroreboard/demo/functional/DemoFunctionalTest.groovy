package com.vsarzhynskyi.scroreboard.demo.functional

import com.vsarzhynskyi.scroreboard.demo.service.match.DefaultMatchRegistrar
import com.vsarzhynskyi.scroreboard.demo.service.match.DefaultScoreboardAwareMatchRegistrar
import com.vsarzhynskyi.scroreboard.demo.service.match.MatchIdGenerator
import com.vsarzhynskyi.scroreboard.demo.service.team.DefaultTeamRegistrar
import com.vsarzhynskyi.scroreboard.demo.service.team.TeamIdGenerator
import spock.lang.Specification

import java.time.Clock
import java.time.ZoneOffset

class DemoFunctionalTest extends Specification {

    def 'end to end flow with multiple matches and scoreboard verification results'() {
        given:
        def teamIdGenerator = new TeamIdGenerator()
        def teamRegistrar = new DefaultTeamRegistrar(teamIdGenerator)
        def clock = Clock.system(ZoneOffset.UTC)
        def matchIdGenerator = new MatchIdGenerator()
        def matchRegistrar = new DefaultMatchRegistrar(matchIdGenerator, teamRegistrar, clock)
        def scoreboardAwareMatchRegistrar = new DefaultScoreboardAwareMatchRegistrar(matchRegistrar)

        when:
        teamRegistrar.registerTeam('Mexico')
        teamRegistrar.registerTeam('Canada')
        teamRegistrar.registerTeam('Spain')
        teamRegistrar.registerTeam('Brazil')
        teamRegistrar.registerTeam('Germany')
        teamRegistrar.registerTeam('France')

        def matchId1 = scoreboardAwareMatchRegistrar.registerMatch('Mexico', 'Canada').getMatchId()
        def matchId2 = scoreboardAwareMatchRegistrar.registerMatch('Spain', 'Brazil').getMatchId()
        def matchId3 = scoreboardAwareMatchRegistrar.registerMatch('Germany', 'France').getMatchId()

        then:
        teamRegistrar.getAllTeams().size() == 6
        scoreboardAwareMatchRegistrar.getAllMatches().size() == 3

        when:
        scoreboardAwareMatchRegistrar.startMatch(matchId1)
        scoreboardAwareMatchRegistrar.startMatch(matchId2)
        def scoreboardSummary = scoreboardAwareMatchRegistrar.getMatchesScoreboardSummary()

        then:
        println '\n' + scoreboardSummary

        scoreboardSummary.getMatches().size() == 2
        scoreboardSummary.getMatches().get(0).getMatchId() == matchId2
        scoreboardSummary.getMatches().get(1).getMatchId() == matchId1

        when:
        scoreboardAwareMatchRegistrar.updateMatchScore(matchId1, 0, 3)
        scoreboardAwareMatchRegistrar.startMatch(matchId3)
        scoreboardSummary = scoreboardAwareMatchRegistrar.getMatchesScoreboardSummary()

        then:
        println '\n' + scoreboardSummary
        scoreboardSummary.getMatches().size() == 3
        scoreboardSummary.getMatches().get(0).getMatchId() == matchId1
        scoreboardSummary.getMatches().get(1).getMatchId() == matchId3
        scoreboardSummary.getMatches().get(2).getMatchId() == matchId2

        when:
        teamRegistrar.registerTeam('Uruguay')
        teamRegistrar.registerTeam('Italy')
        teamRegistrar.registerTeam('Argentina')
        teamRegistrar.registerTeam('Australia')
        def matchId4 = scoreboardAwareMatchRegistrar.registerMatch('Uruguay', 'Italy').getMatchId()
        def matchId5 = scoreboardAwareMatchRegistrar.registerMatch('Argentina', 'Australia').getMatchId()
        scoreboardAwareMatchRegistrar.startMatch(matchId4)
        scoreboardAwareMatchRegistrar.startMatch(matchId5)

        and:
        scoreboardAwareMatchRegistrar.updateMatchScore(matchId2, 4, 0)
        scoreboardAwareMatchRegistrar.updateMatchScore(matchId2, 4, 1)
        scoreboardAwareMatchRegistrar.updateMatchScore(matchId1, 0, 5)
        scoreboardSummary = scoreboardAwareMatchRegistrar.getMatchesScoreboardSummary()

        then:
        println '\n' + scoreboardSummary
        scoreboardSummary.getMatches().size() == 5
        scoreboardSummary.getMatches().get(0).getMatchId() == matchId2
        scoreboardSummary.getMatches().get(1).getMatchId() == matchId1
        scoreboardSummary.getMatches().get(2).getMatchId() == matchId5
        scoreboardSummary.getMatches().get(3).getMatchId() == matchId4
        scoreboardSummary.getMatches().get(4).getMatchId() == matchId3

        when:
        scoreboardAwareMatchRegistrar.updateMatchScore(matchId2, 10, 2)
        scoreboardAwareMatchRegistrar.updateMatchScore(matchId3, 2, 2)
        scoreboardAwareMatchRegistrar.updateMatchScore(matchId4, 6, 6)
        scoreboardAwareMatchRegistrar.updateMatchScore(matchId5, 3, 1)
        scoreboardSummary = scoreboardAwareMatchRegistrar.getMatchesScoreboardSummary()

        then:
        println '\n' + scoreboardSummary
        scoreboardSummary.getMatches().size() == 5
        scoreboardSummary.getMatches().get(0).getMatchId() == matchId4
        scoreboardSummary.getMatches().get(1).getMatchId() == matchId2
        scoreboardSummary.getMatches().get(2).getMatchId() == matchId1
        scoreboardSummary.getMatches().get(3).getMatchId() == matchId5
        scoreboardSummary.getMatches().get(4).getMatchId() == matchId3

        when:
        scoreboardAwareMatchRegistrar.finishMatch(matchId1)
        scoreboardAwareMatchRegistrar.finishMatch(matchId2)
        scoreboardAwareMatchRegistrar.unregisterMatch(matchId1)
        scoreboardSummary = scoreboardAwareMatchRegistrar.getMatchesScoreboardSummary()

        then:
        println '\n' + scoreboardSummary
        scoreboardSummary.getMatches().size() == 3
        scoreboardSummary.getMatches().get(0).getMatchId() == matchId4
        scoreboardSummary.getMatches().get(1).getMatchId() == matchId5
        scoreboardSummary.getMatches().get(2).getMatchId() == matchId3

        when:
        scoreboardAwareMatchRegistrar.unregisterMatch(matchId2)
        scoreboardAwareMatchRegistrar.finishMatch(matchId3)
        scoreboardAwareMatchRegistrar.finishMatch(matchId4)
        scoreboardSummary = scoreboardAwareMatchRegistrar.getMatchesScoreboardSummary()

        then:
        println '\n' + scoreboardSummary
        scoreboardSummary.getMatches().size() == 1
        scoreboardSummary.getMatches().get(0).getMatchId() == matchId5

        when:
        scoreboardAwareMatchRegistrar.finishMatch(matchId5)
        scoreboardAwareMatchRegistrar.unregisterMatch(matchId5)
        scoreboardAwareMatchRegistrar.unregisterMatch(matchId3)
        scoreboardAwareMatchRegistrar.unregisterMatch(matchId4)
        scoreboardSummary = scoreboardAwareMatchRegistrar.getMatchesScoreboardSummary()

        then:
        println '\n' + scoreboardSummary
        scoreboardSummary.getMatches().isEmpty()
    }

}
