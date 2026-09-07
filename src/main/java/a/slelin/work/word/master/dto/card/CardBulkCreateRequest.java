package a.slelin.work.word.master.dto.card;

import a.slelin.work.word.master.dto.RequestDto;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;

import java.util.List;
import java.util.Objects;

@Builder
public record CardBulkCreateRequest(@NotNull List<CardCreateRequest> cards) implements RequestDto {

    @NonNull
    @Override
    public String toString() {
        if (cards == null || cards.isEmpty()) {
            return "CardBulkCreateRequest: [empty]";
        }
        return "CardBulkCreateRequest: [cards = " + cards + "]";
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
