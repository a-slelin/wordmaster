package a.slelin.work.word.master.service;

import a.slelin.work.word.master.dto.language.LanguageRequest;
import a.slelin.work.word.master.dto.language.LanguageResponse;
import a.slelin.work.word.master.entity.Language;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public interface LanguageService {

    @NotNull
    List<LanguageResponse> getAll();

    @Valid
    @NotNull
    LanguageResponse create(@NotNull @Valid LanguageRequest request);

    @Valid
    @NotNull
    LanguageResponse update(@NotNull Long id, @NotNull @Valid LanguageRequest request);

    void delete(@NotNull Long id);

    @NotNull
    Language getEntityById(@NotNull Long id);
}
