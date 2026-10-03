package a.slelin.work.word.master.dto.training;

import a.slelin.work.word.master.dto.ResponseDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record TrainingAnswerResponse(@NotBlank String cardId,
                                     @NotNull Boolean correct,
                                     @NotBlank String grade,
                                     @NotBlank String correctAnswer,
                                     String userAnswer,
                                     @NotNull @Valid CardProgressResponse updatedProgress,
                                     @NotNull @Min(0) Long xpEarned,
                                     @NotNull Boolean finished,
                                     @Valid TrainingNextCardResponse nextCard) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("cardId = " + cardId);
        parts.add("correct = " + correct);
        parts.add("grade = " + grade);
        parts.add("correctAnswer = " + correctAnswer);
        if (userAnswer != null) parts.add("userAnswer = " + userAnswer);
        parts.add("updatedProgress = " + updatedProgress);
        parts.add("xpEarned = " + xpEarned);
        parts.add("finished = " + finished);
        if (nextCard != null) parts.add("nextCard = " + nextCard);
        return "TrainingAnswerResponse: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TrainingAnswerResponse that = (TrainingAnswerResponse) o;
        return Objects.equals(cardId, that.cardId) &&
                Objects.equals(correct, that.correct) &&
                Objects.equals(grade, that.grade) &&
                Objects.equals(correctAnswer, that.correctAnswer) &&
                Objects.equals(userAnswer, that.userAnswer) &&
                Objects.equals(updatedProgress, that.updatedProgress) &&
                Objects.equals(xpEarned, that.xpEarned) &&
                Objects.equals(finished, that.finished) &&
                Objects.equals(nextCard, that.nextCard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cardId, correct, grade, correctAnswer, userAnswer, updatedProgress, xpEarned,
                finished, nextCard);
    }
}
