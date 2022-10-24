DROP TABLE IF EXISTS users;
CREATE TABLE IF NOT EXISTS users (
                                     id SERIAL PRIMARY KEY,
                                     username VARCHAR(31) NOT NULL
);
INSERT INTO users VALUES (DEFAULT, 'Krabelard');
INSERT INTO users VALUES (DEFAULT, 'Gordon');
INSERT INTO users VALUES (DEFAULT, 'Sysy');
INSERT INTO users VALUES (DEFAULT, 'Szniok');
INSERT INTO users VALUES (DEFAULT, 'Gniok');
INSERT INTO users VALUES (DEFAULT, 'Craig');
INSERT INTO users VALUES (DEFAULT, 'MrZaroweczka');
INSERT INTO users VALUES (DEFAULT, 'Grypsztals');
INSERT INTO users VALUES (DEFAULT, 'Dziok');