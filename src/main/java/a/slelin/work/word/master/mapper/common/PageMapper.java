package a.slelin.work.word.master.mapper.common;

import a.slelin.work.word.master.dto.ResponseDto;
import a.slelin.work.word.master.dto.general.PageResponse;
import a.slelin.work.word.master.dto.general.SheetResponse;
import a.slelin.work.word.master.entity.BaseEntity;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PageMapper {

    public static <E extends BaseEntity, D extends ResponseDto> SheetResponse<D> toSheet(Page<E> page, Function<E, D> mapper) {
        if (page == null || mapper == null) {
            throw new IllegalArgumentException("Page and mapper must not be null.");
        }

        List<D> content = page.getContent().stream().map(mapper).toList();
        return new SheetResponse<>(content, PageResponse.of(page));
    }
}