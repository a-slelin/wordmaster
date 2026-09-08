package a.slelin.work.word.master.repository;

import a.slelin.work.word.master.entity.Card;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CardRepository extends JpaRepository<Card, UUID> {

    long countByDeckId(UUID deckId);

    List<Card> findByDeckIdOrderByPositionAsc(UUID deckId);

    void deleteAllByDeckId(UUID deckId);
}