package a.slelin.work.word.master.repository;

import a.slelin.work.word.master.entity.DailyActivity;
import a.slelin.work.word.master.entity.DailyActivityId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Repository
public interface DailyActivityRepository extends JpaRepository<DailyActivity, DailyActivityId> {

    List<DailyActivity> findByUserIdAndActivityDateBetweenOrderByActivityDateAsc(UUID userId, LocalDate from, LocalDate to);

    long countByUserIdAndGoalReachedTrue(UUID userId);
}
