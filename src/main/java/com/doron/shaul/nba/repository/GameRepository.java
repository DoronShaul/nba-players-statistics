package com.doron.shaul.nba.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;

import java.util.HashSet;
import java.util.Set;

@Repository
@RequiredArgsConstructor
public class GameRepository {

    private final NamedParameterJdbcTemplate jdbcTemplate;


    public Long findSeasonIdByGameId(Long gameId) {
        String sql = "SELECT season_id FROM games WHERE game_id = :gameId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("gameId", gameId);

        return jdbcTemplate.queryForObject(sql, params, Long.class);
    }

    public Set<Long> findTeamIdsByGameId(Long gameId) {
        String sql = "SELECT home_team_id, away_team_id FROM games WHERE game_id = :gameId";

        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("gameId", gameId);

        return jdbcTemplate.query(sql, params, rs -> {
            Set<Long> teamIds = new HashSet<>();
            if (rs.next()) {
                teamIds.add(rs.getLong("home_team_id"));
                teamIds.add(rs.getLong("away_team_id"));
            }
            return teamIds;
        });
    }
}
