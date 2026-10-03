package a.slelin.work.word.master.service;

import a.slelin.work.word.master.dto.deck.*;
import a.slelin.work.word.master.dto.general.SheetResponse;
import a.slelin.work.word.master.entity.Deck;
import a.slelin.work.word.master.security.Actor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

public interface DeckService {

    @Valid
    @NotNull
    SheetResponse<DeckSummaryResponse> getMyDecks(@NotNull Actor actor, String search, @NotNull Pageable pageable);

    @Valid
    @NotNull
    SheetResponse<PublicDeckCatalogItemResponse> getCatalog(@NotNull Actor actor,
                                                            @NotNull @Valid DeckCatalogFilter filter,
                                                            @NotNull Pageable pageable);

    @Valid
    @NotNull
    DeckResponse getById(@NotNull Actor actor, @NotNull UUID deckId);

    @Valid
    @NotNull
    DeckResponse create(@NotNull Actor actor, @NotNull @Valid DeckCreateRequest request);

    @Valid
    @NotNull
    DeckResponse update(@NotNull Actor actor, @NotNull UUID deckId, @NotNull @Valid DeckUpdateRequest request);

    void delete(@NotNull Actor actor, @NotNull UUID deckId);

    @Valid
    @NotNull
    DeckCopyResponse copy(@NotNull Actor actor, @NotNull UUID deckId);

    @Valid
    @NotNull
    DeckLikeResponse like(@NotNull Actor actor, @NotNull UUID deckId);

    @Valid
    @NotNull
    DeckLikeResponse unlike(@NotNull Actor actor, @NotNull UUID deckId);

    @Valid
    @NotNull
    DeckProgressResponse getProgress(@NotNull Actor actor, @NotNull UUID deckId);

    /**
     * Deck the actor may read: own deck, public deck or any deck for admin.
     */
    @NotNull
    Deck getReadableDeck(@NotNull Actor actor, @NotNull UUID deckId);

    /**
     * Deck the actor may change: own deck or any deck for admin.
     */
    @NotNull
    Deck getEditableDeck(@NotNull Actor actor, @NotNull UUID deckId);
}
