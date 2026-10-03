package a.slelin.work.word.master.repository;

import a.slelin.work.word.master.entity.TrainingSessionCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrainingSessionCardRepository extends JpaRepository<TrainingSessionCard, UUID> {

    Optional<TrainingSessionCard> findFirstBySession_IdAndDoneFalseOrderByPositionAsc(UUID sessionId);

    Optional<TrainingSessionCard> findBySession_IdAndCard_Id(UUID sessionId, UUID cardId);

    long countBySession_IdAndDoneFalse(UUID sessionId);

    long countBySession_IdAndDoneTrue(UUID sessionId);

    @Query("select coalesce(max(c.position), 0) from TrainingSessionCard c where c.session.id = :sessionId")
    long findMaxPosition(@Param("sessionId") UUID sessionId);
}
