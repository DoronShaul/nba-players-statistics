package com.doron.shaul.nba.repository;

import com.doron.shaul.nba.model.PlayerSeasonStats;
import com.doron.shaul.nba.model.TeamSeasonStats;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class StatsAggregationRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;

    public PlayerSeasonStats getPlayerSeasonStats(long playerId, long seasonId) {
        String sql = "SELECT * FROM player_season_averages WHERE player_id = :playerId AND season_id = :seasonId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("playerId", playerId)
                .addValue("seasonId", seasonId);

        return jdbcTemplate.queryForObject(sql, params,
                new BeanPropertyRowMapper<>(PlayerSeasonStats.class));
    }

    public List<TeamSeasonStats> getTeamSeasonStats(long teamId, long seasonId) {
        String sql = "SELECT * FROM team_season_averages WHERE team_id = :teamId AND season_id = :seasonId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("teamId", teamId)
                .addValue("seasonId", seasonId);

        return jdbcTemplate.query(sql, params,
                new BeanPropertyRowMapper<>(TeamSeasonStats.class));
    }
}