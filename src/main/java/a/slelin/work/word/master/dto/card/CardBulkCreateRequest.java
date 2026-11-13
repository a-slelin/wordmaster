package a.slelin.work.word.master.dto.card;

import a.slelin.work.word.master.dto.RequestDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;

import java.util.List;

@Builder
public record CardBulkCreateRequest(@NotEmpty @Size(max = 1000) List<@Valid CardCreateRequest> cards,
                                    @Schema(description = "Skip cards whose word and translation are already "
                                            + "in the deck or repeated in the request (default: true)")
                                    Boolean skipDuplicates) implements RequestDto {

    public boolean shouldSkipDuplicates() {
        return skipDuplicates == null || skipDuplicates;
    }
}
