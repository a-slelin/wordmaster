package a.slelin.work.word.master.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@IdClass(UserAchievementId.class)
@Table(name = UserAchievement.TABLE_NAME)
@Entity(name = UserAchievement.ENTITY_NAME)
public class UserAchievement implements BaseEntity {

    public static final String ENTITY_NAME = "UserAchievement";

    public static final String TABLE_NAME = "user_achievement";

    @Id
    @Column(name = "user_id")
    private UUID userId;

    @Id
    @Enumerated(EnumType.STRING)
    @Column(length = 50)
    private Achievement code;

    @NotNull
    @Column(nullable = false,
            updatable = false,
            name = "unlocked_at")
    private LocalDateTime unlockedAt;
}
