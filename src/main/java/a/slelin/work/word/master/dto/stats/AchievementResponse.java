package a.slelin.work.word.master.dto.stats;

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
public record AchievementResponse(@NotBlank String code,
                                  @NotBlank String icon,
                                  @NotNull @Min(0) Long progress,
                                  @NotNull @Min(1) Long goal,
                                  @NotNull Boolean unlocked,
                                  @JsonSerialize(using = LocalDateTimeSerializer.class)
                                  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
                                  LocalDateTime unlockedAt) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("code = " + code);
        parts.add("icon = " + icon);
        parts.add("progress = " + progress);
        parts.add("goal = " + goal);
        parts.add("unlocked = " + unlocked);
        if (unlockedAt != null) parts.add("unlockedAt = " + unlockedAt);
        return "AchievementResponse: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        AchievementResponse that = (AchievementResponse) o;
        return Objects.equals(code, that.code) &&
                Objects.equals(icon, that.icon) &&
                Objects.equals(progress, that.progress) &&
                Objects.equals(goal, that.goal) &&
                Objects.equals(unlocked, that.unlocked) &&
                Objects.equals(unlockedAt, that.unlockedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code, icon, progress, goal, unlocked, unlockedAt);
    }
}
