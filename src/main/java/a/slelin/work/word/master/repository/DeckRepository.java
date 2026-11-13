package a.slelin.work.word.master.repository;

import a.slelin.work.word.master.entity.Deck;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeckRepository extends JpaRepository<Deck, UUID>, JpaSpecificationExecutor<Deck> {

    Page<Deck> findAllByOwnerId(UUID ownerId, Pageable pageable);

    Page<Deck> findAllByIsPublicTrue(Pageable pageable);

    long countByOwner_Id(UUID ownerId);

    long countByOwner_IdAndSourceDeckIsNull(UUID ownerId);

    long countByOwner_IdAndSourceDeckIsNotNull(UUID ownerId);

    long countByOwner_IdAndIsPublicTrue(UUID ownerId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Deck d set d.likesCount = d.likesCount + :delta where d.id = :deckId")
    void changeLikesCount(@Param("deckId") UUID deckId, @Param("delta") long delta);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Deck d set d.copiesCount = d.copiesCount + 1 where d.id = :deckId")
    void incrementCopiesCount(@Param("deckId") UUID deckId);

    @Query("select d.likesCount from Deck d where d.id = :deckId")
    long findLikesCount(@Param("deckId") UUID deckId);

    /**
     * Cards, likes, tags, sessions and progress are removed by "ON DELETE CASCADE" in the database.
     */
    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "delete from deck where id = :deckId", nativeQuery = true)
    void deleteDeck(@Param("deckId") UUID deckId);
}
