package a.slelin.work.word.master.repository;

import a.slelin.work.word.master.entity.TrainingSession;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface TrainingSessionRepository extends JpaRepository<TrainingSession, UUID> {

    Optional<TrainingSession> findByIdAndUser_Id(UUID id, UUID userId);

    /**
     * Locks the session row until the end of the transaction, so two parallel answers
     * (double click, retry after a timeout) are processed one after another.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from TrainingSession s where s.id = :id and s.user.id = :userId")
    Optional<TrainingSession> findForUpdate(@Param("id") UUID id, @Param("userId") UUID userId);

    long countByUser_IdAndFinishedAtIsNotNull(UUID userId);

    @Query("""
            select count(s) from TrainingSession s
            where s.user.id = :userId and s.finishedAt is not null
              and s.cardsTotal >= :minCards and s.cardsCorrect = s.cardsTotal
            """)
    long countPerfectSessions(@Param("userId") UUID userId, @Param("minCards") long minCards);

    /**
     * Timestamps are stored in the time zone of the application ({@code appZone});
     * the hour is taken by the clock of the user ({@code userZone}).
     */
    @Query(value = """
            select count(*) from training_session
            where user_id = :userId and finished_at is not null
              and extract(hour from (finished_at at time zone :appZone) at time zone :userZone) >= :fromHour
              and extract(hour from (finished_at at time zone :appZone) at time zone :userZone) < :toHour
            """, nativeQuery = true)
    long countFinishedBetweenHours(@Param("userId") UUID userId,
                                   @Param("appZone") String appZone,
                                   @Param("userZone") String userZone,
                                   @Param("fromHour") int fromHour,
                                   @Param("toHour") int toHour);

    @Query("select count(distinct s.deck.targetLanguage.id) from TrainingSession s where s.user.id = :userId")
    long countTrainedLanguages(@Param("userId") UUID userId);
}
