package a.slelin.work.word.master.dto.user;

import a.slelin.work.word.master.dto.RequestDto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import com.fasterxml.jackson.annotation.JsonIgnore;

import java.time.DateTimeException;
import java.time.ZoneId;

/**
 * Learning settings; a {@code null} field is left unchanged.
 */
@Builder
public record UserSettingsRequest(@Schema(description = "Cards to review per day", example = "20")
                                  @Min(5) @Max(500) Integer dailyGoal,
                                  @Schema(description = "IANA time zone of the user: days, streaks and the "
                                          + "daily goal are counted in it", example = "Europe/Berlin")
                                  @Size(max = 64) String timeZone) implements RequestDto {

    @JsonIgnore
    @Schema(hidden = true)
    @AssertTrue(message = "must be a valid IANA time zone, e.g. Europe/Berlin")
    public boolean isTimeZoneValid() {
        if (timeZone == null) {
            return true;
        }

        try {
            ZoneId.of(timeZone);
            return true;
        } catch (DateTimeException e) {
            return false;
        }
    }
}
