package com.doron.shaul.nba.model;

import lombok.Data;

@Data
public class TeamSeasonStats {
    private Long teamId;
    private String teamName;
    private Long seasonId;
    private String seasonName;
    private Double avgPoints;
    private Double avgRebounds;
    private Double avgAssists;
    private Double avgSteals;
    private Double avgBlocks;
    private Double avgFouls;
    private Double avgTurnovers;
    private Double avgMinutesPlayed;
    private Integer gamesPlayed;
    private Integer playersCount;
}