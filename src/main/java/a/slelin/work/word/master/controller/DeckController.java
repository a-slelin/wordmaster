package a.slelin.work.word.master.controller;

import a.slelin.work.word.master.dto.card.*;
import a.slelin.work.word.master.dto.deck.*;
import a.slelin.work.word.master.dto.general.SheetResponse;
import a.slelin.work.word.master.security.Actor;
import a.slelin.work.word.master.service.CardService;
import a.slelin.work.word.master.service.DeckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/decks")
@Tag(name = "Decks", description = "Own decks, public catalog, copies and likes")
public class DeckController {

    private final DeckService deckService;

    private final CardService cardService;

    @GetMapping
    @Operation(summary = "Own decks with learning progress")
    public SheetResponse<DeckSummaryResponse> getMyDecks(@AuthenticationPrincipal Jwt jwt,
                                                         @RequestParam(required = false) String search,
                                                         @PageableDefault(size = 50) Pageable pageable) {
        return deckService.getMyDecks(Actor.of(jwt), search, pageable);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a deck")
    public DeckResponse create(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid DeckCreateRequest request) {
        return deckService.create(Actor.of(jwt), request);
    }

    @GetMapping("/public")
    @Operation(summary = "Catalog of public decks (available without authentication)")
    public SheetResponse<PublicDeckCatalogItemResponse> getCatalog(@AuthenticationPrincipal Jwt jwt,
                                                                   @Valid DeckCatalogFilter filter,
                                                                   @PageableDefault(size = 24) Pageable pageable) {
        return deckService.getCatalog(jwt == null ? Actor.anonymous() : Actor.of(jwt), filter, pageable);
    }

    @GetMapping("/public/{id}/cards")
    @Operation(summary = "Preview of the first cards of a public deck")
    public List<CardResponse> getPublicPreview(@PathVariable UUID id,
                                               @RequestParam(defaultValue = "12") int limit) {
        return cardService.getPublicPreview(id, Math.max(1, Math.min(limit, 50)));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Deck details")
    public DeckResponse getById(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return deckService.getById(Actor.of(jwt), id);
    }

    @PatchMapping("/{id}")
    @Operation(summary = "Change a deck")
    public DeckResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                               @RequestBody @Valid DeckUpdateRequest request) {
        return deckService.update(Actor.of(jwt), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a deck with all its cards")
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        deckService.delete(Actor.of(jwt), id);
    }

    @PostMapping("/{id}/copy")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Copy a public deck to own library")
    public DeckCopyResponse copy(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return deckService.copy(Actor.of(jwt), id);
    }

    @PostMapping("/{id}/like")
    @Operation(summary = "Like a public deck")
    public DeckLikeResponse like(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return deckService.like(Actor.of(jwt), id);
    }

    @DeleteMapping("/{id}/like")
    @Operation(summary = "Remove like")
    public DeckLikeResponse unlike(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return deckService.unlike(Actor.of(jwt), id);
    }

    @GetMapping("/{id}/progress")
    @Operation(summary = "Learning progress of the current user in the deck")
    public DeckProgressResponse getProgress(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return deckService.getProgress(Actor.of(jwt), id);
    }

    @GetMapping("/{id}/cards")
    @Operation(summary = "Cards of the deck with the current user's progress")
    public List<CardWithProgressResponse> getCards(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return cardService.getCards(Actor.of(jwt), id);
    }

    @PostMapping("/{id}/cards")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a card")
    public CardResponse createCard(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                   @RequestBody @Valid CardCreateRequest request) {
        return cardService.create(Actor.of(jwt), id, request);
    }

    @PostMapping("/{id}/cards/bulk")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add many cards at once (import)")
    public List<CardResponse> createCards(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                          @RequestBody @Valid CardBulkCreateRequest request) {
        return cardService.createBulk(Actor.of(jwt), id, request);
    }
}
