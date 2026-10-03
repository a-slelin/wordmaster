package a.slelin.work.word.master.controller;

import a.slelin.work.word.master.dto.training.*;
import a.slelin.work.word.master.security.Actor;
import a.slelin.work.word.master.service.TrainingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/training/sessions")
@Tag(name = "Training", description = "Training sessions with spaced repetition (SM-2)")
public class TrainingController {

    private final TrainingService trainingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Start a session: modes flashcards/typing/choice/listening, scopes smart/all/hard")
    public TrainingSessionStartResponse start(@AuthenticationPrincipal Jwt jwt,
                                              @RequestBody @Valid TrainingSessionStartRequest request) {
        return trainingService.start(Actor.of(jwt), request);
    }

    @GetMapping("/{id}/next")
    @Operation(summary = "Next card (204 when the session has no cards left)")
    public ResponseEntity<TrainingNextCardResponse> next(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return trainingService.next(Actor.of(jwt), id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @PostMapping("/{id}/answers")
    @Operation(summary = "Answer the card: grade for flashcards, text answer for other modes")
    public TrainingAnswerResponse answer(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id,
                                         @RequestBody @Valid TrainingAnswerRequest request) {
        return trainingService.answer(Actor.of(jwt), id, request);
    }

    @PostMapping("/{id}/finish")
    @Operation(summary = "Finish the session: bonuses, streak and achievements")
    public TrainingSessionFinishResponse finish(@AuthenticationPrincipal Jwt jwt, @PathVariable UUID id) {
        return trainingService.finish(Actor.of(jwt), id);
    }
}
