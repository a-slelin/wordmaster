package a.slelin.work.word.master.repository;

import a.slelin.work.word.master.entity.UserAchievement;
import a.slelin.work.word.master.entity.UserAchievementId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface UserAchievementRepository extends JpaRepository<UserAchievement, UserAchievementId> {

    List<UserAchievement> findByUserId(UUID userId);

    long countByUserId(UUID userId);
}
