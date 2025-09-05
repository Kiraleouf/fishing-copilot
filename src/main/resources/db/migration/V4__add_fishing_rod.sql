CREATE TABLE fishing_rod (
    id SERIAL PRIMARY KEY,
    fish_count INT NOT NULL DEFAULT 0,
    session_id INT NOT NULL REFERENCES fishing_session(id) ON DELETE CASCADE
);
