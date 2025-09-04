CREATE TABLE fisherman (
    id SERIAL PRIMARY KEY,
    login TEXT UNIQUE NOT NULL,
    password TEXT NOT NULL,
    secret_question TEXT NOT NULL,
    secret_answer TEXT NOT NULL
);
