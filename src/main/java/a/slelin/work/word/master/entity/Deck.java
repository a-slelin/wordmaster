package a.slelin.work.word.master.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString(callSuper = true)
@Entity(name = Deck.ENTITY_NAME)
@EqualsAndHashCode(callSuper = false)
@Table(name = Deck.TABLE_NAME)
public class Deck extends Audit {

    public static final String ENTITY_NAME = "Deck";

    public static final String TABLE_NAME = "deck";

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne
    @JoinColumn(name = "source_deck_id")
    private Deck sourceDeck;

    @NotBlank
    @Size(min = 3, max = 255)
    @Column(nullable = false)
    private String title;

    private String description;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "source_language_id", nullable = false)
    private Language sourceLanguage;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "target_language_id", nullable = false)
    private Language targetLanguage;

    @Column(nullable = false,
            name = "is_public")
    private boolean isPublic;

    @Min(0)
    @Column(nullable = false,
            name = "likes_count")
    private Long likesCount;

    @Min(0)
    @Column(nullable = false,
            name = "copies_count")
    private Long copiesCount;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "deck_tag",
            joinColumns = @JoinColumn(name = "deck_id", nullable = false),
            inverseJoinColumns = @JoinColumn(name = "tag_id", nullable = false))
    private List<Tag> tags;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(
            mappedBy = "deck",
            orphanRemoval = true,
            fetch = FetchType.LAZY,
            cascade = CascadeType.REMOVE
    )
    private List<DeckLikes> deckLikes;

    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @OneToMany(
            mappedBy = "deck",
            orphanRemoval = true,
            fetch = FetchType.LAZY,
            cascade = CascadeType.REMOVE
    )
    private List<TrainingSession> trainingSessions;
}
