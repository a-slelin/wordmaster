package a.slelin.work.word.master.dto.training;

import a.slelin.work.word.master.dto.RequestDto;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record TrainingAnswerRequest(@NotBlank String cardId,
                                    String grade,
                                    @Size(max = 255) String answer) implements RequestDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("cardId = " + cardId);
        if (grade != null) parts.add("grade = " + grade);
        if (answer != null) parts.add("answer = " + answer);
        return "TrainingAnswerRequest: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TrainingAnswerRequest that = (TrainingAnswerRequest) o;
        return Objects.equals(cardId, that.cardId) &&
                Objects.equals(grade, that.grade) &&
                Objects.equals(answer, that.answer);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardId, grade, answer);
    }
}
