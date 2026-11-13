--liquibase formatted sql

--changeset a.slelin:003-create-table-tag

CREATE TABLE tag
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

--rollback DROP TABLE IF EXISTS tag CASCADE
