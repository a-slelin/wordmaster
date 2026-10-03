package a.slelin.work.word.master.repository.projection;

import a.slelin.work.word.master.entity.Status;

import java.util.UUID;

/**
 * Aggregated count of card progresses grouped by deck and status.
 */
public record DeckStatusCount(UUID deckId, Status status, Long count) {
}
