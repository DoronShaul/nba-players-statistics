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


-- Insert sample seasons
INSERT INTO seasons (season_name, start_date, end_date) VALUES
('2023-24', '2023-10-24', '2024-06-15'),
('2022-23', '2022-10-18', '2023-06-12');

-- Insert sample teams
INSERT INTO teams (team_name, team_abbreviation, team_conference) VALUES
('Los Angeles Lakers', 'LAL', 'WEST'),
('Boston Celtics', 'BOS', 'EAST'),
('Golden State Warriors', 'GSW', 'WEST'),
('Miami Heat', 'MIA', 'EAST'),
('Denver Nuggets', 'DEN', 'WEST'),
('Milwaukee Bucks', 'MIL', 'EAST');

-- Insert sample players
INSERT INTO players (player_first_name, player_last_name, player_jersey_number, player_position) VALUES
('LeBron', 'James', 23, 'SF'),
('Anthony', 'Davis', 3, 'PF'),
('Jayson', 'Tatum', 0, 'SF'),
('Jaylen', 'Brown', 7, 'SG'),
('Stephen', 'Curry', 30, 'PG'),
('Klay', 'Thompson', 11, 'SG'),
('Jimmy', 'Butler', 22, 'SF'),
('Bam', 'Adebayo', 13, 'C'),
('Nikola', 'Jokic', 15, 'C'),
('Jamal', 'Murray', 27, 'PG'),
('Giannis', 'Antetokounmpo', 34, 'PF'),
('Khris', 'Middleton', 22, 'SF');

-- Associate players with teams for the 2023-24 season
INSERT INTO player_teams (player_id, team_id, season_id, join_date) VALUES
-- Lakers players
(1, 1, 1, '2023-10-01'), -- LeBron - Lakers
(2, 1, 1, '2023-10-01'), -- Davis - Lakers
-- Celtics players
(3, 2, 1, '2023-10-01'), -- Tatum - Celtics
(4, 2, 1, '2023-10-01'), -- Brown - Celtics
-- Warriors players
(5, 3, 1, '2023-10-01'), -- Curry - Warriors
(6, 3, 1, '2023-10-01'), -- Thompson - Warriors
-- Heat players
(7, 4, 1, '2023-10-01'), -- Butler - Heat
(8, 4, 1, '2023-10-01'), -- Adebayo - Heat
-- Nuggets players
(9, 5, 1, '2023-10-01'), -- Jokic - Nuggets
(10, 5, 1, '2023-10-01'), -- Murray - Nuggets
-- Bucks players
(11, 6, 1, '2023-10-01'), -- Giannis - Bucks
(12, 6, 1, '2023-10-01'); -- Middleton - Bucks

-- Associate players with teams for the 2022-23 season
INSERT INTO player_teams (player_id, team_id, season_id, join_date, leave_date) VALUES
-- Same team associations for previous season
(1, 1, 2, '2022-10-01', '2023-06-30'),
(2, 1, 2, '2022-10-01', '2023-06-30'),
(3, 2, 2, '2022-10-01', '2023-06-30'),
(4, 2, 2, '2022-10-01', '2023-06-30'),
(5, 3, 2, '2022-10-01', '2023-06-30'),
(6, 3, 2, '2022-10-01', '2023-06-30'),
(7, 4, 2, '2022-10-01', '2023-06-30'),
(8, 4, 2, '2022-10-01', '2023-06-30'),
(9, 5, 2, '2022-10-01', '2023-06-30'),
(10, 5, 2, '2022-10-01', '2023-06-30'),
(11, 6, 2, '2022-10-01', '2023-06-30'),
(12, 6, 2, '2022-10-01', '2023-06-30');

-- Insert sample games for the 2023-24 season
INSERT INTO games (game_date, home_team_id, away_team_id, season_id, game_is_playoff) VALUES
-- Regular season games
('2023-11-15', 1, 2, 1, FALSE), -- Lakers vs Celtics
('2023-11-22', 3, 1, 1, FALSE), -- Warriors vs Lakers
('2023-12-05', 2, 4, 1, FALSE), -- Celtics vs Heat
('2023-12-12', 5, 6, 1, FALSE), -- Nuggets vs Bucks
('2023-12-25', 1, 3, 1, FALSE), -- Lakers vs Warriors (Christmas game)
('2024-01-08', 4, 5, 1, FALSE), -- Heat vs Nuggets
('2024-01-15', 6, 2, 1, FALSE), -- Bucks vs Celtics
('2024-02-02', 3, 5, 1, FALSE), -- Warriors vs Nuggets
('2024-02-10', 4, 6, 1, FALSE), -- Heat vs Bucks
('2024-03-01', 2, 1, 1, FALSE); -- Celtics vs Lakers

-- Insert sample games for the 2022-23 season
INSERT INTO games (game_date, home_team_id, away_team_id, season_id, game_is_playoff) VALUES
('2022-11-10', 2, 1, 2, FALSE), -- Celtics vs Lakers
('2022-12-20', 3, 5, 2, FALSE), -- Warriors vs Nuggets
('2023-01-05', 6, 4, 2, FALSE), -- Bucks vs Heat
('2023-02-15', 1, 5, 2, FALSE), -- Lakers vs Nuggets
('2023-03-10', 4, 3, 2, FALSE); -- Heat vs Warriors