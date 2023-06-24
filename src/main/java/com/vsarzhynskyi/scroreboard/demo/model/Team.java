package com.vsarzhynskyi.scroreboard.demo.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
@AllArgsConstructor
public class Team {
    int id;
    String name;
}
