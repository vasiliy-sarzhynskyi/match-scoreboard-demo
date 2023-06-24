package com.vsarzhynskyi.scroreboard.demo.service

import spock.lang.Specification

class MatchIdGeneratorTest extends Specification {

    def 'should generate each time unique identifier'() {
        given:
        def matchIdGenerator = new MatchIdGenerator()

        when:
        def id1 = matchIdGenerator.nextId()
        def id2 = matchIdGenerator.nextId()
        def id3 = matchIdGenerator.nextId()

        then:
        id1 == 1
        id2 == 2
        id3 == 3
    }

}
