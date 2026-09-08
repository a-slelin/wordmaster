package a.slelin.work.word.master.repository;

import a.slelin.work.word.master.entity.CardProgress;
import a.slelin.work.word.master.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface CardProgressRepository extends JpaRepository<CardProgress, UUID> {

    long countByCard_Deck_IdAndUser_IdAndStatus(UUID deckId, UUID userId, Status status);
}