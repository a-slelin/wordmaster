package a.slelin.work.word.master.service;

import a.slelin.work.word.master.dto.stats.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface StatsService {

    @Valid
    @NotNull
    UserStatsResponse getOverview(@NotNull UUID userId);

    @NotNull
    List<@Valid ActivityDayResponse> getActivity(@NotNull UUID userId, @Min(1) @Max(400) int days);

    @NotNull
    List<@Valid ForecastDayResponse> getForecast(@NotNull UUID userId, @Min(1) @Max(60) int days);

    @NotNull
    List<@Valid HardWordResponse> getHardWords(@NotNull UUID userId, UUID deckId, @Min(1) @Max(50) int limit);

    @NotNull
    Optional<WordOfTheDayResponse> getWordOfTheDay(@NotNull UUID userId);
}
