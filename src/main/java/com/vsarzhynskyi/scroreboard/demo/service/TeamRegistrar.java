package com.vsarzhynskyi.scroreboard.demo.service;

import com.vsarzhynskyi.scroreboard.demo.model.Team;

public interface TeamRegistrar {

    Team registerTeam(String teamName);
    void unregisterTeam(String teamName);
    Team getTeam(String teamName);

}
