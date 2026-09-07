package a.slelin.work.word.master.dto.training;

import a.slelin.work.word.master.dto.RequestDto;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record TrainingAnswerRequest(@NotBlank String sessionId,
                                    @NotBlank String cardId,
                                    @NotBlank String grade) implements RequestDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("sessionId = " + sessionId);
        parts.add("cardId = " + cardId);
        parts.add("grade = " + grade);
        return "TrainingAnswerRequest: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TrainingAnswerRequest that = (TrainingAnswerRequest) o;
        return Objects.equals(grade, that.grade) &&
                Objects.equals(cardId, that.cardId) &&
                Objects.equals(sessionId, that.sessionId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sessionId, cardId, grade);
    }
}
