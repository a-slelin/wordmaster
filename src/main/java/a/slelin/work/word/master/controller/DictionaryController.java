package a.slelin.work.word.master.controller;

import a.slelin.work.word.master.dto.language.LanguageResponse;
import a.slelin.work.word.master.dto.tag.TagResponse;
import a.slelin.work.word.master.service.LanguageService;
import a.slelin.work.word.master.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
@Tag(name = "Dictionaries", description = "Languages and tags")
public class DictionaryController {

    private final LanguageService languageService;

    private final TagService tagService;

    @GetMapping("/languages")
    @Operation(summary = "All languages")
    public List<LanguageResponse> getLanguages() {
        return languageService.getAll();
    }

    @GetMapping("/tags")
    @Operation(summary = "All tags")
    public List<TagResponse> getTags() {
        return tagService.getAll();
    }
}
