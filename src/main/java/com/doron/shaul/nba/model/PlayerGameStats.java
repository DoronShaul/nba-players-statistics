package com.doron.shaul.nba.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class PlayerGameStats {

    private Long statId;

    @NotNull(message = "Game ID is required")
    private Long gameId;

    @NotNull(message = "Player ID is required")
    private Long playerId;

    @NotNull(message = "Points value is required")
    @Min(value = 0, message = "Points cannot be negative")
    private Integer points;

    @NotNull(message = "Rebounds value is required")
    @Min(value = 0, message = "Rebounds cannot be negative")
    private Integer rebounds;

    @NotNull(message = "Assists value is required")
    @Min(value = 0, message = "Assists cannot be negative")
    private Integer assists;

    @NotNull(message = "Steals value is required")
    @Min(value = 0, message = "Steals cannot be negative")
    private Integer steals;

    @NotNull(message = "Blocks value is required")
    @Min(value = 0, message = "Blocks cannot be negative")
    private Integer blocks;

    @NotNull(message = "Fouls value is required")
    @Min(value = 0, message = "Fouls cannot be negative")
    @Max(value = 6, message = "Fouls cannot exceed 6")
    private Integer fouls;

    @NotNull(message = "Turnovers value is required")
    @Min(value = 0, message = "Turnovers cannot be negative")
    private Integer turnovers;

    @JsonProperty("minutesPlayed")
    @NotNull(message = "Minutes played value is required")
    @Min(value = 0, message = "Minutes played cannot be negative")
    @Max(value = 48, message = "Minutes played cannot exceed 48.0")
    private Double minutesPlayed;
}