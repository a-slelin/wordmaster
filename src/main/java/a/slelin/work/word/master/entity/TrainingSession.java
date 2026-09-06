package a.slelin.work.word.master.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Entity(name = TrainingSession.ENTITY_NAME)
@EqualsAndHashCode
@Table(name = TrainingSession.TABLE_NAME)
public class TrainingSession implements BaseEntity {

    public static final String ENTITY_NAME = "TrainingSession";

    public static final String TABLE_NAME = "training_session";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    @Column(nullable = false,
            name = "started_at")
    private LocalDateTime startedAt;

    @Column(name = "finished_at")
    private LocalDateTime finishedAt;

    @Min(0)
    @Column(nullable = false,
            name = "cards_total")
    private Long cardsTotal;

    @Min(0)
    @Column(name = "cards_correct")
    private Long cardsCorrect;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(
            mappedBy = "session",
            orphanRemoval = true,
            fetch = FetchType.LAZY,
            cascade = CascadeType.REMOVE
    )
    private List<TrainingAnswer> trainingAnswers;
}
