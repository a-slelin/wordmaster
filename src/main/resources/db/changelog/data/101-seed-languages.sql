--liquibase formatted sql

--changeset a.slelin:101-seed-languages

INSERT INTO language (code, name, flag)
VALUES ('ru', 'Russian', '🇷🇺'),
       ('en', 'English', '🇬🇧'),
       ('es', 'Spanish', '🇪🇸'),
       ('de', 'German', '🇩🇪'),
       ('fr', 'French', '🇫🇷'),
       ('it', 'Italian', '🇮🇹'),
       ('pt', 'Portuguese', '🇵🇹'),
       ('tr', 'Turkish', '🇹🇷'),
       ('zh', 'Chinese', '🇨🇳'),
       ('ja', 'Japanese', '🇯🇵'),
       ('ko', 'Korean', '🇰🇷');

--rollback DELETE FROM language;
