CREATE TABLE user_session (
    session_id UUID PRIMARY KEY,
    user_id INT NOT NULL REFERENCES fisherman(id)
);

CREATE TABLE fishing_session (
    id SERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    user_id INT NOT NULL REFERENCES fisherman(id)
);
