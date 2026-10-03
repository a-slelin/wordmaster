package a.slelin.work.word.master.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.util.UUID;

/**
 * One card of a training session plan. Cards answered with {@link Grade#AGAIN}
 * are moved to the end of the queue and asked again in the same session.
 */
@Getter
@Setter
@Builder
@ToString
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
@Table(name = TrainingSessionCard.TABLE_NAME)
@Entity(name = TrainingSessionCard.ENTITY_NAME)
public class TrainingSessionCard implements BaseEntity {

    public static final String ENTITY_NAME = "TrainingSessionCard";

    public static final String TABLE_NAME = "training_session_card";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY,
            optional = false)
    @JoinColumn(name = "session_id",
            nullable = false)
    private TrainingSession session;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY,
            optional = false)
    @JoinColumn(name = "card_id",
            nullable = false)
    private Card card;

    @Min(1)
    @NotNull
    @Column(nullable = false)
    private Long position;

    @NotNull
    @Column(nullable = false)
    private Boolean done;

    @Min(0)
    @NotNull
    @Column(nullable = false)
    private Integer attempts;
}
