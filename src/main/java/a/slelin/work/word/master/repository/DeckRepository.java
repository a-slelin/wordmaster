package a.slelin.work.word.master.repository;

import a.slelin.work.word.master.entity.Deck;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DeckRepository extends JpaRepository<Deck, UUID> {

    Page<Deck> findAllByOwnerId(UUID ownerId, Pageable pageable);

    Page<Deck> findAllByIsPublicTrue(Pageable pageable);
}