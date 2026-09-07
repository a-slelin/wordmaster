package a.slelin.work.word.master.dto.training;

import a.slelin.work.word.master.dto.ResponseDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record TrainingSessionFinishResponse(@NotBlank String sessionId,
                                            @NotNull @Min(0) Long cardsTotal,
                                            @NotNull @Min(0) Long cardsCorrect,
                                            @NotNull @Min(0) @Max(100) Double accuracyPercent,
                                            @NotNull @Min(0) Long durationSeconds) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("sessionId = " + sessionId);
        parts.add("cardsTotal = " + cardsTotal);
        parts.add("cardsCorrect = " + cardsCorrect);
        parts.add("accuracyPercent = " + accuracyPercent);
        parts.add("durationSeconds = " + durationSeconds);
        return "TrainingSessionFinishResponse: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TrainingSessionFinishResponse that = (TrainingSessionFinishResponse) o;
        return Objects.equals(cardsTotal, that.cardsTotal) &&
                Objects.equals(sessionId, that.sessionId) &&
                Objects.equals(cardsCorrect, that.cardsCorrect) &&
                Objects.equals(durationSeconds, that.durationSeconds) &&
                Objects.equals(accuracyPercent, that.accuracyPercent);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, cardsTotal, cardsCorrect, accuracyPercent, durationSeconds);
    }
}
