package a.slelin.work.word.master.controller;

import a.slelin.work.word.master.dto.stats.*;
import a.slelin.work.word.master.security.CurrentUser;
import a.slelin.work.word.master.service.GamificationService;
import a.slelin.work.word.master.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/stats")
@Tag(name = "Stats", description = "Progress, streaks, achievements and fun features")
public class StatsController {

    private final StatsService statsService;

    private final GamificationService gamificationService;

    @GetMapping("/overview")
    @Operation(summary = "Totals, XP, level, streak and daily goal")
    public UserStatsResponse overview(@AuthenticationPrincipal Jwt jwt) {
        return statsService.getOverview(CurrentUser.id(jwt));
    }

    @GetMapping("/activity")
    @Operation(summary = "Reviewed cards per day (for the heatmap)")
    public List<ActivityDayResponse> activity(@AuthenticationPrincipal Jwt jwt,
                                              @RequestParam(defaultValue = "182") int days) {
        return statsService.getActivity(CurrentUser.id(jwt), days);
    }

    @GetMapping("/forecast")
    @Operation(summary = "Number of reviews planned for the next days")
    public List<ForecastDayResponse> forecast(@AuthenticationPrincipal Jwt jwt,
                                              @RequestParam(defaultValue = "14") int days) {
        return statsService.getForecast(CurrentUser.id(jwt), days);
    }

    @GetMapping("/hard-words")
    @Operation(summary = "Cards with the highest error rate")
    public List<HardWordResponse> hardWords(@AuthenticationPrincipal Jwt jwt,
                                            @RequestParam(required = false) UUID deckId,
                                            @RequestParam(defaultValue = "10") int limit) {
        return statsService.getHardWords(CurrentUser.id(jwt), deckId, limit);
    }

    @GetMapping("/word-of-the-day")
    @Operation(summary = "Word of the day (204 if there are no cards at all)")
    public ResponseEntity<WordOfTheDayResponse> wordOfTheDay(@AuthenticationPrincipal Jwt jwt) {
        return statsService.getWordOfTheDay(CurrentUser.id(jwt))
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }

    @GetMapping("/achievements")
    @Operation(summary = "All achievements with progress")
    public List<AchievementResponse> achievements(@AuthenticationPrincipal Jwt jwt) {
        return gamificationService.getAchievements(CurrentUser.id(jwt));
    }
}
