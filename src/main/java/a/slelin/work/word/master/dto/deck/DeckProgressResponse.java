package a.slelin.work.word.master.dto.deck;

import a.slelin.work.word.master.dto.ResponseDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record DeckProgressResponse(@NotBlank String deckId,
                                   @NonNull @Min(0) Long totalCards,
                                   @NonNull @Min(0) Long newCards,
                                   @NonNull @Min(0) Long learningCards,
                                   @NonNull @Min(0) Long knownCards,
                                   @NonNull @Min(0) @Max(100) Integer percentLearned) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("deckId = " + deckId);
        parts.add("totalCards = " + totalCards);
        parts.add("newCards = " + newCards);
        parts.add("learningCards = " + learningCards);
        parts.add("knownCards = " + knownCards);
        parts.add("percentLearned = " + percentLearned);
        return "DeckProgressResponse: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        DeckProgressResponse that = (DeckProgressResponse) o;
        return Objects.equals(deckId, that.deckId) &&
                Objects.equals(newCards, that.newCards) &&
                Objects.equals(totalCards, that.totalCards) &&
                Objects.equals(knownCards, that.knownCards) &&
                Objects.equals(learningCards, that.learningCards) &&
                Objects.equals(percentLearned, that.percentLearned);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deckId, totalCards, newCards, learningCards, knownCards, percentLearned);
    }
}
