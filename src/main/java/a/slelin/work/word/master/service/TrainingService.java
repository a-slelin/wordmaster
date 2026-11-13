package a.slelin.work.word.master.service;

import a.slelin.work.word.master.dto.training.*;
import a.slelin.work.word.master.security.Actor;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Optional;
import java.util.UUID;

public interface TrainingService {

    @Valid
    @NotNull
    TrainingSessionStartResponse start(@NotNull Actor actor, @NotNull @Valid TrainingSessionStartRequest request);

    /**
     * Next card of the session or empty if all cards are answered.
     */
    @NotNull
    Optional<TrainingNextCardResponse> next(@NotNull Actor actor, @NotNull UUID sessionId);

    @Valid
    @NotNull
    TrainingAnswerResponse answer(@NotNull Actor actor, @NotNull UUID sessionId,
                                  @NotNull @Valid TrainingAnswerRequest request);

    @Valid
    @NotNull
    TrainingSessionFinishResponse finish(@NotNull Actor actor, @NotNull UUID sessionId);
}
