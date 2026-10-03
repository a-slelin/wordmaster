package a.slelin.work.word.master.dto.user;

import a.slelin.work.word.master.dto.RequestDto;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record UserSettingsRequest(@NotNull @Min(5) @Max(500) Integer dailyGoal) implements RequestDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("dailyGoal = " + dailyGoal);
        return "UserSettingsRequest: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        UserSettingsRequest that = (UserSettingsRequest) o;
        return Objects.equals(dailyGoal, that.dailyGoal);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(dailyGoal);
    }
}
