package a.slelin.work.word.master.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
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
@Table(name = CardProgress.TABLE_NAME)
@Entity(name = CardProgress.ENTITY_NAME)
public class CardProgress implements BaseEntity {

    public static final String ENTITY_NAME = "CardProgress";

    public static final String TABLE_NAME = "card_progress";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.EAGER,
            optional = false)
    @JoinColumn(name = "user_id",
            nullable = false)
    private User user;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.EAGER,
            optional = false)
    @JoinColumn(name = "card_id",
            nullable = false)
    private Card card;

    @Min(0)
    @NotNull
    @Column(nullable = false,
            name = "incorrect_count")
    private Long incorrectCount;

    @Min(0)
    @NotNull
    @Column(nullable = false,
            name = "correct_count")
    private Long correctCount;

    @Column(name = "last_reviewed_at")
    private LocalDateTime lastReviewedAt;

    @Column(name = "next_review_at")
    private LocalDateTime nextReviewAt;

    @Min(1)
    @NotNull
    @Column(nullable = false,
            name = "ease_factor")
    private Double easeFactor;

    @NotNull
    @Column(nullable = false, length = 15)
    @Convert(converter = StatusConverter.class)
    private Status status;
}
