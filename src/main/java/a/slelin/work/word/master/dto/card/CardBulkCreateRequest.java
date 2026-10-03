package a.slelin.work.word.master.dto.card;

import a.slelin.work.word.master.dto.RequestDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record CardBulkCreateRequest(@NotEmpty @Size(max = 1000) List<@Valid CardCreateRequest> cards) implements RequestDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("cards = " + cards);
        return "CardBulkCreateRequest: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CardBulkCreateRequest that = (CardBulkCreateRequest) o;
        return Objects.equals(cards, that.cards);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(cards);
    }
}
