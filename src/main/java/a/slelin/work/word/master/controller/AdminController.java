package a.slelin.work.word.master.controller;

import a.slelin.work.word.master.dto.admin.AdminUpdateUserRoleRequest;
import a.slelin.work.word.master.dto.general.SheetResponse;
import a.slelin.work.word.master.dto.language.LanguageRequest;
import a.slelin.work.word.master.dto.language.LanguageResponse;
import a.slelin.work.word.master.dto.tag.TagRequest;
import a.slelin.work.word.master.dto.tag.TagResponse;
import a.slelin.work.word.master.dto.user.UserResponse;
import a.slelin.work.word.master.service.LanguageService;
import a.slelin.work.word.master.service.TagService;
import a.slelin.work.word.master.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@Tag(name = "Admin", description = "Users, languages and tags management (role ADMIN)")
public class AdminController {

    private final UserService userService;

    private final LanguageService languageService;

    private final TagService tagService;

    @GetMapping("/users")
    @Operation(summary = "All users")
    public SheetResponse<UserResponse> getUsers(@PageableDefault(size = 20, sort = "createdAt",
            direction = Sort.Direction.DESC) Pageable pageable) {
        return userService.getAll(pageable);
    }

    @PatchMapping("/users/{id}/role")
    @Operation(summary = "Change role of a user")
    public UserResponse updateRole(@PathVariable UUID id, @RequestBody @Valid AdminUpdateUserRoleRequest request) {
        return userService.updateRole(id, request);
    }

    @DeleteMapping("/users/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a user")
    public void deleteUser(@PathVariable UUID id) {
        userService.delete(id);
    }

    @PostMapping("/languages")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a language")
    public LanguageResponse createLanguage(@RequestBody @Valid LanguageRequest request) {
        return languageService.create(request);
    }

    @PatchMapping("/languages/{id}")
    @Operation(summary = "Change a language")
    public LanguageResponse updateLanguage(@PathVariable Long id, @RequestBody @Valid LanguageRequest request) {
        return languageService.update(id, request);
    }

    @DeleteMapping("/languages/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a language that is not used by decks")
    public void deleteLanguage(@PathVariable Long id) {
        languageService.delete(id);
    }

    @PostMapping("/tags")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Add a tag")
    public TagResponse createTag(@RequestBody @Valid TagRequest request) {
        return tagService.create(request);
    }

    @PatchMapping("/tags/{id}")
    @Operation(summary = "Rename a tag")
    public TagResponse updateTag(@PathVariable Long id, @RequestBody @Valid TagRequest request) {
        return tagService.update(id, request);
    }

    @DeleteMapping("/tags/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete a tag")
    public void deleteTag(@PathVariable Long id) {
        tagService.delete(id);
    }
}
