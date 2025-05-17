package com.doron.shaul.nba.service;

import com.doron.shaul.nba.model.PlayerGameStats;
import com.doron.shaul.nba.model.PlayerSeasonStats;
import com.doron.shaul.nba.model.TeamSeasonStats;
import com.doron.shaul.nba.repository.GameRepository;
import com.doron.shaul.nba.repository.PlayerStatsRepository;
import com.doron.shaul.nba.repository.StatsAggregationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StatsService {

    private final PlayerStatsRepository playerStatsRepository;
    private final StatsAggregationRepository statsAggregationRepository;
    private final GameRepository gameRepository;
    private final CacheManager cacheManager;

    @Transactional
    public List<Long> recordPlayerStats(List<PlayerGameStats> statsList) {

        Set<Long> playerIds = statsList.stream()
                .map(PlayerGameStats::getPlayerId)
                .collect(Collectors.toSet());

        Long gameId = statsList.stream().map(PlayerGameStats::getGameId).findFirst().orElse(null);
        Long seasonId = gameRepository.findSeasonIdByGameId(gameId);
        Set<Long> teamIds = gameRepository.findTeamIdsByGameId(gameId);

        List<Long> result = playerStatsRepository.savePlayerStats(statsList);

        evictPlayerCaches(playerIds, seasonId);
        evictTeamCaches(teamIds, seasonId);

        return result;
    }

    @Cacheable(cacheNames = "playerSeasonStats",
            key = "'player_' + #playerId + '_season_' + #seasonId")
    public PlayerSeasonStats getPlayerSeasonStats(long playerId, long seasonId) {
        return statsAggregationRepository.getPlayerSeasonStats(playerId, seasonId);
    }

    @Cacheable(cacheNames = "teamSeasonStats",
            key = "'team_' + #teamId + '_season_' + #seasonId")
    public List<TeamSeasonStats> getTeamSeasonStats(long teamId, long seasonId) {
        return statsAggregationRepository.getTeamSeasonStats(teamId, seasonId);
    }

    private void evictPlayerCaches(Set<Long> playerIds, Long seasonId) {
        if (playerIds == null || playerIds.isEmpty() || seasonId == null) {
            return;
        }

        for (Long playerId : playerIds) {
            String cacheKey = "player_" + playerId + "_season_" + seasonId;
            cacheManager.getCache("playerSeasonStats").evict(cacheKey);
        }
    }

    private void evictTeamCaches(Set<Long> teamIds, Long seasonId) {
        if (teamIds == null || teamIds.isEmpty() || seasonId == null) {
            return;
        }

        for (Long teamId : teamIds) {
            String cacheKey = "team_" + teamId + "_season_" + seasonId;
            cacheManager.getCache("teamSeasonStats").evict(cacheKey);
        }
    }
}