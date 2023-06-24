package com.vsarzhynskyi.scroreboard.demo.service.team;

import com.vsarzhynskyi.scroreboard.demo.exception.TeamAlreadyRegisteredException;
import com.vsarzhynskyi.scroreboard.demo.exception.TeamNotRegisteredException;
import com.vsarzhynskyi.scroreboard.demo.model.Team;
import com.vsarzhynskyi.scroreboard.demo.service.IdGenerator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

import static java.util.Objects.isNull;

public class DefaultTeamRegistrar implements TeamRegistrar {

    private final IdGenerator teamIdGenerator;

    private final Map<Integer, Team> teamIdToTeamMapping;
    private final Map<String, Team> teamNameToIdMapping;
    private final ReadWriteLock readWriteLock;

    public DefaultTeamRegistrar(IdGenerator teamIdGenerator) {
        this.teamIdGenerator = teamIdGenerator;
        this.readWriteLock = new ReentrantReadWriteLock();

        teamIdToTeamMapping = new ConcurrentHashMap<>();
        teamNameToIdMapping = new ConcurrentHashMap<>();
    }

    @Override
    public Team registerTeam(String teamName) {
        var writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            if (teamNameToIdMapping.containsKey(teamName)) {
                throw new TeamAlreadyRegisteredException(teamName);
            }

            var teamId = teamIdGenerator.nextId();
            var team = Team.builder()
                    .id(teamId)
                    .name(teamName)
                    .build();
            teamIdToTeamMapping.put(teamId, team);
            teamNameToIdMapping.put(teamName, team);
            return team;
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void unregisterTeam(String teamName) {
        var writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            if (!teamNameToIdMapping.containsKey(teamName)) {
                throw new TeamNotRegisteredException(teamName);
            }
            var removedTeam = teamNameToIdMapping.remove(teamName);
            teamIdToTeamMapping.remove(removedTeam.getId());
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public void unregisterTeam(int teamId) {
        var writeLock = readWriteLock.writeLock();
        writeLock.lock();
        try {
            if (!teamIdToTeamMapping.containsKey(teamId)) {
                throw new TeamNotRegisteredException(teamId);
            }
            var removedTeam = teamIdToTeamMapping.remove(teamId);
            teamNameToIdMapping.remove(removedTeam.getName());
        } finally {
            writeLock.unlock();
        }
    }

    @Override
    public boolean isTeamRegistered(String teamName) {
        var readLock = readWriteLock.readLock();
        readLock.lock();
        var isRegistered = teamNameToIdMapping.containsKey(teamName);
        readLock.unlock();
        return isRegistered;
    }

    @Override
    public boolean isTeamRegistered(int teamId) {
        var readLock = readWriteLock.readLock();
        readLock.lock();
        var isRegistered = teamIdToTeamMapping.containsKey(teamId);
        readLock.unlock();
        return isRegistered;
    }

    @Override
    public Team getTeam(String teamName) {
        var readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            var team = teamNameToIdMapping.get(teamName);
            if (isNull(team)) {
                throw new TeamNotRegisteredException(teamName);
            }
            return team;
        } finally {
            readLock.unlock();
        }
    }

    @Override
    public Team getTeam(int teamId) {
        var readLock = readWriteLock.readLock();
        readLock.lock();
        try {
            var team = teamIdToTeamMapping.get(teamId);
            if (isNull(team)) {
                throw new TeamNotRegisteredException(teamId);
            }
            return team;
        } finally {
            readLock.unlock();
        }
    }

}
