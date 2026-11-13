package a.slelin.work.word.master.dto.stats;

import a.slelin.work.word.master.dto.ResponseDto;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.NonNull;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Builder
public record ForecastDayResponse(@NotNull LocalDate date,
                                  @NotNull @Min(0) Long dueCount) implements ResponseDto {

    @NonNull
    @Override
    public String toString() {
        List<String> parts = new ArrayList<>();
        parts.add("date = " + date);
        parts.add("dueCount = " + dueCount);
        return "ForecastDayResponse: [" + String.join(", ", parts) + "]";
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ForecastDayResponse that = (ForecastDayResponse) o;
        return Objects.equals(date, that.date) &&
                Objects.equals(dueCount, that.dueCount);
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, dueCount);
    }
}
