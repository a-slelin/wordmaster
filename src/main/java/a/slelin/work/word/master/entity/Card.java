package a.slelin.work.word.master.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.validator.constraints.URL;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Entity(name = Card.ENTITY_NAME)
@EqualsAndHashCode(callSuper = false)
@Table(name = Card.TABLE_NAME)
public class Card extends Audit {

    public static final String ENTITY_NAME = "Card";

    public static final String TABLE_NAME = "card";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "deck_id", nullable = false)
    private Deck deck;

    @NotBlank
    @Size(min = 1, max = 255)
    @Column(nullable = false)
    private String word;

    @NotBlank
    @Size(min = 1, max = 255)
    @Column(nullable = false)
    private String translation;

    private String transcription;

    private String example_sentence;

    @URL
    @Column(name = "image_url")
    private String imageUrl;

    @URL
    @Column(name = "audio_url")
    private String audioUrl;

    @Min(1)
    @Column(nullable = false)
    private Long position;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(
            mappedBy = "card",
            orphanRemoval = true,
            fetch = FetchType.LAZY,
            cascade = CascadeType.REMOVE
    )
    private List<CardProgress> cardProgresses;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(
            mappedBy = "card",
            orphanRemoval = true,
            fetch = FetchType.LAZY,
            cascade = CascadeType.REMOVE
    )
    private List<TrainingAnswer> trainingAnswers;
}
