DROP TABLE IF EXISTS users;
CREATE TABLE IF NOT EXISTS users (
                                     id SERIAL PRIMARY KEY,
                                     username VARCHAR(31) NOT NULL
    );