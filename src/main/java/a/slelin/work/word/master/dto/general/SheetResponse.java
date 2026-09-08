package a.slelin.work.word.master.dto.general;

import a.slelin.work.word.master.dto.ResponseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;

import java.util.List;
import java.util.Objects;

@Builder
public record SheetResponse<D extends ResponseDto>(@NotNull @Valid List<D> content,
                                                   @NotNull @Valid PageResponse page) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        return "SheetResponse: [hashcode = %d]".formatted(this.hashCode());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SheetResponse<?> sheetDto = (SheetResponse<?>) o;
        return Objects.equals(page, sheetDto.page) &&
                Objects.equals(content, sheetDto.content);
    }

    @Override
    public int hashCode() {
        return Objects.hash(content, page);
    }
}