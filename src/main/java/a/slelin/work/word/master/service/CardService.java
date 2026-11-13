package a.slelin.work.word.master.service;

import a.slelin.work.word.master.dto.card.*;
import a.slelin.work.word.master.security.Actor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.UUID;

public interface CardService {

    @NotNull
    List<@Valid CardWithProgressResponse> getCards(@NotNull Actor actor, @NotNull UUID deckId);

    @NotNull
    List<@Valid CardResponse> getPublicPreview(@NotNull UUID deckId, int limit);

    @Valid
    @NotNull
    CardResponse create(@NotNull Actor actor, @NotNull UUID deckId, @NotNull @Valid CardCreateRequest request);

    @NotNull
    List<@Valid CardResponse> createBulk(@NotNull Actor actor, @NotNull UUID deckId,
                                  @NotNull @Valid CardBulkCreateRequest request);

    @Valid
    @NotNull
    CardResponse update(@NotNull Actor actor, @NotNull UUID cardId, @NotNull @Valid CardUpdateRequest request);

    void delete(@NotNull Actor actor, @NotNull UUID cardId);
}
