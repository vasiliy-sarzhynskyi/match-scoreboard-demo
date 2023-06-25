package com.vsarzhynskyi.scroreboard.demo.service.team

import com.vsarzhynskyi.scroreboard.demo.exception.TeamAlreadyRegisteredException
import com.vsarzhynskyi.scroreboard.demo.exception.TeamNameInvalidException
import com.vsarzhynskyi.scroreboard.demo.exception.TeamNotRegisteredException
import com.vsarzhynskyi.scroreboard.demo.service.IdGenerator
import spock.lang.Specification
import spock.lang.Unroll

class DefaultTeamRegistrarTest extends Specification {

    private static final TEAM_ID = 2
    private static final TEAM_NAME = 'Norway'

    def teamIdGenerator = Mock(IdGenerator)

    @Unroll
    def 'should register team'() {
        given:
        def teamRegistrar = new DefaultTeamRegistrar(teamIdGenerator)

        when:
        def registeredTeam = teamRegistrar.registerTeam(teamName)

        then:
        1 * teamIdGenerator.nextId() >> TEAM_ID
        0 * _

        and:
        registeredTeam.getName() == teamName
        registeredTeam.getId() == TEAM_ID

        where:
        teamName << [
                'Ukraine', 'Team 1', 'Team-2', 'United Kingdom', 'Super    team'
        ]
    }

    def 'should throw exception on register already registered team'() {
        given:
        def teamRegistrar = new DefaultTeamRegistrar(teamIdGenerator)

        when:
        teamRegistrar.registerTeam(TEAM_NAME)

        then:
        1 * teamIdGenerator.nextId() >> TEAM_ID
        0 * _

        when:
        teamRegistrar.registerTeam(TEAM_NAME)

        then:
        thrown(TeamAlreadyRegisteredException)
    }

    @Unroll
    def 'should throw exception on register invalid team name'() {
        given:
        def teamRegistrar = new DefaultTeamRegistrar(teamIdGenerator)

        when:
        teamRegistrar.registerTeam(teamName)

        then:
        0 * _

        and:
        thrown(TeamNameInvalidException)

        where:
        teamName << [
                null, '    ', '? team', 'Team ++ Team'
        ]
    }

    def 'should unregister already registered team by team ID'() {
        given:
        def teamRegistrar = new DefaultTeamRegistrar(teamIdGenerator)

        when:
        teamRegistrar.registerTeam(TEAM_NAME)

        then:
        1 * teamIdGenerator.nextId() >> TEAM_ID
        0 * _

        when:
        def isRegistered = teamRegistrar.isTeamRegistered(TEAM_ID)

        then:
        isRegistered

        when:
        teamRegistrar.unregisterTeam(TEAM_ID)

        then:
        0 * _

        when:
        isRegistered = teamRegistrar.isTeamRegistered(TEAM_ID)

        then:
        !isRegistered
    }

    def 'should fail unregister non registered team by team ID'() {
        given:
        def teamRegistrar = new DefaultTeamRegistrar(teamIdGenerator)

        when:
        teamRegistrar.unregisterTeam(TEAM_ID)

        then:
        0 * _

        and:
        thrown(TeamNotRegisteredException)
    }

    def 'should unregister already registered team by team name'() {
        given:
        def teamRegistrar = new DefaultTeamRegistrar(teamIdGenerator)

        when:
        teamRegistrar.registerTeam(TEAM_NAME)

        then:
        1 * teamIdGenerator.nextId() >> TEAM_ID
        0 * _

        when:
        teamRegistrar.unregisterTeam(TEAM_NAME)

        then:
        0 * _

        when:
        def isRegistered = teamRegistrar.isTeamRegistered(TEAM_ID)

        then:
        !isRegistered
    }

    def 'should fail unregister non registered team by team name'() {
        given:
        def teamRegistrar = new DefaultTeamRegistrar(teamIdGenerator)

        when:
        teamRegistrar.unregisterTeam(TEAM_NAME)

        then:
        0 * _

        and:
        thrown(TeamNotRegisteredException)
    }

    def 'should get registered team by team name'() {
        given:
        def teamRegistrar = new DefaultTeamRegistrar(teamIdGenerator)
        def anotherTeamName = 'Spain'
        def anotherTeamId = 19

        when:
        teamRegistrar.registerTeam(anotherTeamName)
        teamRegistrar.registerTeam(TEAM_NAME)

        then:
        1 * teamIdGenerator.nextId() >> anotherTeamId
        1 * teamIdGenerator.nextId() >> TEAM_ID
        0 * _

        when:
        def fetchedTeam = teamRegistrar.getTeam(TEAM_NAME)

        then:
        fetchedTeam.getId() == TEAM_ID
        fetchedTeam.getName() == TEAM_NAME
    }

    def 'should throw exception on get non registered team by team name'() {
        given:
        def teamRegistrar = new DefaultTeamRegistrar(teamIdGenerator)

        when:
        teamRegistrar.getTeam(TEAM_NAME)

        then:
        thrown(TeamNotRegisteredException)
    }

    def 'should get registered team by team ID'() {
        given:
        def teamRegistrar = new DefaultTeamRegistrar(teamIdGenerator)
        def anotherTeamName = 'Spain'
        def anotherTeamId = 19

        when:
        teamRegistrar.registerTeam(anotherTeamName)
        teamRegistrar.registerTeam(TEAM_NAME)

        then:
        1 * teamIdGenerator.nextId() >> anotherTeamId
        1 * teamIdGenerator.nextId() >> TEAM_ID
        0 * _

        when:
        def fetchedTeam = teamRegistrar.getTeam(anotherTeamId)

        then:
        fetchedTeam.getId() == anotherTeamId
        fetchedTeam.getName() == anotherTeamName
    }

    def 'should throw exception on get non registered team by team ID'() {
        given:
        def teamRegistrar = new DefaultTeamRegistrar(teamIdGenerator)

        when:
        teamRegistrar.getTeam(TEAM_ID)

        then:
        thrown(TeamNotRegisteredException)
    }

    def 'should verify is team registered by team name'() {
        given:
        def teamRegistrar = new DefaultTeamRegistrar(teamIdGenerator)

        when:
        teamRegistrar.registerTeam(TEAM_NAME)

        then:
        1 * teamIdGenerator.nextId() >> TEAM_ID
        0 * _

        when:
        def isRegistered = teamRegistrar.isTeamRegistered(TEAM_NAME)

        then:
        isRegistered
    }

    def 'should get all registered teams'() {
        given:
        def teamRegistrar = new DefaultTeamRegistrar(teamIdGenerator)

        when:
        def fetchedTeams = teamRegistrar.getAllTeams()

        then:
        fetchedTeams.isEmpty()

        when:
        teamRegistrar.registerTeam(TEAM_NAME)
        teamRegistrar.registerTeam('Slovenia')
        teamRegistrar.registerTeam('Austria')
        fetchedTeams = teamRegistrar.getAllTeams()

        then:
        1 * teamIdGenerator.nextId() >> 3
        1 * teamIdGenerator.nextId() >> 4
        1 * teamIdGenerator.nextId() >> 5
        0 * _

        fetchedTeams.size() == 3
        fetchedTeams.collect({ it.name }).toSet() == [TEAM_NAME, 'Slovenia', 'Austria'] as Set
    }

}
