package a.slelin.work.word.master.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * How much a user trained during one day (used for daily goal and activity heatmap).
 */
@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@IdClass(DailyActivityId.class)
@Table(name = DailyActivity.TABLE_NAME)
@Entity(name = DailyActivity.ENTITY_NAME)
public class DailyActivity implements BaseEntity {

    public static final String ENTITY_NAME = "DailyActivity";

    public static final String TABLE_NAME = "daily_activity";

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Column(name = "activity_date")
    private LocalDate activityDate;

    @Min(0)
    @NotNull
    @Column(nullable = false,
            name = "cards_reviewed")
    private Integer cardsReviewed;

    @Min(0)
    @NotNull
    @Column(nullable = false,
            name = "xp_earned")
    private Long xpEarned;
}
