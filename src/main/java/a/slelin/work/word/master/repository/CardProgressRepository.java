package a.slelin.work.word.master.repository;

import a.slelin.work.word.master.entity.CardProgress;
import a.slelin.work.word.master.entity.Status;
import a.slelin.work.word.master.repository.projection.DeckCount;
import a.slelin.work.word.master.repository.projection.DeckStatusCount;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CardProgressRepository extends JpaRepository<CardProgress, UUID> {

    Optional<CardProgress> findByUser_IdAndCard_Id(UUID userId, UUID cardId);

    List<CardProgress> findByUser_IdAndCard_Deck_Id(UUID userId, UUID deckId);

    long countByCard_Deck_IdAndUser_IdAndStatus(UUID deckId, UUID userId, Status status);

    long countByCard_Deck_IdAndUser_IdAndNextReviewAtLessThanEqual(UUID deckId, UUID userId, LocalDateTime time);

    long countByUser_IdAndStatus(UUID userId, Status status);

    long countByUser_IdAndNextReviewAtLessThanEqual(UUID userId, LocalDateTime time);

    @Query("""
            select new a.slelin.work.word.master.repository.projection.DeckStatusCount(p.card.deck.id, p.status, count(p))
            from CardProgress p
            where p.user.id = :userId and p.card.deck.id in :deckIds
            group by p.card.deck.id, p.status
            """)
    List<DeckStatusCount> countByStatus(@Param("userId") UUID userId, @Param("deckIds") Collection<UUID> deckIds);

    @Query("""
            select new a.slelin.work.word.master.repository.projection.DeckCount(p.card.deck.id, count(p))
            from CardProgress p
            where p.user.id = :userId and p.card.deck.id in :deckIds and p.nextReviewAt <= :time
            group by p.card.deck.id
            """)
    List<DeckCount> countDue(@Param("userId") UUID userId, @Param("deckIds") Collection<UUID> deckIds,
                             @Param("time") LocalDateTime time);

    @Query("""
            select p from CardProgress p
            join fetch p.card c
            join fetch c.deck d
            where p.user.id = :userId
              and p.incorrectCount > 0
            order by (p.incorrectCount * 1.0 / (p.correctCount + p.incorrectCount)) desc, p.incorrectCount desc
            """)
    List<CardProgress> findHardest(@Param("userId") UUID userId, Pageable pageable);

    @Query("""
            select p from CardProgress p
            join fetch p.card c
            join fetch c.deck d
            where p.user.id = :userId
              and d.id = :deckId
              and p.incorrectCount > 0
            order by (p.incorrectCount * 1.0 / (p.correctCount + p.incorrectCount)) desc, p.incorrectCount desc
            """)
    List<CardProgress> findHardestInDeck(@Param("userId") UUID userId, @Param("deckId") UUID deckId,
                                         Pageable pageable);

    /**
     * Number of reviews per day in [from, to) for the review forecast.
     */
    @Query(value = """
            select cast(next_review_at as date) as day, count(*) as cnt
            from card_progress
            where user_id = :userId and next_review_at < :to
            group by cast(next_review_at as date)
            order by day
            """, nativeQuery = true)
    List<Object[]> forecast(@Param("userId") UUID userId, @Param("to") LocalDateTime to);
}
