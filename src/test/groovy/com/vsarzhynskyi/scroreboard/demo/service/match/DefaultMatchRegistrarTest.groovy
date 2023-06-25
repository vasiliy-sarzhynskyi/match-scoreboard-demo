package com.vsarzhynskyi.scroreboard.demo.service.match

import com.vsarzhynskyi.scroreboard.demo.exception.MatchAlreadyRegisteredException
import com.vsarzhynskyi.scroreboard.demo.exception.MatchInvalidUpdateException
import com.vsarzhynskyi.scroreboard.demo.exception.MatchNotRegisteredException
import com.vsarzhynskyi.scroreboard.demo.model.MatchDetails
import com.vsarzhynskyi.scroreboard.demo.model.MatchStatus
import com.vsarzhynskyi.scroreboard.demo.model.Team
import com.vsarzhynskyi.scroreboard.demo.service.IdGenerator
import com.vsarzhynskyi.scroreboard.demo.service.team.TeamRegistrar
import spock.lang.Specification
import spock.lang.Unroll

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

    def 'should register new match by team names'() {
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

    def 'should register new match by team IDs'() {
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
        def registeredMatch = matchRegistrar.registerMatch(TEAM_ID_1, TEAM_ID_2)

        then:
        1 * teamRegistrar.getTeam(TEAM_ID_1) >> team1
        1 * teamRegistrar.getTeam(TEAM_ID_2) >> team2
        1 * matchIdGenerator.nextId() >> MATCH_ID
        0 * _

        and:
        registeredMatch == expectedRegisteredMatch

        and:
        matchRegistrar.getAllMatches().size() == 1
    }

    def 'should fail register already registered match'() {
        given:
        def matchRegistrar = new DefaultMatchRegistrar(matchIdGenerator, teamRegistrar, clock)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)

        when:
        matchRegistrar.registerMatch(TEAM_ID_1, TEAM_ID_2)

        then:
        1 * teamRegistrar.getTeam(TEAM_ID_1) >> team1
        1 * teamRegistrar.getTeam(TEAM_ID_2) >> team2
        1 * matchIdGenerator.nextId() >> MATCH_ID
        0 * _

        when:
        matchRegistrar.registerMatch(TEAM_ID_1, TEAM_ID_2)

        then:
        1 * teamRegistrar.getTeam(TEAM_ID_1) >> team1
        1 * teamRegistrar.getTeam(TEAM_ID_2) >> team2
        0 * _

        and:
        thrown(MatchAlreadyRegisteredException)
    }

    def 'should start registered match by match ID'() {
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
        startedMatch.matchStartTimestamp == clock.instant()
    }

    def 'should start registered match by match team IDs'() {
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
        def startedMatch = matchRegistrar.startMatch(TEAM_ID_1, TEAM_ID_2)

        then:
        1 * teamRegistrar.getTeam(TEAM_ID_1) >> team1
        1 * teamRegistrar.getTeam(TEAM_ID_2) >> team2
        0 * _

        and:
        startedMatch.matchStatus == MatchStatus.IN_PROGRESS
        startedMatch.matchId == registeredMatch.getMatchId()
    }

    def 'should start registered match by match team names'() {
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
        def startedMatch = matchRegistrar.startMatch(TEAM_NAME_1, TEAM_NAME_2)

        then:
        1 * teamRegistrar.getTeam(TEAM_NAME_1) >> team1
        1 * teamRegistrar.getTeam(TEAM_NAME_2) >> team2
        0 * _

        and:
        startedMatch.matchStatus == MatchStatus.IN_PROGRESS
        startedMatch.matchId == registeredMatch.getMatchId()
    }

    def 'should fail start non registered match by team names'() {
        given:
        def matchRegistrar = new DefaultMatchRegistrar(matchIdGenerator, teamRegistrar, clock)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)

        when:
        matchRegistrar.startMatch(TEAM_NAME_1, TEAM_NAME_2)

        then:
        1 * teamRegistrar.getTeam(TEAM_NAME_1) >> team1
        1 * teamRegistrar.getTeam(TEAM_NAME_2) >> team2
        0 * _

        and:
        thrown(MatchNotRegisteredException)
    }

    def 'should fail start non registered match by match ID'() {
        given:
        def matchRegistrar = new DefaultMatchRegistrar(matchIdGenerator, teamRegistrar, clock)

        when:
        matchRegistrar.startMatch(MATCH_ID)

        then:
        0 * _

        and:
        thrown(MatchNotRegisteredException)
    }

    def 'should fail start registered match if specific team already actively playing in another match'() {
        given:
        def matchRegistrar = new DefaultMatchRegistrar(matchIdGenerator, teamRegistrar, clock)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)
        def anotherTeamName = 'Super Team'
        def anotherMatchId = 659
        def team3 = new Team(787, anotherTeamName)

        when:
        matchRegistrar.registerMatch(TEAM_NAME_1, TEAM_NAME_2)
        matchRegistrar.registerMatch(TEAM_NAME_2, anotherTeamName)

        then:
        1 * teamRegistrar.getTeam(TEAM_NAME_1) >> team1
        2 * teamRegistrar.getTeam(TEAM_NAME_2) >> team2
        1 * teamRegistrar.getTeam(anotherTeamName) >> team3
        1 * matchIdGenerator.nextId() >> MATCH_ID
        1 * matchIdGenerator.nextId() >> anotherMatchId
        0 * _

        when:
        def startedMatch = matchRegistrar.startMatch(MATCH_ID)

        then:
        0 * _

        and:
        startedMatch.matchStatus == MatchStatus.IN_PROGRESS

        when:
        matchRegistrar.startMatch(anotherMatchId)

        then:
        0 * _

        and:
        thrown(MatchInvalidUpdateException)
    }

    def 'should allow start registered match if specific team already completed previous another match'() {
        given:
        def matchRegistrar = new DefaultMatchRegistrar(matchIdGenerator, teamRegistrar, clock)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)
        def anotherTeamName = 'Super Team'
        def anotherMatchId = 659
        def team3 = new Team(787, anotherTeamName)

        when:
        matchRegistrar.registerMatch(TEAM_NAME_1, TEAM_NAME_2)
        matchRegistrar.registerMatch(TEAM_NAME_2, anotherTeamName)

        then:
        1 * teamRegistrar.getTeam(TEAM_NAME_1) >> team1
        2 * teamRegistrar.getTeam(TEAM_NAME_2) >> team2
        1 * teamRegistrar.getTeam(anotherTeamName) >> team3
        1 * matchIdGenerator.nextId() >> MATCH_ID
        1 * matchIdGenerator.nextId() >> anotherMatchId
        0 * _

        when:
        matchRegistrar.startMatch(MATCH_ID)
        matchRegistrar.finishMatch(MATCH_ID)

        then:
        0 * _

        when:
        matchRegistrar.startMatch(anotherMatchId)

        then:
        0 * _
    }

    def 'should update active match score by match ID'() {
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

        when:
        updatedMatch = matchRegistrar.updateMatchScore(MATCH_ID, 2, 5)

        then:
        0 * _

        and:
        updatedMatch.matchStatus == MatchStatus.IN_PROGRESS
        updatedMatch.getHomeTeamScore() == 2
        updatedMatch.getAwayTeamScore() == 5
    }

    def 'should update active match score by match team ID'() {
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
        def updatedMatch = matchRegistrar.updateMatchScore(TEAM_ID_1, 2, TEAM_ID_2, 3)

        then:
        1 * teamRegistrar.getTeam(TEAM_ID_1) >> team1
        1 * teamRegistrar.getTeam(TEAM_ID_2) >> team2
        0 * _

        and:
        updatedMatch.getHomeTeamScore() == 2
        updatedMatch.getAwayTeamScore() == 3
    }

    def 'should update active match score by match team names'() {
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
        def updatedMatch = matchRegistrar.updateMatchScore(TEAM_NAME_1, 2, TEAM_NAME_2, 3)

        then:
        1 * teamRegistrar.getTeam(TEAM_NAME_1) >> team1
        1 * teamRegistrar.getTeam(TEAM_NAME_2) >> team2
        0 * _

        and:
        updatedMatch.getHomeTeamScore() == 2
        updatedMatch.getAwayTeamScore() == 3
    }

    def 'should throw exception on update score for non active match'() {
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
        matchRegistrar.updateMatchScore(TEAM_NAME_1, 2, TEAM_NAME_2, 3)

        then:
        1 * teamRegistrar.getTeam(TEAM_NAME_1) >> team1
        1 * teamRegistrar.getTeam(TEAM_NAME_2) >> team2
        0 * _

        and:
        thrown(MatchInvalidUpdateException)
    }

    @Unroll
    def 'should throw exception on update active match score with negative value'() {
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
        matchRegistrar.updateMatchScore(TEAM_NAME_1, homeTeamScore, TEAM_NAME_2, awayTeamScore)

        then:
        1 * teamRegistrar.getTeam(TEAM_NAME_1) >> team1
        1 * teamRegistrar.getTeam(TEAM_NAME_2) >> team2
        0 * _

        and:
        thrown(MatchInvalidUpdateException)

        where:
        homeTeamScore | awayTeamScore
        -2            | -5
        -2            | 1
        1             | -3
    }

    def 'should finish active match by match ID'() {
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

    def 'should finish active match by match team IDs'() {
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
        def finishedMatch = matchRegistrar.finishMatch(TEAM_ID_1, TEAM_ID_2)

        then:
        1 * teamRegistrar.getTeam(TEAM_ID_1) >> team1
        1 * teamRegistrar.getTeam(TEAM_ID_2) >> team2
        0 * _

        and:
        finishedMatch.matchStatus == MatchStatus.FINISHED
    }

    def 'should finish active match by match team names'() {
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
        def finishedMatch = matchRegistrar.finishMatch(TEAM_NAME_1, TEAM_NAME_2)

        then:
        1 * teamRegistrar.getTeam(TEAM_NAME_1) >> team1
        1 * teamRegistrar.getTeam(TEAM_NAME_2) >> team2
        0 * _

        and:
        finishedMatch.matchStatus == MatchStatus.FINISHED
    }

    def 'should throw exception on finish non active match'() {
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
        matchRegistrar.finishMatch(MATCH_ID)

        then:
        0 * _

        and:
        thrown(MatchInvalidUpdateException)
    }

    def 'should unregister active match by match ID'() {
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

    def 'should unregister active match by match team IDs'() {
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
        def unregisteredMatch = matchRegistrar.unregisterMatch(TEAM_ID_1, TEAM_ID_2)

        then:
        1 * teamRegistrar.getTeam(TEAM_ID_1) >> team1
        1 * teamRegistrar.getTeam(TEAM_ID_2) >> team2
        0 * _

        and:
        unregisteredMatch.matchStatus == MatchStatus.UNREGISTERED
    }

    def 'should unregister active match by match team names'() {
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
        def unregisteredMatch = matchRegistrar.unregisterMatch(TEAM_NAME_1, TEAM_NAME_2)

        then:
        1 * teamRegistrar.getTeam(TEAM_NAME_1) >> team1
        1 * teamRegistrar.getTeam(TEAM_NAME_2) >> team2
        0 * _

        and:
        unregisteredMatch.matchStatus == MatchStatus.UNREGISTERED
    }

    def 'should fail unregister non registered match'() {
        given:
        def matchRegistrar = new DefaultMatchRegistrar(matchIdGenerator, teamRegistrar, clock)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)

        when:
        matchRegistrar.unregisterMatch(TEAM_ID_1, TEAM_ID_2)

        then:
        1 * teamRegistrar.getTeam(TEAM_ID_1) >> team1
        1 * teamRegistrar.getTeam(TEAM_ID_2) >> team2
        0 * _

        and:
        thrown(MatchNotRegisteredException)
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

    def 'get all registered matches should not return unregistered matches'() {
        given:
        def matchRegistrar = new DefaultMatchRegistrar(matchIdGenerator, teamRegistrar, clock)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)
        def team3 = new Team(7, 'Canada')
        def team4 = new Team(8, 'Netherlands')
        def matchId2 = 178

        when:
        def allMatches = matchRegistrar.getAllMatches()

        then:
        allMatches.isEmpty()

        when:
        matchRegistrar.registerMatch(TEAM_NAME_1, TEAM_NAME_2)
        matchRegistrar.startMatch(MATCH_ID)
        matchRegistrar.registerMatch('Canada', 'Netherlands')

        then:
        1 * teamRegistrar.getTeam(TEAM_NAME_1) >> team1
        1 * teamRegistrar.getTeam(TEAM_NAME_2) >> team2
        1 * matchIdGenerator.nextId() >> MATCH_ID
        1 * teamRegistrar.getTeam('Canada') >> team3
        1 * teamRegistrar.getTeam('Netherlands') >> team4
        1 * matchIdGenerator.nextId() >> matchId2
        0 * _

        when:
        allMatches = matchRegistrar.getAllMatches()

        then:
        0 * _

        and:
        allMatches.size() == 2

        when:
        matchRegistrar.finishMatch(MATCH_ID)
        matchRegistrar.unregisterMatch(MATCH_ID)

        then:
        0 * _

        when:
        allMatches = matchRegistrar.getAllMatches()

        then:
        0 * _

        and:
        allMatches.size() == 1
        allMatches.get(0).matchId == matchId2
    }

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

    def 'should verify whether match is registered by match ID'() {
        given:
        def matchRegistrar = new DefaultMatchRegistrar(matchIdGenerator, teamRegistrar, clock)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)

        when:
        def isRegistered = matchRegistrar.isMatchRegistered(MATCH_ID)

        then:
        !isRegistered

        when:
        matchRegistrar.registerMatch(TEAM_ID_1, TEAM_ID_2)

        then:
        1 * teamRegistrar.getTeam(TEAM_ID_1) >> team1
        1 * teamRegistrar.getTeam(TEAM_ID_2) >> team2
        1 * matchIdGenerator.nextId() >> MATCH_ID
        0 * _

        when:
        isRegistered = matchRegistrar.isMatchRegistered(MATCH_ID)

        then:
        isRegistered
    }

    def 'should verify whether match is registered by match team IDs'() {
        given:
        def matchRegistrar = new DefaultMatchRegistrar(matchIdGenerator, teamRegistrar, clock)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)

        when:
        def isRegistered = matchRegistrar.isMatchRegistered(TEAM_ID_1, TEAM_ID_2)

        then:
        1 * teamRegistrar.getTeam(TEAM_ID_1) >> team1
        1 * teamRegistrar.getTeam(TEAM_ID_2) >> team2
        0 * _

        and:
        !isRegistered
    }

    def 'should verify whether match is registered by match team names'() {
        given:
        def matchRegistrar = new DefaultMatchRegistrar(matchIdGenerator, teamRegistrar, clock)
        def team1 = new Team(TEAM_ID_1, TEAM_NAME_1)
        def team2 = new Team(TEAM_ID_2, TEAM_NAME_2)

        when:
        def isRegistered = matchRegistrar.isMatchRegistered(TEAM_NAME_1, TEAM_NAME_2)

        then:
        1 * teamRegistrar.getTeam(TEAM_NAME_1) >> team1
        1 * teamRegistrar.getTeam(TEAM_NAME_2) >> team2
        0 * _

        and:
        !isRegistered
    }

}
