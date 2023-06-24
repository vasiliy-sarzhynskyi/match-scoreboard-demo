package com.vsarzhynskyi.scroreboard.demo.service.match;

import com.vsarzhynskyi.scroreboard.demo.exception.MatchAlreadyRegisteredException;
import com.vsarzhynskyi.scroreboard.demo.exception.MatchNotRegisteredException;
import com.vsarzhynskyi.scroreboard.demo.model.MatchDetails;
import com.vsarzhynskyi.scroreboard.demo.model.MatchStatus;
import com.vsarzhynskyi.scroreboard.demo.model.Team;
import com.vsarzhynskyi.scroreboard.demo.service.IdGenerator;
import com.vsarzhynskyi.scroreboard.demo.service.team.TeamRegistrar;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.lang.String.format;
import static java.util.Objects.isNull;

public class DefaultMatchRegistrar implements MatchRegistrar {

    private static final String CONCATENATED_TEAM_IDS_TEMPLATE = "%d_%d";

    private final IdGenerator matchIdGenerator;
    private final TeamRegistrar teamRegistrar;
    private final Clock clock;

    private final ReadWriteLock readWriteLock;
    private final Map<Integer, MatchDetails> matchIdToMatchDetailsMapping;
    private final Map<String, MatchDetails> matchTeamIdsToMatchDetailsMapping;

    public DefaultMatchRegistrar(IdGenerator matchIdGenerator,
                                 TeamRegistrar teamRegistrar,
                                 Clock clock) {
        this.matchIdGenerator = matchIdGenerator;
        this.teamRegistrar = teamRegistrar;
        this.clock = clock;

        readWriteLock = new ReentrantReadWriteLock();
        matchIdToMatchDetailsMapping = new ConcurrentHashMap<>();
        matchTeamIdsToMatchDetailsMapping = new ConcurrentHashMap<>();
    }

    @Override
    public MatchDetails registerMatch(int homeTeamId, int awayTeamId) {
        var homeTeam = teamRegistrar.getTeam(homeTeamId);
        var awayTeam = teamRegistrar.getTeam(awayTeamId);
        return registerMatch(homeTeam, awayTeam);
    }

    @Override
    public MatchDetails registerMatch(String homeTeamName, String awayTeamName) {
        var homeTeam = teamRegistrar.getTeam(homeTeamName);
        var awayTeam = teamRegistrar.getTeam(awayTeamName);
        return registerMatch(homeTeam, awayTeam);
    }

