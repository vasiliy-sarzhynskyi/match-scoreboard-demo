package com.vsarzhynskyi.scroreboard.demo

import spock.lang.Specification
import spock.lang.Unroll

class DemoTest extends Specification {

    @Unroll
    def 'test that add operation matches expectations'() {
        expect:
        new Demo().add(a, b) == expectedResult

        where:
        a  | b  || expectedResult
        1  | 0  || 1
        1  | 9  || 10
        10 | 15 || 25
    }

}
