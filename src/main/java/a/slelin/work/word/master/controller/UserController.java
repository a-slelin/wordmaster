package a.slelin.work.word.master.controller;

import a.slelin.work.word.master.dto.stats.UserStatsResponse;
import a.slelin.work.word.master.dto.user.*;
import a.slelin.work.word.master.security.CurrentUser;
import a.slelin.work.word.master.service.GamificationService;
import a.slelin.work.word.master.service.StatsService;
import a.slelin.work.word.master.service.UserService;
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
@RequestMapping("/api/users")
@Tag(name = "Users", description = "Profile of the current user")
public class UserController {

    private final UserService userService;

    private final GamificationService gamificationService;

    private final StatsService statsService;

    @GetMapping("/me")
    @Operation(summary = "Current user")
    public UserResponse me(@AuthenticationPrincipal Jwt jwt) {
        return userService.getById(CurrentUser.id(jwt));
    }

    @PatchMapping("/me")
    @Operation(summary = "Change username and/or email")
    public UserResponse update(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid UserRequest request) {
        return userService.update(CurrentUser.id(jwt), request);
    }

    @PutMapping("/me/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Change password")
    public void changePassword(@AuthenticationPrincipal Jwt jwt, @RequestBody @Valid ChangePasswordRequest request) {
        userService.changePassword(CurrentUser.id(jwt), request);
    }

    @PutMapping("/me/settings")
    @Operation(summary = "Change learning settings (daily goal)")
    public UserStatsResponse updateSettings(@AuthenticationPrincipal Jwt jwt,
                                            @RequestBody @Valid UserSettingsRequest request) {
        UUID userId = CurrentUser.id(jwt);
        gamificationService.updateDailyGoal(userId, request.dailyGoal());
        return statsService.getOverview(userId);
    }

    @DeleteMapping("/me")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete own account with all decks and progress")
    public void delete(@AuthenticationPrincipal Jwt jwt) {
        userService.delete(CurrentUser.id(jwt));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Public profile of a user")
    public UserPublicResponse getPublic(@PathVariable UUID id) {
        return userService.getPublicById(id);
    }
}
