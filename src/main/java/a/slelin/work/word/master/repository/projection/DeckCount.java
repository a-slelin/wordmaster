package a.slelin.work.word.master.repository.projection;

import java.util.UUID;

/**
 * Aggregated count grouped by deck.
 */
public record DeckCount(UUID deckId, Long count) {
}
