--liquibase formatted sql

--changeset a.slelin:001-create-table-language

CREATE TABLE language
(
    id   BIGSERIAL PRIMARY KEY,
    code VARCHAR(10)  NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL,
    flag VARCHAR(16)
);

--rollback DROP TABLE IF EXISTS language CASCADE
