package a.slelin.work.word.master.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Gamification state of a user: experience, streak and daily goal.
 */
@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Table(name = UserStats.TABLE_NAME)
@Entity(name = UserStats.ENTITY_NAME)
public class UserStats implements BaseEntity {

    public static final String ENTITY_NAME = "UserStats";

    public static final String TABLE_NAME = "user_stats";

    public static final int DEFAULT_DAILY_GOAL = 20;

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Min(0)
    @NotNull
    @Column(nullable = false)
    private Long xp;

    @Min(0)
    @NotNull
    @Column(nullable = false,
            name = "current_streak")
    private Integer currentStreak;

    @Min(0)
    @NotNull
    @Column(nullable = false,
            name = "longest_streak")
    private Integer longestStreak;

    @Column(name = "last_active_date")
    private LocalDate lastActiveDate;

    @Min(5)
    @Max(500)
    @NotNull
    @Column(nullable = false,
            name = "daily_goal")
    private Integer dailyGoal;

    public static UserStats empty(UUID userId) {
        return UserStats.builder()
                .userId(userId)
                .xp(0L)
                .currentStreak(0)
                .longestStreak(0)
                .dailyGoal(DEFAULT_DAILY_GOAL)
                .build();
    }
}
