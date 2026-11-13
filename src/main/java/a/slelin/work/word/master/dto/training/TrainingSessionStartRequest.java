package a.slelin.work.word.master.dto.training;

import a.slelin.work.word.master.dto.RequestDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record TrainingSessionStartRequest(@NotBlank String deckId,
                                          String mode,
                                          String direction,
                                          String scope,
                                          @Min(1) @Max(200) Integer limit) implements RequestDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("deckId = " + deckId);
        if (mode != null) parts.add("mode = " + mode);
        if (direction != null) parts.add("direction = " + direction);
        if (scope != null) parts.add("scope = " + scope);
        if (limit != null) parts.add("limit = " + limit);
        return "TrainingSessionStartRequest: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        TrainingSessionStartRequest that = (TrainingSessionStartRequest) o;
        return Objects.equals(deckId, that.deckId) &&
                Objects.equals(mode, that.mode) &&
                Objects.equals(direction, that.direction) &&
                Objects.equals(scope, that.scope) &&
                Objects.equals(limit, that.limit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deckId, mode, direction, scope, limit);
    }
}
