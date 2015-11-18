CREATE TABLE people (
        userid VARCHAR(50) NOT NULL,
        name VARCHAR(50) NOT NULL,
        password VARCHAR(50) NOT NULL,
        homephone VARCHAR(15) NOT NULL,
        mail VARCHAR(100),
        description VARCHAR(250)
        );

CREATE TABLE groups (
        userid VARCHAR(50) NOT NULL,
        name VARCHAR(50) NOT NULL
        );