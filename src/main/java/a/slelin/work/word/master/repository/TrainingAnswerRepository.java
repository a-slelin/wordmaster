package a.slelin.work.word.master.repository;

import a.slelin.work.word.master.entity.TrainingAnswer;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TrainingAnswerRepository extends JpaRepository<TrainingAnswer, UUID> {

    long countBySession_User_Id(UUID userId);

    long countBySession_User_IdAndIsCorrectTrue(UUID userId);
}
