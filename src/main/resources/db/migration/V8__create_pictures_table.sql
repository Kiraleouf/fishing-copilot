-- Create pictures table
CREATE TABLE pictures (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    fishing_session_id INTEGER NOT NULL,
    CONSTRAINT fk_fishing_session FOREIGN KEY (fishing_session_id)
        REFERENCES fishing_session (id) ON DELETE CASCADE
);

