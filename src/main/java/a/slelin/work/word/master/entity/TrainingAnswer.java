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
@Table(name = TrainingAnswer.TABLE_NAME)
@Entity(name = TrainingAnswer.ENTITY_NAME)
public class TrainingAnswer implements BaseEntity {

    public static final String ENTITY_NAME = "TrainingAnswer";

    public static final String TABLE_NAME = "training_answer";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.EAGER,
            optional = false)
    @JoinColumn(name = "session_id",
            nullable = false)
    private TrainingSession session;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.EAGER,
            optional = false)
    @JoinColumn(name = "card_id",
            nullable = false)
    private Card card;

    @Column(nullable = false,
            name = "is_correct")
    private boolean isCorrect;

    @NotNull
    @Column(nullable = false,
            name = "answered_at")
    private LocalDateTime answeredAt;
}
