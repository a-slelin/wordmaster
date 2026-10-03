package a.slelin.work.word.master.mapper.training;

import a.slelin.work.word.master.dto.training.CardProgressResponse;
import a.slelin.work.word.master.dto.training.TrainingNextCardResponse;
import a.slelin.work.word.master.dto.training.TrainingSessionStartResponse;
import a.slelin.work.word.master.entity.CardProgress;
import a.slelin.work.word.master.entity.TrainingSession;
import a.slelin.work.word.master.mapper.common.MapstructConfig;
import a.slelin.work.word.master.mapper.common.StandardEnumMapper;
import a.slelin.work.word.master.mapper.common.StatusMapper;
import a.slelin.work.word.master.mapper.common.UuidMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.time.Duration;

@SuppressWarnings("unused")
@Mapper(config = MapstructConfig.class,
        uses = {UuidMapper.class, StatusMapper.class, StandardEnumMapper.class})
public interface TrainingMapper {

    @Mapping(target = "cardId", source = "card.id")
    @Mapping(target = "status", source = "status", qualifiedByName = "statusToDisplayName")
    CardProgressResponse toDto(CardProgress progress);

    @Mapping(target = "sessionId", source = "session.id")
    @Mapping(target = "deckId", source = "session.deck.id")
    @Mapping(target = "deckTitle", source = "session.deck.title")
    @Mapping(target = "mode", source = "session.mode")
    @Mapping(target = "direction", source = "session.direction")
    @Mapping(target = "totalCards", source = "session.cardsTotal")
    @Mapping(target = "startedAt", source = "session.startedAt")
    @Mapping(target = "firstCard", source = "firstCard")
    TrainingSessionStartResponse toStartDto(TrainingSession session, TrainingNextCardResponse firstCard);

    default Double calculateAccuracyPercent(long correct, long total) {
        if (total == 0) {
            return 0.0;
        }

        return Math.round((correct * 1000.0) / total) / 10.0;
    }

    default Long calculateDurationSeconds(TrainingSession session) {
        if (session == null || session.getStartedAt() == null || session.getFinishedAt() == null) {
            return 0L;
        }

        return Duration.between(session.getStartedAt(), session.getFinishedAt()).getSeconds();
    }
}
