package com.doron.shaul.nba.repository;

import com.doron.shaul.nba.model.PlayerGameStats;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class PlayerStatsRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public List<Long> savePlayerStats(List<PlayerGameStats> statsList) {
        String sql = "INSERT INTO player_game_stats (game_id, player_id, stat_points, stat_rebounds, " +
                "stat_assists, stat_steals, stat_blocks, stat_fouls, stat_turnovers, stat_minutes_played) " +
                "VALUES (:gameId, :playerId, :points, :rebounds, :assists, :steals, :blocks, :fouls, " +
                ":turnovers, :minutesPlayed)";

        SqlParameterSource[] batchParams = statsList.stream()
                .map(stats -> new MapSqlParameterSource()
                        .addValue("gameId", stats.getGameId())
                        .addValue("playerId", stats.getPlayerId())
                        .addValue("points", stats.getPoints())
                        .addValue("rebounds", stats.getRebounds())
                        .addValue("assists", stats.getAssists())
                        .addValue("steals", stats.getSteals())
                        .addValue("blocks", stats.getBlocks())
                        .addValue("fouls", stats.getFouls())
                        .addValue("turnovers", stats.getTurnovers())
                        .addValue("minutesPlayed", stats.getMinutesPlayed()))
                .toArray(SqlParameterSource[]::new);

        KeyHolder keyHolder = new GeneratedKeyHolder();
        jdbcTemplate.batchUpdate(sql, batchParams, keyHolder);

        // Extract and return generated keys
        return keyHolder.getKeyList().stream()
                .map(keyMap -> ((Number) keyMap.get("GENERATED_KEY")).longValue())
                .collect(Collectors.toList());
    }
}