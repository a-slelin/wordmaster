--liquibase formatted sql

--changeset a.slelin:102-seed-tags

INSERT INTO tag (name)
VALUES ('глаголы'),
       ('грамматика'),
       ('лексика'),
       ('идиомы'),
       ('разговорный'),
       ('путешествия'),
       ('IT'),
       ('бизнес'),
       ('для начинающих'),
       ('A1'),
       ('A2'),
       ('B1'),
       ('B2'),
       ('C1'),
       ('еда'),
       ('сериалы и кино');

--rollback DELETE FROM tag;
