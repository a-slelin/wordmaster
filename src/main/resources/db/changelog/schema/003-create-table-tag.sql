--liquibase formatted sql

--changeset a.slelin:003-create-table-tag

DROP TABLE IF EXISTS tag CASCADE;

CREATE TABLE tag
(
    id   BIGINT PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

--rollback DROP TABLE IF EXISTS tag CASCADE