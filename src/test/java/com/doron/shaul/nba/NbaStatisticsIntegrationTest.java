package com.doron.shaul.nba;

import com.doron.shaul.nba.model.PlayerGameStats;
import com.doron.shaul.nba.model.PlayerSeasonStats;
import com.doron.shaul.nba.model.TeamSeasonStats;
import com.doron.shaul.nba.service.StatsService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.context.annotation.Import;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@Import(TestRedisConfig.class)
public class NbaStatisticsIntegrationTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private StatsService statsService;

    @Container
    static MySQLContainer<?> mysqlContainer = new MySQLContainer<>(DockerImageName.parse("mysql:8.0"))
            .withDatabaseName("nba_stats_test")
            .withUsername("test")
            .withPassword("test")
            .withInitScript("init.sql");

    @Container
    static GenericContainer<?> redisContainer = new GenericContainer<>(DockerImageName.parse("redis:7.0"))
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", mysqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", mysqlContainer::getUsername);
        registry.add("spring.datasource.password", mysqlContainer::getPassword);
        registry.add("spring.redis.host", redisContainer::getHost);
        registry.add("spring.redis.port", () -> redisContainer.getMappedPort(6379));
    }

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void testRecordAndRetrievePlayerStats() {
        long gameId = 1L;
        String url = "http://localhost:" + port + "/api/v1/games/" + gameId + "/stats";

        PlayerGameStats stats1 = new PlayerGameStats();
        stats1.setPlayerId(1L);
        stats1.setGameId(1L);
        stats1.setPoints(20);
        stats1.setRebounds(5);
        stats1.setAssists(10);
        stats1.setSteals(2);
        stats1.setBlocks(1);
        stats1.setFouls(3);
        stats1.setTurnovers(1);
        stats1.setMinutesPlayed(32.5);

        PlayerGameStats stats2 = new PlayerGameStats();
        stats2.setPlayerId(2L);
        stats2.setGameId(1L);
        stats2.setPoints(15);
        stats2.setRebounds(7);
        stats2.setAssists(3);
        stats2.setSteals(1);
        stats2.setBlocks(0);
        stats2.setFouls(2);
        stats2.setTurnovers(2);
        stats2.setMinutesPlayed(28.0);

        List<PlayerGameStats> statsList = Arrays.asList(stats1, stats2);

        ResponseEntity<List<Long>> recordResponse = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(statsList),
                new ParameterizedTypeReference<>() {
                }
        );

        assertEquals(HttpStatus.CREATED, recordResponse.getStatusCode());
        assertNotNull(recordResponse.getBody());
        assertEquals(2, recordResponse.getBody().size());

        String playerStatsUrl = "http://localhost:" + port + "/api/v1/players/1/stats/averages?seasonId=1";
        ResponseEntity<PlayerSeasonStats> playerStatsResponse = restTemplate.getForEntity(
                playerStatsUrl,
                PlayerSeasonStats.class
        );

        assertEquals(HttpStatus.OK, playerStatsResponse.getStatusCode());
        assertNotNull(playerStatsResponse.getBody());
        assertEquals(1L, playerStatsResponse.getBody().getPlayerId());

        String teamStatsUrl = "http://localhost:" + port + "/api/v1/teams/1/stats/averages?seasonId=1";
        ResponseEntity<List<TeamSeasonStats>> teamStatsResponse = restTemplate.exchange(
                teamStatsUrl,
                HttpMethod.GET,
                null,
                new ParameterizedTypeReference<>() {
                }
        );

        assertEquals(HttpStatus.OK, teamStatsResponse.getStatusCode());
        assertNotNull(teamStatsResponse.getBody());
    }
}