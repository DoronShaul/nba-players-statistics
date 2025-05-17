CREATE DATABASE IF NOT EXISTS nba_stats;
USE nba_stats;

CREATE TABLE seasons (
    season_id INT AUTO_INCREMENT PRIMARY KEY,
    season_name VARCHAR(9) NOT NULL, -- Format: "2023-24"
    start_date DATE NOT NULL,
    end_date DATE,  -- NULL if season is ongoing
    UNIQUE KEY unique_season_name (season_name)
);

CREATE TABLE teams (
    team_id INT AUTO_INCREMENT PRIMARY KEY,
    team_name VARCHAR(100) NOT NULL,
    team_abbreviation VARCHAR(3) NOT NULL,
    team_conference ENUM('EAST', 'WEST') NOT NULL,
    UNIQUE KEY unique_team_name (team_name),
    UNIQUE KEY unique_abbreviation (team_abbreviation)
);

CREATE TABLE players (
    player_id INT AUTO_INCREMENT PRIMARY KEY,
    player_first_name VARCHAR(50) NOT NULL,
    player_last_name VARCHAR(50) NOT NULL,
    player_jersey_number INT,
    player_position VARCHAR(10)
);

CREATE TABLE player_teams (
    player_team_id INT AUTO_INCREMENT PRIMARY KEY,
    player_id INT NOT NULL,
    team_id INT NOT NULL,
    season_id INT NOT NULL,
    join_date DATE NOT NULL,
    leave_date DATE,  -- NULL means still with the team
    FOREIGN KEY (player_id) REFERENCES players (player_id) ON DELETE CASCADE,
    FOREIGN KEY (team_id) REFERENCES teams (team_id) ON DELETE CASCADE,
    FOREIGN KEY (season_id) REFERENCES seasons (season_id) ON DELETE CASCADE,
    INDEX idx_player (player_id),
    INDEX idx_team (team_id),
    INDEX idx_season (season_id),
    INDEX idx_player_season (player_id, season_id)
);

CREATE TABLE games (
    game_id INT AUTO_INCREMENT PRIMARY KEY,
    game_date DATE NOT NULL,
    home_team_id INT NOT NULL,
    away_team_id INT NOT NULL,
    season_id INT NOT NULL,
    game_is_playoff BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (home_team_id) REFERENCES teams (team_id) ON DELETE CASCADE,
    FOREIGN KEY (away_team_id) REFERENCES teams (team_id) ON DELETE CASCADE,
    FOREIGN KEY (season_id) REFERENCES seasons (season_id) ON DELETE CASCADE,
    INDEX idx_season (season_id),
    INDEX idx_game_date (game_date),
    INDEX idx_teams (home_team_id, away_team_id)
);

CREATE TABLE player_game_stats (
    stat_id INT AUTO_INCREMENT PRIMARY KEY,
    game_id INT NOT NULL,
    player_id INT NOT NULL,
    stat_points INT NOT NULL DEFAULT 0,
    stat_rebounds INT NOT NULL DEFAULT 0,
    stat_assists INT NOT NULL DEFAULT 0,
    stat_steals INT NOT NULL DEFAULT 0,
    stat_blocks INT NOT NULL DEFAULT 0,
    stat_fouls INT NOT NULL DEFAULT 0 CHECK (stat_fouls <= 6),
    stat_turnovers INT NOT NULL DEFAULT 0,
    stat_minutes_played DECIMAL(4,1) NOT NULL DEFAULT 0.0 CHECK (stat_minutes_played >= 0 AND stat_minutes_played <= 48.0),
    FOREIGN KEY (game_id) REFERENCES games (game_id) ON DELETE CASCADE,
    FOREIGN KEY (player_id) REFERENCES players (player_id) ON DELETE CASCADE,
    UNIQUE KEY unique_player_game (player_id, game_id),
    INDEX idx_game (game_id),
    INDEX idx_player (player_id)
);

CREATE OR REPLACE VIEW player_season_averages AS
SELECT
    p.player_id,
    p.player_first_name,
    p.player_last_name,
    s.season_id,
    s.season_name,
    t.team_id,
    t.team_name,
    AVG(pgs.stat_points) AS avg_points,
    AVG(pgs.stat_rebounds) AS avg_rebounds,
    AVG(pgs.stat_assists) AS avg_assists,
    AVG(pgs.stat_steals) AS avg_steals,
    AVG(pgs.stat_blocks) AS avg_blocks,
    AVG(pgs.stat_fouls) AS avg_fouls,
    AVG(pgs.stat_turnovers) AS avg_turnovers,
    AVG(pgs.stat_minutes_played) AS avg_minutes_played,
    COUNT(pgs.stat_id) AS games_played
FROM
    player_game_stats pgs
JOIN
    players p ON pgs.player_id = p.player_id
JOIN
    games g ON pgs.game_id = g.game_id
JOIN
    seasons s ON g.season_id = s.season_id
JOIN
    player_teams pt ON p.player_id = pt.player_id AND s.season_id = pt.season_id
JOIN
    teams t ON pt.team_id = t.team_id
GROUP BY
    p.player_id, p.player_first_name, p.player_last_name, s.season_id, s.season_name, t.team_id, t.team_name;

CREATE OR REPLACE VIEW team_season_averages AS
SELECT
    t.team_id,
    t.team_name,
    s.season_id,
    s.season_name,
    AVG(pgs.stat_points) AS avg_points,
    AVG(pgs.stat_rebounds) AS avg_rebounds,
    AVG(pgs.stat_assists) AS avg_assists,
    AVG(pgs.stat_steals) AS avg_steals,
    AVG(pgs.stat_blocks) AS avg_blocks,
    AVG(pgs.stat_fouls) AS avg_fouls,
    AVG(pgs.stat_turnovers) AS avg_turnovers,
    AVG(pgs.stat_minutes_played) AS avg_minutes_played,
    COUNT(DISTINCT g.game_id) AS games_played,
    COUNT(DISTINCT pgs.player_id) AS players_count
FROM
    teams t
JOIN
    player_teams pt ON t.team_id = pt.team_id
JOIN
    players p ON pt.player_id = p.player_id
JOIN
    player_game_stats pgs ON p.player_id = pgs.player_id
JOIN
    games g ON pgs.game_id = g.game_id
JOIN
    seasons s ON g.season_id = s.season_id AND pt.season_id = s.season_id
GROUP BY
    t.team_id, t.team_name, s.season_id, s.season_name;