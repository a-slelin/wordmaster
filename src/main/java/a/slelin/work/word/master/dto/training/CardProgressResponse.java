package a.slelin.work.word.master.dto.training;

import a.slelin.work.word.master.dto.ResponseDto;
import a.slelin.work.word.master.utility.LocalDateTimeDeserializer;
import a.slelin.work.word.master.utility.LocalDateTimeSerializer;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;
import tools.jackson.databind.annotation.JsonDeserialize;
import tools.jackson.databind.annotation.JsonSerialize;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record CardProgressResponse(@NotBlank String cardId,
                                   @NotBlank String status,
                                   @NotNull @Min(1) Double easeFactor,
                                   @NotNull @Min(0) Long intervalDays,
                                   @NotNull Boolean repetitions,
                                   @NotNull @Min(0) Long correctCount,
                                   @NotNull @Min(0) Long incorrectCount,
                                   @JsonSerialize(using = LocalDateTimeSerializer.class)
                                   @JsonDeserialize(using = LocalDateTimeDeserializer.class)
                                   @NotNull LocalDateTime lastReviewedAt,
                                   @JsonSerialize(using = LocalDateTimeSerializer.class)
                                   @JsonDeserialize(using = LocalDateTimeDeserializer.class)
                                   @NotNull LocalDateTime nextReviewAt) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("cardId = " + cardId);
        parts.add("status = " + status);
        parts.add("easeFactor = " + easeFactor);
        parts.add("intervalDays = " + intervalDays);
        parts.add("repetitions = " + repetitions);
        parts.add("correctCount = " + correctCount);
        parts.add("incorrectCount = " + incorrectCount);
        parts.add("lastReviewedAt = " + lastReviewedAt);
        parts.add("nextReviewAt = " + nextReviewAt);
        return "CardProgressResponse: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        CardProgressResponse that = (CardProgressResponse) o;
        return Objects.equals(cardId, that.cardId) &&
                Objects.equals(status, that.status) &&
                Objects.equals(easeFactor, that.easeFactor) &&
                Objects.equals(intervalDays, that.intervalDays) &&
                Objects.equals(correctCount, that.correctCount) &&
                Objects.equals(repetitions, that.repetitions) &&
                Objects.equals(incorrectCount, that.incorrectCount) &&
                Objects.equals(nextReviewAt, that.nextReviewAt) &&
                Objects.equals(lastReviewedAt, that.lastReviewedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardId, status, easeFactor, intervalDays,
                repetitions, correctCount, incorrectCount, lastReviewedAt, nextReviewAt);
    }
}
