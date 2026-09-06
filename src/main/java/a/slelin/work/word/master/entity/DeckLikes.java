package a.slelin.work.word.master.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
@IdClass(DeckLikesId.class)
@Table(name = DeckLikes.TABLE_NAME)
@Entity(name = DeckLikes.ENTITY_NAME)
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class DeckLikes implements BaseEntity {

    public static final String ENTITY_NAME = "DeckLikes";

    public static final String TABLE_NAME = "deck_likes";

    @Id
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id",
            nullable = false)
    private User user;

    @Id
    @ToString.Exclude
    @EqualsAndHashCode.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "deck_id",
            nullable = false)
    private Deck deck;

    @Column(name = "created_at",
            nullable = false,
            updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void prePersist() {
        this.createdAt = LocalDateTime.now();
    }
}
