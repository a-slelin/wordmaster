package a.slelin.work.word.master.service;

import a.slelin.work.word.master.dto.tag.TagRequest;
import a.slelin.work.word.master.dto.tag.TagResponse;
import a.slelin.work.word.master.entity.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

import java.util.Collection;
import java.util.List;

public interface TagService {

    @NotNull
    List<TagResponse> getAll();

    @Valid
    @NotNull
    TagResponse create(@NotNull @Valid TagRequest request);

    @Valid
    @NotNull
    TagResponse update(@NotNull Long id, @NotNull @Valid TagRequest request);

    void delete(@NotNull Long id);

    @NotNull
    List<Tag> getEntitiesByIds(Collection<Long> ids);
}
