--liquibase formatted sql

--changeset a.slelin:001-create-table-language

DROP TABLE IF EXISTS language CASCADE;

CREATE TABLE language
(
    id   BIGSERIAL PRIMARY KEY,
    code VARCHAR(255) NOT NULL UNIQUE,
    name VARCHAR(255) NOT NULL
);

--rollback DROP TABLE IF EXISTS language CASCADE