--liquibase formatted sql

--changeset a.slelin:007-create-table-deck-tag

DROP TABLE IF EXISTS deck_tag CASCADE;

CREATE TABLE deck_tag
(
    deck_id UUID   NOT NULL,
    tag_id  BIGINT NOT NULL,
    PRIMARY KEY (deck_id, tag_id),
    CONSTRAINT fk_deck_tag_to_deck FOREIGN KEY (deck_id) REFERENCES deck (id),
    CONSTRAINT fk_deck_tag_to_tag FOREIGN KEY (tag_id) REFERENCES tag (id)
);

--rollback DROP TABLE IF EXISTS deck_tag CASCADE