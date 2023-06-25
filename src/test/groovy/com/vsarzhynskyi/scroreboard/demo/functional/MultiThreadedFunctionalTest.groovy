package com.vsarzhynskyi.scroreboard.demo.functional

import com.vsarzhynskyi.scroreboard.demo.service.match.DefaultMatchRegistrar
import com.vsarzhynskyi.scroreboard.demo.service.match.DefaultScoreboardAwareMatchRegistrar
import com.vsarzhynskyi.scroreboard.demo.service.match.MatchIdGenerator
import com.vsarzhynskyi.scroreboard.demo.service.team.DefaultTeamRegistrar
import com.vsarzhynskyi.scroreboard.demo.service.team.TeamIdGenerator
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

import java.time.Clock
import java.time.ZoneOffset
import java.util.concurrent.Executors
import java.util.concurrent.ThreadLocalRandom

class MultiThreadedFunctionalTest extends Specification {

    private static final POLLING_CONDITIONS = new PollingConditions(timeout: 3)

    def 'should process multiple matches concurrently and scoreboard verification results'() {
        given:
        def threadPool = Executors.newFixedThreadPool(20)
        def teamIdGenerator = new TeamIdGenerator()
        def teamRegistrar = new DefaultTeamRegistrar(teamIdGenerator)
        def clock = Clock.system(ZoneOffset.UTC)
        def matchIdGenerator = new MatchIdGenerator()
        def matchRegistrar = new DefaultMatchRegistrar(matchIdGenerator, teamRegistrar, clock)
        def scoreboardAwareMatchRegistrar = new DefaultScoreboardAwareMatchRegistrar(matchRegistrar)

        when:
        (1..100).each { it ->
            {
                def teamName = 'Team' + it
                threadPool.submit({ teamRegistrar.registerTeam(teamName) })
            }
        }

        then:
        POLLING_CONDITIONS.eventually {
            assert teamRegistrar.getAllTeams().size() == 100
        }

        when:
        (1..50).each { it ->
            {
                def teamName1 = 'Team' + it
                def teamName2 = 'Team' + (it + 50)
                threadPool.submit({
                    scoreboardAwareMatchRegistrar.registerMatch(teamName1, teamName2)
                    scoreboardAwareMatchRegistrar.startMatch(teamName1, teamName2)
                })
            }
        }

        then:
        POLLING_CONDITIONS.eventually {
            assert scoreboardAwareMatchRegistrar.getActiveMatches().size() == 50
            println '\n' + scoreboardAwareMatchRegistrar.getMatchesScoreboardSummary()
        }

        when:
        (1..50).each { it ->
            {
                def teamName1 = 'Team' + it
                def teamName2 = 'Team' + (it + 50)
                threadPool.submit({
                    scoreboardAwareMatchRegistrar.updateMatchScore(teamName1, ThreadLocalRandom.current().nextInt(5) + 1, teamName2, ThreadLocalRandom.current().nextInt(5) + 1)
                })
            }
        }

        then:
        POLLING_CONDITIONS.eventually {
            assert scoreboardAwareMatchRegistrar.getActiveMatches().size() == 50
            scoreboardAwareMatchRegistrar.getActiveMatches().each {
                activeMatch -> assert activeMatch.getHomeTeamScore() != 0 && activeMatch.getAwayTeamScore() != 0
            }
            println '\n' + scoreboardAwareMatchRegistrar.getMatchesScoreboardSummary()
        }
    }

}
