package com.doron.shaul.nba.controller;

import com.doron.shaul.nba.model.PlayerGameStats;
import com.doron.shaul.nba.model.PlayerSeasonStats;
import com.doron.shaul.nba.model.TeamSeasonStats;
import com.doron.shaul.nba.service.StatsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class StatsController {

    private final StatsService statsService;

    @PostMapping("/games/{gameId}/stats")
    public ResponseEntity<List<Long>> recordPlayerStats(
            @PathVariable long gameId,
            @Valid @RequestBody List<PlayerGameStats> statsList) {

        statsList.forEach(stats -> stats.setGameId(gameId));

        List<Long> statIds = statsService.recordPlayerStats(statsList);

        return ResponseEntity.status(HttpStatus.CREATED).body(statIds);
    }

    @GetMapping("/players/{playerId}/stats/averages")
    public ResponseEntity<PlayerSeasonStats> getPlayerSeasonStats(
            @PathVariable long playerId,
            @RequestParam long seasonId) {

        PlayerSeasonStats stats = statsService.getPlayerSeasonStats(playerId, seasonId);
        return ResponseEntity.ok(stats);
    }

    @GetMapping("/teams/{teamId}/stats/averages")
    public ResponseEntity<List<TeamSeasonStats>> getTeamSeasonStats(
            @PathVariable long teamId,
            @RequestParam long seasonId) {

        List<TeamSeasonStats> stats = statsService.getTeamSeasonStats(teamId, seasonId);
        return ResponseEntity.ok(stats);
    }
}