package com.vsarzhynskyi.scroreboard.demo.service

import com.vsarzhynskyi.scroreboard.demo.model.MatchDetails
import com.vsarzhynskyi.scroreboard.demo.model.MatchStatus
import com.vsarzhynskyi.scroreboard.demo.model.Team
import spock.lang.Ignore
import spock.lang.Specification

import java.time.Clock
import java.time.Instant
import java.time.ZoneOffset

class DefaultMatchRegistrarTest extends Specification {

    private static final FIXED_EPOCH_MILLI = 14900000000L
    private static final TEAM_ID_1 = 1
    private static final TEAM_NAME_1 = 'United Kingdom'
    private static final TEAM_ID_2 = 2
    private static final TEAM_NAME_2 = 'Poland'
    private static final MATCH_ID = 123

    def matchIdGenerator = Mock(IdGenerator)
    def teamRegistrar = Mock(TeamRegistrar)
    def clock = Clock.fixed(Instant.ofEpochMilli(FIXED_EPOCH_MILLI), ZoneOffset.UTC)

    def 'should register new match'() {
        given:
        def matchRegistrar = new DefaultMatchRegistrar(matchIdGenerator, teamRegistrar, clock)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)
        def expectedRegisteredMatch = MatchDetails.builder()
                .matchId(MATCH_ID)
                .homeTeam(team1)
                .homeTeamScore(0)
                .awayTeam(team2)
                .awayTeamScore(0)
                .matchStatus(MatchStatus.REGISTERED)
                .lastUpdatedTimestamp(Instant.ofEpochMilli(FIXED_EPOCH_MILLI))
                .build()

        when:
        def registeredMatch = matchRegistrar.registerMatch(TEAM_NAME_1, TEAM_NAME_2)

        then:
        1 * teamRegistrar.getTeam(TEAM_NAME_1) >> team1
        1 * teamRegistrar.getTeam(TEAM_NAME_2) >> team2
        1 * matchIdGenerator.nextId() >> MATCH_ID
        0 * _

        and:
        registeredMatch == expectedRegisteredMatch

        and:
        matchRegistrar.getAllMatches().size() == 1
    }

    def 'should start registered match'() {
        given:
        def matchRegistrar = new DefaultMatchRegistrar(matchIdGenerator, teamRegistrar, clock)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)

        when:
        def registeredMatch = matchRegistrar.registerMatch(TEAM_NAME_1, TEAM_NAME_2)

        then:
        1 * teamRegistrar.getTeam(TEAM_NAME_1) >> team1
        1 * teamRegistrar.getTeam(TEAM_NAME_2) >> team2
        1 * matchIdGenerator.nextId() >> MATCH_ID
        0 * _

        when:
        def startedMatch = matchRegistrar.startMatch(MATCH_ID)

        then:
        0 * _

        and:
        startedMatch.matchStatus == MatchStatus.IN_PROGRESS
        startedMatch.matchId == registeredMatch.getMatchId()
    }

    def 'should update active match score'() {
        given:
        def matchRegistrar = new DefaultMatchRegistrar(matchIdGenerator, teamRegistrar, clock)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)

        when:
        matchRegistrar.registerMatch(TEAM_NAME_1, TEAM_NAME_2)
        matchRegistrar.startMatch(MATCH_ID)

        then:
        1 * teamRegistrar.getTeam(TEAM_NAME_1) >> team1
        1 * teamRegistrar.getTeam(TEAM_NAME_2) >> team2
        1 * matchIdGenerator.nextId() >> MATCH_ID
        0 * _

        when:
        def updatedMatch = matchRegistrar.updateMatchScore(MATCH_ID, 2, 3)

        then:
        0 * _

        and:
        updatedMatch.matchStatus == MatchStatus.IN_PROGRESS
        updatedMatch.getHomeTeamScore() == 2
        updatedMatch.getAwayTeamScore() == 3
    }

    def 'should finish active match'() {
        given:
        def matchRegistrar = new DefaultMatchRegistrar(matchIdGenerator, teamRegistrar, clock)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)

        when:
        matchRegistrar.registerMatch(TEAM_NAME_1, TEAM_NAME_2)
        matchRegistrar.startMatch(MATCH_ID)
        matchRegistrar.updateMatchScore(MATCH_ID, 2, 3)

        then:
        1 * teamRegistrar.getTeam(TEAM_NAME_1) >> team1
        1 * teamRegistrar.getTeam(TEAM_NAME_2) >> team2
        1 * matchIdGenerator.nextId() >> MATCH_ID
        0 * _

        when:
        def finishedMatch = matchRegistrar.finishMatch(MATCH_ID)

        then:
        0 * _

        and:
        finishedMatch.matchStatus == MatchStatus.FINISHED
        finishedMatch.getHomeTeamScore() == 2
        finishedMatch.getAwayTeamScore() == 3
    }

    def 'should unregister active match'() {
        given:
        def matchRegistrar = new DefaultMatchRegistrar(matchIdGenerator, teamRegistrar, clock)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)

        when:
        matchRegistrar.registerMatch(TEAM_NAME_1, TEAM_NAME_2)

        then:
        1 * teamRegistrar.getTeam(TEAM_NAME_1) >> team1
        1 * teamRegistrar.getTeam(TEAM_NAME_2) >> team2
        1 * matchIdGenerator.nextId() >> MATCH_ID
        0 * _

        when:
        def unregisteredMatch = matchRegistrar.unregisterMatch(MATCH_ID)

        then:
        0 * _

        and:
        unregisteredMatch.matchStatus == MatchStatus.UNREGISTERED
    }

    def 'should get all registered matches'() {
        given:
        def matchRegistrar = new DefaultMatchRegistrar(matchIdGenerator, teamRegistrar, clock)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)

        when:
        def allMatches = matchRegistrar.getAllMatches()

        then:
        allMatches.isEmpty()

        when:
        matchRegistrar.registerMatch(TEAM_NAME_1, TEAM_NAME_2)

        then:
        1 * teamRegistrar.getTeam(TEAM_NAME_1) >> team1
        1 * teamRegistrar.getTeam(TEAM_NAME_2) >> team2
        1 * matchIdGenerator.nextId() >> MATCH_ID
        0 * _

        when:
        allMatches = matchRegistrar.getAllMatches()

        then:
        0 * _

        and:
        allMatches.size() == 1
        allMatches.get(0).matchId == MATCH_ID
    }

    @Ignore
    def 'should get all active matches'() {
        given:
        def matchRegistrar = new DefaultMatchRegistrar(matchIdGenerator, teamRegistrar, clock)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)

        when:
        matchRegistrar.registerMatch(TEAM_NAME_1, TEAM_NAME_2)
        def activeMatches = matchRegistrar.getActiveMatches()

        then:
        1 * teamRegistrar.getTeam(TEAM_NAME_1) >> team1
        1 * teamRegistrar.getTeam(TEAM_NAME_2) >> team2
        1 * matchIdGenerator.nextId() >> MATCH_ID
        0 * _

        and:
        activeMatches.isEmpty()

        when:
        matchRegistrar.startMatch(MATCH_ID)
        activeMatches = matchRegistrar.getActiveMatches()

        then:
        0 * _

        and:
        activeMatches.size() == 1
        activeMatches.get(0).matchId == MATCH_ID
    }

    def 'should get matches scoreboard'() {

    }


}
