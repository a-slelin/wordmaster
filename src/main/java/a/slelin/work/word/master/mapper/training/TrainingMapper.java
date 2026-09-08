package a.slelin.work.word.master.mapper.training;

import a.slelin.work.word.master.dto.training.CardProgressResponse;
import a.slelin.work.word.master.dto.training.TrainingNextCardResponse;
import a.slelin.work.word.master.dto.training.TrainingSessionFinishResponse;
import a.slelin.work.word.master.dto.training.TrainingSessionStartResponse;
import a.slelin.work.word.master.entity.Card;
import a.slelin.work.word.master.entity.CardProgress;
import a.slelin.work.word.master.entity.TrainingSession;
import a.slelin.work.word.master.mapper.common.MapstructConfig;
import a.slelin.work.word.master.mapper.common.StatusMapper;
import a.slelin.work.word.master.mapper.common.UuidMapper;
import org.mapstruct.*;

import java.time.Duration;

@SuppressWarnings("unused")
@Mapper(config = MapstructConfig.class,
        uses = {UuidMapper.class, StatusMapper.class})
public interface TrainingMapper {

    @Mapping(target = "cardId", source = "card.id")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToDisplayName")
    CardProgressResponse toDto(CardProgress progress);

    @Mapping(target = "sessionId", source = "id")
    @Mapping(target = "deckId", source = "deck.id")
    @Mapping(target = "totalCards", source = "cardsTotal")
    TrainingSessionStartResponse toStartDto(TrainingSession session);

    @Mapping(target = "sessionId", source = "id")
    @Mapping(target = "accuracyPercent", expression = "java(calculateAccuracyPercent(session))")
    @Mapping(target = "durationSeconds", expression = "java(calculateDurationSeconds(session))")
    TrainingSessionFinishResponse toFinishDto(TrainingSession session);

    @Mapping(target = "sessionId", source = "session.id")
    @Mapping(target = "cardId", source = "card.id")
    @Mapping(target = "word", source = "card.word")
    @Mapping(target = "transcription", source = "card.transcription")
    TrainingNextCardResponse toNextCardDto(TrainingSession session, Card card, long cardsRemaining);

    default Double calculateAccuracyPercent(TrainingSession session) {
        if (session == null || session.getCardsTotal() == null || session.getCardsTotal() == 0) {
            return 0.0;
        }

        long correct = session.getCardsCorrect() == null ? 0L : session.getCardsCorrect();
        return (correct * 100.0) / session.getCardsTotal();
    }

    default Long calculateDurationSeconds(TrainingSession session) {
        if (session == null || session.getStartedAt() == null || session.getFinishedAt() == null) {
            return 0L;
        }

        return Duration.between(session.getStartedAt(), session.getFinishedAt()).getSeconds();
    }
}