package a.slelin.work.word.master.repository;

import a.slelin.work.word.master.entity.DeckLikes;
import a.slelin.work.word.master.entity.DeckLikesId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeckLikesRepository extends JpaRepository<DeckLikes, DeckLikesId> {

    boolean existsByUser_IdAndDeck_Id(UUID userId, UUID deckId);

    void deleteByUser_IdAndDeck_Id(UUID userId, UUID deckId);
}