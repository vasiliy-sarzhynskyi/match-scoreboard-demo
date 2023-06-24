package com.vsarzhynskyi.scroreboard.demo.service.team;

import com.vsarzhynskyi.scroreboard.demo.model.Team;

public interface TeamRegistrar {

    Team registerTeam(String teamName);
    void unregisterTeam(String teamName);
    void unregisterTeam(int teamId);
    boolean isTeamRegistered(String teamName);
    boolean isTeamRegistered(int teamId);
    Team getTeam(String teamName);
    Team getTeam(int teamId);


}
