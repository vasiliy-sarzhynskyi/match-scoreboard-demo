package com.vsarzhynskyi.scroreboard.demo.service.match

import com.vsarzhynskyi.scroreboard.demo.model.MatchDetails
import com.vsarzhynskyi.scroreboard.demo.model.MatchStatus
import com.vsarzhynskyi.scroreboard.demo.model.Team
import spock.lang.Specification
import spock.lang.Unroll

import java.time.Instant

class DefaultScoreboardAwareMatchRegistrarTest extends Specification {

    private static final FIXED_EPOCH_MILLI = 14900000000L
    private static final TEAM_ID_1 = 1
    private static final TEAM_NAME_1 = 'United Kingdom'
    private static final TEAM_ID_2 = 2
    private static final TEAM_NAME_2 = 'Poland'
    private static final MATCH_ID = 123

    def matchRegistrar = Mock(MatchRegistrar)

    def 'should delegate register match to core match registrar by team IDs'() {
        given:
        def scoreboardAwareMatchRegistrar = new DefaultScoreboardAwareMatchRegistrar(matchRegistrar)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)
        def registeredMatch = MatchDetails.builder()
                .matchId(MATCH_ID)
                .homeTeam(team1)
                .awayTeam(team2)
                .matchStatus(MatchStatus.REGISTERED)
                .lastUpdatedTimestamp(Instant.ofEpochMilli(FIXED_EPOCH_MILLI))
                .build()

        when:
        def resultRegisteredMatch = scoreboardAwareMatchRegistrar.registerMatch(TEAM_ID_1, TEAM_ID_2)

        then:
        1 * matchRegistrar.registerMatch(TEAM_ID_1, TEAM_ID_2) >> registeredMatch
        0 * _