    private MatchDetails registerMatch(Team homeTeam, Team awayTeam) {
        var writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            var concatenateTeamIds = concatenateTeamIds(homeTeam, awayTeam);
            if (matchTeamIdsToMatchDetailsMapping.containsKey(concatenateTeamIds)) {
                throw new MatchAlreadyRegisteredException(homeTeam.getName(), awayTeam.getName());
            }

            var matchId = matchIdGenerator.nextId();
            var matchDetails = MatchDetails.builder()
                    .matchId(matchId)
                    .homeTeam(homeTeam)
                    .awayTeam(awayTeam)
                    .matchStatus(MatchStatus.REGISTERED)
                    .lastUpdatedTimestamp(Instant.now(clock))
                    .build();
            matchIdToMatchDetailsMapping.put(matchId, matchDetails);
            matchTeamIdsToMatchDetailsMapping.put(concatenateTeamIds, matchDetails);
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
            verifyMatchIdRegistered(matchId);
            var match = matchIdToMatchDetailsMapping.get(matchId);
            var updatedMatch = match.toBuilder()
                    .matchStatus(MatchStatus.IN_PROGRESS)
                    .lastUpdatedTimestamp(Instant.now(clock))
                    .build();
            matchIdToMatchDetailsMapping.put(matchId, updatedMatch);
            var concatenatedTeamIds = concatenateTeamIds(match.getHomeTeam(), match.getAwayTeam());
            matchTeamIdsToMatchDetailsMapping.put(concatenatedTeamIds, updatedMatch);
            return updatedMatch;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public MatchDetails startMatch(int homeTeamId, int awayTeamId) {
        var homeTeam = teamRegistrar.getTeam(homeTeamId);
        var awayTeam = teamRegistrar.getTeam(awayTeamId);
        var match = getMatchDetailsOrThrowException(homeTeam, awayTeam);
        return startMatch(match.getMatchId());
    }

    @Override
    public MatchDetails startMatch(String homeTeamName, String awayTeamName) {
        var homeTeam = teamRegistrar.getTeam(homeTeamName);
        var awayTeam = teamRegistrar.getTeam(awayTeamName);
        var match = getMatchDetailsOrThrowException(homeTeam, awayTeam);
        return startMatch(match.getMatchId());
    }

    @Override
    public MatchDetails updateMatchScore(int matchId, int homeTeamScore, int awayTeamScore) {
        var writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            verifyMatchIdRegistered(matchId);
            var matchDetails = matchIdToMatchDetailsMapping.get(matchId);
            var updatedMatchDetails = matchDetails.toBuilder()
                    .homeTeamScore(homeTeamScore)
                    .awayTeamScore(awayTeamScore)
                    .lastUpdatedTimestamp(Instant.now(clock))
                    .build();
            var concatenatedTeamIds = concatenateTeamIds(matchDetails.getHomeTeam(), matchDetails.getAwayTeam());
            matchIdToMatchDetailsMapping.put(matchId, updatedMatchDetails);
            matchTeamIdsToMatchDetailsMapping.put(concatenatedTeamIds, updatedMatchDetails);
            return updatedMatchDetails;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public MatchDetails updateMatchScore(int homeTeamId, int homeTeamScore, int awayTeamId, int awayTeamScore) {
        var homeTeam = teamRegistrar.getTeam(homeTeamId);
        var awayTeam = teamRegistrar.getTeam(awayTeamId);
        var match = getMatchDetailsOrThrowException(homeTeam, awayTeam);
        return updateMatchScore(match.getMatchId(), homeTeamScore, awayTeamScore);
    }

    @Override
    public MatchDetails updateMatchScore(String homeTeamName, int homeTeamScore, String awayTeamName, int awayTeamScore) {
        var homeTeam = teamRegistrar.getTeam(homeTeamName);
        var awayTeam = teamRegistrar.getTeam(awayTeamName);
        var match = getMatchDetailsOrThrowException(homeTeam, awayTeam);
        return updateMatchScore(match.getMatchId(), homeTeamScore, awayTeamScore);
    }

    @Override
    public MatchDetails finishMatch(int matchId) {
        var writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            verifyMatchIdRegistered(matchId);
            var match = matchIdToMatchDetailsMapping.get(matchId);
            var updatedMatch = match.toBuilder()
                    .matchStatus(MatchStatus.FINISHED)
                    .lastUpdatedTimestamp(Instant.now(clock))
                    .build();
            matchIdToMatchDetailsMapping.put(matchId, updatedMatch);
            var concatenatedTeamIds = concatenateTeamIds(match.getHomeTeam(), match.getAwayTeam());
            matchTeamIdsToMatchDetailsMapping.put(concatenatedTeamIds, updatedMatch);
            return updatedMatch;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public MatchDetails finishMatch(int homeTeamId, int awayTeamId) {
        var homeTeam = teamRegistrar.getTeam(homeTeamId);
        var awayTeam = teamRegistrar.getTeam(awayTeamId);
        var match = getMatchDetailsOrThrowException(homeTeam, awayTeam);
        return finishMatch(match.getMatchId());
    }

    @Override
    public MatchDetails finishMatch(String homeTeamName, String awayTeamName) {
        var homeTeam = teamRegistrar.getTeam(homeTeamName);
        var awayTeam = teamRegistrar.getTeam(awayTeamName);
        var match = getMatchDetailsOrThrowException(homeTeam, awayTeam);
        return finishMatch(match.getMatchId());
    }

    @Override
    public MatchDetails unregisterMatch(int matchId) {
        var writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            verifyMatchIdRegistered(matchId);
            var removedMatch = matchIdToMatchDetailsMapping.remove(matchId);
            var concatenatedTeamIds = concatenateTeamIds(removedMatch.getHomeTeam(), removedMatch.getAwayTeam());
            matchTeamIdsToMatchDetailsMapping.remove(concatenatedTeamIds);
            return removedMatch.toBuilder()
                    .matchStatus(MatchStatus.UNREGISTERED)
                    .lastUpdatedTimestamp(Instant.now(clock))
                    .build();
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public MatchDetails unregisterMatch(int homeTeamId, int awayTeamId) {
        var homeTeam = teamRegistrar.getTeam(homeTeamId);
        var awayTeam = teamRegistrar.getTeam(awayTeamId);
        var match = getMatchDetailsOrThrowException(homeTeam, awayTeam);
        return unregisterMatch(match.getMatchId());
    }

    @Override
    public MatchDetails unregisterMatch(String homeTeamName, String awayTeamName) {
        var homeTeam = teamRegistrar.getTeam(homeTeamName);
        var awayTeam = teamRegistrar.getTeam(awayTeamName);
        var match = getMatchDetailsOrThrowException(homeTeam, awayTeam);
        return unregisterMatch(match.getMatchId());
    }

    @Override
    public List<MatchDetails> getAllMatches() {
        var readLock = readWriteLock.readLock();
        readLock.lock();
        var allMatches = new ArrayList<>(matchIdToMatchDetailsMapping.values());
        readLock.unlock();
        return allMatches;
    }

    @Override
    public List<MatchDetails> getActiveMatches() {
        var readLock = readWriteLock.readLock();
        readLock.lock();
        var activeMatches = matchIdToMatchDetailsMapping.values()
                .stream()
                .filter(match -> match.getMatchStatus() == MatchStatus.IN_PROGRESS)
                .toList();
        readLock.unlock();
        return activeMatches;
    }

    @Override
    public boolean isMatchRegistered(int matchId) {
        var readLock = readWriteLock.readLock();
        readLock.lock();
        var isRegistered = matchIdToMatchDetailsMapping.containsKey(matchId);
        readLock.unlock();
        return isRegistered;
    }

    @Override
    public boolean isMatchRegistered(int homeTeamId, int awayTeamId) {
        var homeTeam = teamRegistrar.getTeam(homeTeamId);
        var awayTeam = teamRegistrar.getTeam(awayTeamId);
        return isMatchRegistered(homeTeam, awayTeam);
    }

    @Override
    public boolean isMatchRegistered(String homeTeamName, String awayTeamName) {
        var homeTeam = teamRegistrar.getTeam(homeTeamName);
        var awayTeam = teamRegistrar.getTeam(awayTeamName);
        return isMatchRegistered(homeTeam, awayTeam);
    }

    private boolean isMatchRegistered(Team homeTeam, Team awayTeam) {
        var readLock = readWriteLock.readLock();
        readLock.lock();
        var concatenatedTeamIds = concatenateTeamIds(homeTeam, awayTeam);
        var isRegistered = matchTeamIdsToMatchDetailsMapping.containsKey(concatenatedTeamIds);
        readLock.unlock();
        return isRegistered;
    }

    private String concatenateTeamIds(Team homeTeam, Team awayTeam) {
        return format(CONCATENATED_TEAM_IDS_TEMPLATE, homeTeam.getId(), awayTeam.getId());
    }

    private void verifyMatchIdRegistered(int matchId) {
        if (!matchIdToMatchDetailsMapping.containsKey(matchId)) {
            throw new MatchNotRegisteredException(matchId);
        }
    }

    private MatchDetails getMatchDetailsOrThrowException(Team homeTeam, Team awayTeam) {
        var concatenatedTeamIds = concatenateTeamIds(homeTeam, awayTeam);
        var matchDetails = matchTeamIdsToMatchDetailsMapping.get(concatenatedTeamIds);
        if (isNull(matchDetails)) {
            throw new MatchNotRegisteredException(homeTeam.getName(), awayTeam.getName());
        }
        return matchDetails;
    }

}
