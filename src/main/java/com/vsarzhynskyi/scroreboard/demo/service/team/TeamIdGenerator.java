package com.vsarzhynskyi.scroreboard.demo.service.team;

import com.vsarzhynskyi.scroreboard.demo.service.IdGenerator;

import java.util.concurrent.atomic.AtomicInteger;

public class TeamIdGenerator implements IdGenerator {

    private final AtomicInteger incrementor = new AtomicInteger();

    @Override
    public int nextId() {
        return incrementor.incrementAndGet();
    }

}