        and:
        resultRegisteredMatch == registeredMatch
    }

    def 'should delegate register match to core match registrar by team names'() {
        given:
        def scoreboardAwareMatchRegistrar = new DefaultScoreboardAwareMatchRegistrar(matchRegistrar)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)
        def registeredMatch = MatchDetails.builder()
                .matchId(MATCH_ID)
                .homeTeam(team1)
                .awayTeam(team2)
                .matchStatus(MatchStatus.REGISTERED)
                .lastUpdatedTimestamp(Instant.ofEpochMilli(FIXED_EPOCH_MILLI))
                .build()

        when:
        def resultRegisteredMatch = scoreboardAwareMatchRegistrar.registerMatch(TEAM_NAME_1, TEAM_NAME_2)

        then:
        1 * matchRegistrar.registerMatch(TEAM_NAME_1, TEAM_NAME_2) >> registeredMatch
        0 * _

        and:
        resultRegisteredMatch == registeredMatch
    }

    def 'should start match by team IDs and in result scoreboard updated'() {
        given:
        def scoreboardAwareMatchRegistrar = new DefaultScoreboardAwareMatchRegistrar(matchRegistrar)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)
        def registeredMatchDetails = MatchDetails.builder()
                .matchId(MATCH_ID)
                .homeTeam(team1)
                .awayTeam(team2)
                .matchStatus(MatchStatus.REGISTERED)
                .build()
        def startedMatchDetails = registeredMatchDetails.toBuilder()
                .matchStatus(MatchStatus.IN_PROGRESS)
                .build()

        when:
        scoreboardAwareMatchRegistrar.registerMatch(TEAM_NAME_1, TEAM_NAME_2)
        def startedMatchResult = scoreboardAwareMatchRegistrar.startMatch(MATCH_ID)

        then:
        1 * matchRegistrar.registerMatch(TEAM_NAME_1, TEAM_NAME_2) >> registeredMatchDetails
        1 * matchRegistrar.startMatch(MATCH_ID) >> startedMatchDetails
        1 * matchRegistrar.getActiveMatches() >> List.of(startedMatchDetails)
        0 * _

        and:
        startedMatchResult == startedMatchDetails

        when:
        def scoreboardSummary = scoreboardAwareMatchRegistrar.getMatchesScoreboardSummary()

        then:
        0 * _

        and:
        scoreboardSummary.getMatches().size() == 1
        def matchScoreboardPresentation = scoreboardSummary.getMatches().get(0)
        matchScoreboardPresentation.getMatchId() == MATCH_ID
        matchScoreboardPresentation.getMatchScoreboardRank() == 1
        matchScoreboardPresentation.getHomeTeam().getId() == TEAM_ID_1
        matchScoreboardPresentation.getHomeTeamScore() == 0
        matchScoreboardPresentation.getAwayTeam().getId() == TEAM_ID_2
        matchScoreboardPresentation.getAwayTeamScore() == 0
    }

    def 'should start match by team IDs and team names and in result scoreboard updated'() {
        given:
        def scoreboardAwareMatchRegistrar = new DefaultScoreboardAwareMatchRegistrar(matchRegistrar)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)
        def teamId3 = 7
        def teamId4 = 8
        def matchId2 = 189
        def team3 = new Team(teamId3, 'Canada')
        def team4 = new Team(teamId4, 'Netherlands')
        def registeredMatchDetails1 = MatchDetails.builder()
                .matchId(MATCH_ID)
                .homeTeam(team1)
                .awayTeam(team2)
                .matchStatus(MatchStatus.REGISTERED)
                .build()
        def registeredMatchDetails2 = MatchDetails.builder()
                .matchId(matchId2)
                .homeTeam(team3)
                .awayTeam(team4)
                .matchStatus(MatchStatus.REGISTERED)
                .build()
        def startedMatchDetails1 = registeredMatchDetails1.toBuilder()
                .matchStatus(MatchStatus.IN_PROGRESS)
                .matchStartTimestamp(Instant.ofEpochMilli(FIXED_EPOCH_MILLI + 10))
                .build()
        def startedMatchDetails2 = registeredMatchDetails2.toBuilder()
                .matchStatus(MatchStatus.IN_PROGRESS)
                .matchStartTimestamp(Instant.ofEpochMilli(FIXED_EPOCH_MILLI + 20))
                .build()

        when:
        scoreboardAwareMatchRegistrar.registerMatch(TEAM_NAME_1, TEAM_NAME_2)
        scoreboardAwareMatchRegistrar.registerMatch(teamId3, teamId4)
        def startedMatchResult1 = scoreboardAwareMatchRegistrar.startMatch(TEAM_NAME_1, TEAM_NAME_2)
        def startedMatchResult2 = scoreboardAwareMatchRegistrar.startMatch(teamId3, teamId4)

        then:
        1 * matchRegistrar.registerMatch(TEAM_NAME_1, TEAM_NAME_2) >> registeredMatchDetails1
        1 * matchRegistrar.registerMatch(teamId3, teamId4) >> registeredMatchDetails2
        1 * matchRegistrar.startMatch(TEAM_NAME_1, TEAM_NAME_2) >> startedMatchDetails1
        1 * matchRegistrar.startMatch(teamId3, teamId4) >> startedMatchDetails2
        2 * matchRegistrar.getActiveMatches() >> List.of(startedMatchDetails1, startedMatchDetails2)
        0 * _

        and:
        startedMatchResult1 == startedMatchDetails1
        startedMatchResult2 == startedMatchDetails2

        when:
        def scoreboardSummary = scoreboardAwareMatchRegistrar.getMatchesScoreboardSummary()

        then:
        0 * _

        and:
        scoreboardSummary.getMatches().size() == 2
        scoreboardSummary.getMatches().get(0).getMatchId() == matchId2
        scoreboardSummary.getMatches().get(1).getMatchId() == MATCH_ID
    }

    def 'should update match by match ID and in result scoreboard updated'() {
        given:
        def scoreboardAwareMatchRegistrar = new DefaultScoreboardAwareMatchRegistrar(matchRegistrar)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)
        def homeTeamScore = 2
        def awayTeamScore = 3
        def matchDetails = MatchDetails.builder()
                .matchId(MATCH_ID)
                .homeTeam(team1)
                .homeTeamScore(homeTeamScore)
                .awayTeam(team2)
                .awayTeamScore(awayTeamScore)
                .matchStatus(MatchStatus.IN_PROGRESS)
                .build()

        when:
        def updatedMatchDetails = scoreboardAwareMatchRegistrar.updateMatchScore(MATCH_ID, homeTeamScore, awayTeamScore)

        then:
        1 * matchRegistrar.updateMatchScore(MATCH_ID, homeTeamScore, awayTeamScore) >> matchDetails
        1 * matchRegistrar.getActiveMatches() >> List.of(matchDetails)
        0 * _

        when:
        def scoreboardSummary = scoreboardAwareMatchRegistrar.getMatchesScoreboardSummary()

        then:
        0 * _

        and:
        updatedMatchDetails == matchDetails
        scoreboardSummary.getMatches().size() == 1
        def matchScoreboardPresentation = scoreboardSummary.getMatches().get(0)
        matchScoreboardPresentation.getMatchId() == MATCH_ID
        matchScoreboardPresentation.getMatchScoreboardRank() == 1
        matchScoreboardPresentation.getHomeTeam().getId() == TEAM_ID_1
        matchScoreboardPresentation.getHomeTeamScore() == 2
        matchScoreboardPresentation.getAwayTeam().getId() == TEAM_ID_2
        matchScoreboardPresentation.getAwayTeamScore() == 3
    }

    def 'should update match by team IDs and team names and in result scoreboard updated'() {
        given:
        def scoreboardAwareMatchRegistrar = new DefaultScoreboardAwareMatchRegistrar(matchRegistrar)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)
        def homeTeamScore = 2
        def awayTeamScore1 = 3
        def awayTeamScore2 = 4
        def matchDetails1 = MatchDetails.builder()
                .matchId(MATCH_ID)
                .homeTeam(team1)
                .homeTeamScore(homeTeamScore)
                .awayTeam(team2)
                .awayTeamScore(awayTeamScore1)
                .matchStatus(MatchStatus.IN_PROGRESS)
                .build()
        def matchDetails2 = MatchDetails.builder()
                .matchId(MATCH_ID)
                .homeTeam(team1)
                .homeTeamScore(homeTeamScore)
                .awayTeam(team2)
                .awayTeamScore(awayTeamScore2)
                .matchStatus(MatchStatus.IN_PROGRESS)
                .build()

        when:
        def updatedMatchDetails1 = scoreboardAwareMatchRegistrar.updateMatchScore(TEAM_ID_1, homeTeamScore, TEAM_ID_2, awayTeamScore1)
        def updatedMatchDetails2 = scoreboardAwareMatchRegistrar.updateMatchScore(TEAM_NAME_1, homeTeamScore, TEAM_NAME_2, awayTeamScore2)

        then:
        1 * matchRegistrar.updateMatchScore(TEAM_ID_1, homeTeamScore, TEAM_ID_2, awayTeamScore1) >> matchDetails1
        1 * matchRegistrar.updateMatchScore(TEAM_NAME_1, homeTeamScore, TEAM_NAME_2, awayTeamScore2) >> matchDetails2
        1 * matchRegistrar.getActiveMatches() >> List.of(matchDetails1)
        1 * matchRegistrar.getActiveMatches() >> List.of(matchDetails2)
        0 * _

        and:
        updatedMatchDetails1 == matchDetails1
        updatedMatchDetails2 == matchDetails2

        when:
        def scoreboardSummary = scoreboardAwareMatchRegistrar.getMatchesScoreboardSummary()

        then:
        0 * _

        and:
        scoreboardSummary.getMatches().size() == 1
        scoreboardSummary.getMatches().get(0).getMatchId() == MATCH_ID
    }

    def 'should finish match by match ID and in result scoreboard updated'() {
        given:
        def scoreboardAwareMatchRegistrar = new DefaultScoreboardAwareMatchRegistrar(matchRegistrar)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)
        def matchDetails = MatchDetails.builder()
                .matchId(MATCH_ID)
                .homeTeam(team1)
                .awayTeam(team2)
                .matchStatus(MatchStatus.FINISHED)
                .build()

        when:
        def finishedMatchDetails = scoreboardAwareMatchRegistrar.finishMatch(MATCH_ID)

        then:
        1 * matchRegistrar.finishMatch(MATCH_ID) >> matchDetails
        1 * matchRegistrar.getActiveMatches() >> List.of()
        0 * _

        and:
        finishedMatchDetails == matchDetails

        when:
        def scoreboardSummary = scoreboardAwareMatchRegistrar.getMatchesScoreboardSummary()

        then:
        0 * _

        and:
        scoreboardSummary.getMatches().isEmpty()
    }

    def 'should finish match by team IDs and team names, and in result scoreboard updated'() {
        given:
        def scoreboardAwareMatchRegistrar = new DefaultScoreboardAwareMatchRegistrar(matchRegistrar)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)
        def matchDetails = MatchDetails.builder()
                .matchId(MATCH_ID)
                .homeTeam(team1)
                .awayTeam(team2)
                .matchStatus(MatchStatus.FINISHED)
                .build()

        when:
        def finishedMatchDetails1 = scoreboardAwareMatchRegistrar.finishMatch(TEAM_ID_1, TEAM_ID_2)
        def finishedMatchDetails2 = scoreboardAwareMatchRegistrar.finishMatch(TEAM_NAME_1, TEAM_NAME_2)

        then:
        1 * matchRegistrar.finishMatch(TEAM_ID_1, TEAM_ID_2) >> matchDetails
        1 * matchRegistrar.finishMatch(TEAM_NAME_1, TEAM_NAME_2) >> matchDetails
        2 * matchRegistrar.getActiveMatches() >> List.of()
        0 * _

        and:
        finishedMatchDetails1 == matchDetails
        finishedMatchDetails2 == matchDetails

        when:
        def scoreboardSummary = scoreboardAwareMatchRegistrar.getMatchesScoreboardSummary()

        then:
        0 * _

        and:
        scoreboardSummary.getMatches().isEmpty()
    }

    def 'should unregister match by match ID'() {
        given:
        def scoreboardAwareMatchRegistrar = new DefaultScoreboardAwareMatchRegistrar(matchRegistrar)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)
        def matchDetails = MatchDetails.builder()
                .matchId(MATCH_ID)
                .homeTeam(team1)
                .awayTeam(team2)
                .matchStatus(MatchStatus.UNREGISTERED)
                .build()

        when:
        def unregisteredMatchDetails = scoreboardAwareMatchRegistrar.unregisterMatch(MATCH_ID)

        then:
        1 * matchRegistrar.unregisterMatch(MATCH_ID) >> matchDetails
        0 * _

        and:
        unregisteredMatchDetails == matchDetails
    }

    def 'should unregister match by team IDs and team names'() {
        given:
        def scoreboardAwareMatchRegistrar = new DefaultScoreboardAwareMatchRegistrar(matchRegistrar)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)
        def matchDetails = MatchDetails.builder()
                .matchId(MATCH_ID)
                .homeTeam(team1)
                .awayTeam(team2)
                .matchStatus(MatchStatus.UNREGISTERED)
                .build()

        when:
        def unregisteredMatchDetails1 = scoreboardAwareMatchRegistrar.unregisterMatch(TEAM_ID_1, TEAM_ID_2)
        def unregisteredMatchDetails2 = scoreboardAwareMatchRegistrar.unregisterMatch(TEAM_NAME_1, TEAM_NAME_2)

        then:
        1 * matchRegistrar.unregisterMatch(TEAM_ID_1, TEAM_ID_2) >> matchDetails
        1 * matchRegistrar.unregisterMatch(TEAM_NAME_1, TEAM_NAME_2) >> matchDetails
        0 * _

        and:
        unregisteredMatchDetails1 == matchDetails
        unregisteredMatchDetails2 == matchDetails
    }

    def 'should get all matches'() {
        given:
        def scoreboardAwareMatchRegistrar = new DefaultScoreboardAwareMatchRegistrar(matchRegistrar)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)
        def homeTeamScore = 2
        def awayTeamScore1 = 3
        def awayTeamScore2 = 4
        def matchDetails1 = MatchDetails.builder()
                .matchId(MATCH_ID)
                .homeTeam(team1)
                .homeTeamScore(homeTeamScore)
                .awayTeam(team2)
                .awayTeamScore(awayTeamScore1)
                .matchStatus(MatchStatus.REGISTERED)
                .build()
        def matchDetails2 = MatchDetails.builder()
                .matchId(MATCH_ID)
                .homeTeam(team1)
                .homeTeamScore(homeTeamScore)
                .awayTeam(team2)
                .awayTeamScore(awayTeamScore2)
                .matchStatus(MatchStatus.IN_PROGRESS)
                .build()

        when:
        def allMatchesResult = scoreboardAwareMatchRegistrar.getAllMatches()

        then:
        1 * matchRegistrar.getAllMatches() >> [matchDetails1, matchDetails2]
        0 * _

        and:
        allMatchesResult == [matchDetails1, matchDetails2]
    }

    def 'should get active matches'() {
        given:
        def scoreboardAwareMatchRegistrar = new DefaultScoreboardAwareMatchRegistrar(matchRegistrar)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)
        def matchDetails = MatchDetails.builder()
                .matchId(MATCH_ID)
                .homeTeam(team1)
                .awayTeam(team2)
                .matchStatus(MatchStatus.IN_PROGRESS)
                .build()

        when:
        def activeMatchesResult = scoreboardAwareMatchRegistrar.getActiveMatches()

        then:
        1 * matchRegistrar.getActiveMatches() >> [matchDetails]
        0 * _

        and:
        activeMatchesResult == [matchDetails]
    }

    @Unroll
    def 'should verify if match registered by match ID'() {
        given:
        def scoreboardAwareMatchRegistrar = new DefaultScoreboardAwareMatchRegistrar(matchRegistrar)

        when:
        def isRegisteredResult = scoreboardAwareMatchRegistrar.isMatchRegistered(MATCH_ID)

        then:
        1 * matchRegistrar.isMatchRegistered(MATCH_ID) >> isRegistered
        0 * _

        and:
        isRegisteredResult == isRegistered

        where:
        isRegistered << [true, false]
    }

    @Unroll
    def 'should verify if match registered by team IDs and team names'() {
        given:
        def scoreboardAwareMatchRegistrar = new DefaultScoreboardAwareMatchRegistrar(matchRegistrar)

        when:
        def isRegisteredResult = scoreboardAwareMatchRegistrar.isMatchRegistered(TEAM_ID_1, TEAM_ID_2)

        then:
        1 * matchRegistrar.isMatchRegistered(TEAM_ID_1, TEAM_ID_2) >> isRegistered
        0 * _

        and:
        isRegisteredResult == isRegistered

        when:
        isRegisteredResult = scoreboardAwareMatchRegistrar.isMatchRegistered(TEAM_NAME_1, TEAM_NAME_2)

        then:
        1 * matchRegistrar.isMatchRegistered(TEAM_NAME_1, TEAM_NAME_2) >> isRegistered
        0 * _

        and:
        isRegisteredResult == isRegistered

        where:
        isRegistered << [true, false]
    }

    def 'should get matches scoreboard from highest rank to lowest'() {
        given:
        def scoreboardAwareMatchRegistrar = new DefaultScoreboardAwareMatchRegistrar(matchRegistrar)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)
        def homeTeamScore = 2
        def awayTeamScore = 3
        def matchDetails1 = MatchDetails.builder()
                .matchId(126)
                .homeTeam(team1)
                .homeTeamScore(2)
                .awayTeam(team2)
                .awayTeamScore(3)
                .matchStatus(MatchStatus.IN_PROGRESS)
                .matchStartTimestamp(Instant.ofEpochMilli(FIXED_EPOCH_MILLI + 10))
                .build()
        def matchDetails2 = MatchDetails.builder()
                .matchId(128)
                .homeTeam(team1)
                .homeTeamScore(5)
                .awayTeam(team2)
                .awayTeamScore(3)
                .matchStatus(MatchStatus.IN_PROGRESS)
                .matchStartTimestamp(Instant.ofEpochMilli(FIXED_EPOCH_MILLI + 20))
                .build()
        def matchDetails3 = MatchDetails.builder()
                .matchId(129)
                .homeTeam(team1)
                .homeTeamScore(2)
                .awayTeam(team2)
                .awayTeamScore(3)
                .matchStatus(MatchStatus.IN_PROGRESS)
                .matchStartTimestamp(Instant.ofEpochMilli(FIXED_EPOCH_MILLI + 30))
                .build()

        when:
        scoreboardAwareMatchRegistrar.updateMatchScore(126, homeTeamScore, awayTeamScore)

        then:
        1 * matchRegistrar.updateMatchScore(126, homeTeamScore, awayTeamScore)
        1 * matchRegistrar.getActiveMatches() >> [matchDetails1, matchDetails2, matchDetails3]
        0 * _

        when:
        def scoreboardSummary = scoreboardAwareMatchRegistrar.getMatchesScoreboardSummary()

        then:
        0 * _

        and:
        scoreboardSummary.getMatches().size() == 3
        scoreboardSummary.getMatches().get(0).getMatchId() == 128
        scoreboardSummary.getMatches().get(1).getMatchId() == 129
        scoreboardSummary.getMatches().get(2).getMatchId() == 126

        and:
        scoreboardSummary.toString() == '1. United Kingdom 5 - Poland 3\n2. United Kingdom 2 - Poland 3\n3. United Kingdom 2 - Poland 3'
    }

}
