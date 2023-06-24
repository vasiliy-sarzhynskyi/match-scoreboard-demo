package com.vsarzhynskyi.scroreboard.demo.service;

import com.vsarzhynskyi.scroreboard.demo.model.MatchDetails;
import com.vsarzhynskyi.scroreboard.demo.model.MatchStatus;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DefaultMatchRegistrar implements MatchRegistrar {

    private final IdGenerator matchIdGenerator;
    private final TeamRegistrar teamRegistrar;
    private final Clock clock;

    private final ReadWriteLock readWriteLock;
    private final Map<Integer, MatchDetails> matchIdToMatchDetailsMapping;

    public DefaultMatchRegistrar(IdGenerator matchIdGenerator,
                                 TeamRegistrar teamRegistrar,
                                 Clock clock) {
        this.matchIdGenerator = matchIdGenerator;
        this.teamRegistrar = teamRegistrar;
        this.clock = clock;

        readWriteLock = new ReentrantReadWriteLock();
        matchIdToMatchDetailsMapping = new ConcurrentHashMap<>();
    }

    @Override
    public MatchDetails registerMatch(String homeTeamName, String awayTeamName) {
        var homeTeam = teamRegistrar.getTeam(homeTeamName);
        var awayTeam = teamRegistrar.getTeam(awayTeamName);
        var writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            var matchId = matchIdGenerator.nextId();
            var matchDetails = MatchDetails.builder()
                    .matchId(matchId)
                    .homeTeam(homeTeam)
                    .awayTeam(awayTeam)
                    .matchStatus(MatchStatus.REGISTERED)
                    .lastUpdatedTimestamp(Instant.now(clock))
                    .build();
            matchIdToMatchDetailsMapping.put(matchId, matchDetails);
            return matchDetails;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public MatchDetails startMatch(int matchId) {
        var writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            var match = matchIdToMatchDetailsMapping.get(matchId);
            var updatedMatch = match.toBuilder()
                    .matchStatus(MatchStatus.IN_PROGRESS)
                    .lastUpdatedTimestamp(Instant.now(clock))
                    .build();
            matchIdToMatchDetailsMapping.put(matchId, updatedMatch);
            return updatedMatch;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public MatchDetails updateMatchScore(int matchId, int homeTeamScore, int awayTeamScore) {
        var writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            var matchDetails = matchIdToMatchDetailsMapping.get(matchId);
            var updatedMatchDetails = matchDetails.toBuilder()
                    .homeTeamScore(homeTeamScore)
                    .awayTeamScore(awayTeamScore)
                    .lastUpdatedTimestamp(Instant.now(clock))
                    .build();
            matchIdToMatchDetailsMapping.put(matchId, updatedMatchDetails);
            return updatedMatchDetails;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public MatchDetails finishMatch(int matchId) {
        var writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            var match = matchIdToMatchDetailsMapping.get(matchId);
            var updatedMatch = match.toBuilder()
                    .matchStatus(MatchStatus.FINISHED)
                    .lastUpdatedTimestamp(Instant.now(clock))
                    .build();
            matchIdToMatchDetailsMapping.put(matchId, updatedMatch);
            return updatedMatch;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public MatchDetails unregisterMatch(int matchId) {
        var writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            var removedMatch = matchIdToMatchDetailsMapping.remove(matchId);
            return removedMatch.toBuilder()
                    .matchStatus(MatchStatus.UNREGISTERED)
                    .lastUpdatedTimestamp(Instant.now(clock))
                    .build();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public List<MatchDetails> getAllMatches() {
        return new ArrayList<>(matchIdToMatchDetailsMapping.values());
    }


    @Override
    public List<MatchDetails> getActiveMatches() {
        // TODO: implement
        return List.of();
    }

    public List<MatchDetails> getMatchesScoreboard() {
        // TODO: implement
        return List.of();
    }

}
