package a.slelin.work.word.master.repository;

import a.slelin.work.word.master.entity.DeckLikes;
import a.slelin.work.word.master.entity.DeckLikesId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.Set;
import java.util.UUID;

/**
 * Native queries: Hibernate fails on derived/JPQL queries by association parts of {@link DeckLikesId}.
 */
@Repository
public interface DeckLikesRepository extends JpaRepository<DeckLikes, DeckLikesId> {

    @Query(value = "select exists(select 1 from deck_likes where user_id = :userId and deck_id = :deckId)",
            nativeQuery = true)
    boolean existsByUserAndDeck(@Param("userId") UUID userId, @Param("deckId") UUID deckId);

    @Query(value = "select count(*) from deck_likes where user_id = :userId", nativeQuery = true)
    long countByUser(@Param("userId") UUID userId);

    @Modifying
    @Query(value = "insert into deck_likes (user_id, deck_id) values (:userId, :deckId) on conflict do nothing",
            nativeQuery = true)
    int insert(@Param("userId") UUID userId, @Param("deckId") UUID deckId);

    @Modifying
    @Query(value = "delete from deck_likes where user_id = :userId and deck_id = :deckId", nativeQuery = true)
    int deleteByUserAndDeck(@Param("userId") UUID userId, @Param("deckId") UUID deckId);

    @Query(value = "select deck_id from deck_likes where user_id = :userId and deck_id in (:deckIds)",
            nativeQuery = true)
    Set<UUID> findLikedDeckIds(@Param("userId") UUID userId, @Param("deckIds") Collection<UUID> deckIds);
}
