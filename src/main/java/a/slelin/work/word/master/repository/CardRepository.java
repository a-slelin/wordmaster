package a.slelin.work.word.master.repository;

import a.slelin.work.word.master.entity.Card;
import a.slelin.work.word.master.repository.projection.DeckCount;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    long countByDeckId(UUID deckId);

    long countByDeck_Owner_Id(UUID ownerId);

    List<Card> findByDeckIdOrderByPositionAsc(UUID deckId);

    @Query("""
            select new a.slelin.work.word.master.repository.projection.DeckCount(c.deck.id, count(c))
            from Card c
            where c.deck.id in :deckIds
            group by c.deck.id
            """)
    List<DeckCount> countByDeckIds(@Param("deckIds") Collection<UUID> deckIds);

    @Query("select coalesce(max(c.position), 0) from Card c where c.deck.id = :deckId")
    long findMaxPosition(@Param("deckId") UUID deckId);

    /**
     * Cards of the deck the user has never trained (no progress yet), in deck order.
     */
    @Query("""
            select c from Card c
            where c.deck.id = :deckId
              and not exists (select 1 from CardProgress p where p.card = c and p.user.id = :userId)
            order by c.position asc
            """)
    List<Card> findNewCards(@Param("deckId") UUID deckId, @Param("userId") UUID userId, Pageable pageable);

    /**
     * Cards whose review date has come, the most overdue first.
     */
    @Query("""
            select p.card from CardProgress p
            where p.card.deck.id = :deckId
              and p.user.id = :userId
              and p.nextReviewAt <= :now
            order by p.nextReviewAt asc
            """)
    List<Card> findDueCards(@Param("deckId") UUID deckId, @Param("userId") UUID userId,
                            @Param("now") LocalDateTime now, Pageable pageable);

    /**
     * Already studied cards ordered by the nearest review date (used to practice ahead of schedule).
     */
    @Query("""
            select p.card from CardProgress p
            where p.card.deck.id = :deckId
              and p.user.id = :userId
            order by p.nextReviewAt asc
            """)
    List<Card> findUpcomingCards(@Param("deckId") UUID deckId, @Param("userId") UUID userId, Pageable pageable);

    /**
     * Cards with the highest error rate.
     */
    @Query("""
            select p.card from CardProgress p
            where p.card.deck.id = :deckId
              and p.user.id = :userId
              and p.incorrectCount > 0
            order by (p.incorrectCount * 1.0 / (p.correctCount + p.incorrectCount)) desc, p.incorrectCount desc
            """)
    List<Card> findHardCards(@Param("deckId") UUID deckId, @Param("userId") UUID userId, Pageable pageable);

    @Query("select c from Card c join fetch c.deck d where d.owner.id = :ownerId order by c.id")
    List<Card> findByOwner(@Param("ownerId") UUID ownerId, Pageable pageable);

    @Query("select c from Card c join fetch c.deck d where d.isOfficial = true order by c.id")
    List<Card> findOfficial(Pageable pageable);

    long countByDeck_IsOfficialTrue();

    /**
     * Progress, answers and session plan rows are removed by "ON DELETE CASCADE" in the database.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "delete from card where id = :cardId", nativeQuery = true)
    void deleteCard(@Param("cardId") UUID cardId);
}
