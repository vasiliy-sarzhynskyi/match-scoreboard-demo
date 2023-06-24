package com.vsarzhynskyi.scroreboard.demo.service

import spock.lang.Specification

class TeamIdGeneratorTest extends Specification {

    def 'should generate each time unique identifier'() {
        given:
        def teamIdGenerator = new TeamIdGenerator()

        when:
        def id1 = teamIdGenerator.nextId()
        def id2 = teamIdGenerator.nextId()
        def id3 = teamIdGenerator.nextId()

        then:
        id1 == 1
        id2 == 2
        id3 == 3
    }

}
