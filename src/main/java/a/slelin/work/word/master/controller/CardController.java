package a.slelin.work.word.master.controller;

import a.slelin.work.word.master.dto.card.CardResponse;
import a.slelin.work.word.master.dto.card.CardUpdateRequest;
import a.slelin.work.word.master.security.Actor;
import a.slelin.work.word.master.service.CardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/cards")
@Tag(name = "Cards", description = "Change and delete cards")
public class CardController {

    private final CardService cardService;

    @PatchMapping("/{id}")
    @Operation(summary = "Change a card")
    public CardResponse update(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                               @RequestBody @Valid CardUpdateRequest request) {
        return cardService.update(Actor.of(jwt), id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a card")
    public void delete(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        cardService.delete(Actor.of(jwt), id);
    }
}
