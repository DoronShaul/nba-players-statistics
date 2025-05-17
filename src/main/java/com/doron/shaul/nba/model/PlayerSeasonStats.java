package com.doron.shaul.nba.model;

import lombok.Data;

@Data
public class PlayerSeasonStats {
    private Long playerId;
    private String firstName;
    private String lastName;
    private Long seasonId;
    private String seasonName;
    private Long teamId;
    private String teamName;
    private Double avgPoints;
    private Double avgRebounds;
    private Double avgAssists;
    private Double avgSteals;
    private Double avgBlocks;
    private Double avgFouls;
    private Double avgTurnovers;
    private Double avgMinutesPlayed;
    private Integer gamesPlayed;
}