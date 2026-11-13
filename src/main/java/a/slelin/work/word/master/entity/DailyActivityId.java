package a.slelin.work.word.master.entity;

import lombok.*;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@EqualsAndHashCode
@AllArgsConstructor
public class DailyActivityId implements Serializable {

    private UUID userId;

    private LocalDate activityDate;
}
