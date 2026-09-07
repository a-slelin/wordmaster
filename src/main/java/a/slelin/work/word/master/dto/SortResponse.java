package a.slelin.work.word.master.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.NonNull;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Objects;

@Builder
public record SortResponse(@NotNull @Pattern(regexp = "^[A-Za-z._]+$") String property,
                           @NotNull @Pattern(regexp = "^(?i)asc|desc$") String direction) implements ResponseDto {

    public static List<SortResponse> of(Sort sort) {
        if (sort == null) {
            throw new IllegalArgumentException("Sort must not be null.");
        }

        return sort.stream()
                .map(order -> SortResponse.builder()
                        .property(order.getProperty())
                        .direction(order.getDirection().toString().toLowerCase())
                        .build())
                .toList();
    }

    @NonNull
    @Override
    public String toString() {
        return "SortResponse: [property = %s, direction = %s]"
                .formatted(property, direction.toUpperCase());
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        SortResponse sort = (SortResponse) o;
        return Objects.equals(property, sort.property) &&
                Objects.equals(direction, sort.direction);
    }

    @Override
    public int hashCode() {
        return Objects.hash(property, direction);
    }
}