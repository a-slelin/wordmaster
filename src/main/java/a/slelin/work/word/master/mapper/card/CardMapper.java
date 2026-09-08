package a.slelin.work.word.master.mapper.card;

import a.slelin.work.word.master.dto.card.CardCreateRequest;
import a.slelin.work.word.master.dto.card.CardResponse;
import a.slelin.work.word.master.dto.card.CardUpdateRequest;
import a.slelin.work.word.master.dto.card.CardWithProgressResponse;
import a.slelin.work.word.master.entity.Card;
import a.slelin.work.word.master.entity.CardProgress;
import a.slelin.work.word.master.mapper.common.MapstructConfig;
import a.slelin.work.word.master.mapper.common.StatusMapper;
import a.slelin.work.word.master.mapper.common.UuidMapper;
import org.mapstruct.*;

@SuppressWarnings("unused")
@Mapper(config = MapstructConfig.class,
        uses = {UuidMapper.class, StatusMapper.class})
public interface CardMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deck", ignore = true)
    @Mapping(target = "cardProgresses", ignore = true)
    @Mapping(target = "trainingAnswers", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    Card toEntity(CardCreateRequest request);

    @Mapping(target = "deckId", source = "deck.id")
    CardResponse toDto(Card card);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "deck", ignore = true)
    @Mapping(target = "cardProgresses", ignore = true)
    @Mapping(target = "trainingAnswers", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Card patch(@MappingTarget Card card, CardUpdateRequest request);

    @Mapping(target = "id", source = "card.id")
    @Mapping(target = "deckId", source = "card.deck.id")
    @Mapping(target = "word", source = "card.word")
    @Mapping(target = "translation", source = "card.translation")
    @Mapping(target = "transcription", source = "card.transcription")
    @Mapping(target = "exampleSentence", source = "card.exampleSentence")
    @Mapping(target = "imageUrl", source = "card.imageUrl")
    @Mapping(target = "audioUrl", source = "card.audioUrl")
    @Mapping(target = "position", source = "card.position")
    @Mapping(target = "createdAt", source = "card.createdAt")
    @Mapping(target = "updatedAt", source = "card.updatedAt")
    @Mapping(target = "status", source = "progress.status", qualifiedByName = "statusToDisplayName")
    @Mapping(target = "easeFactor", source = "progress.easeFactor")
    @Mapping(target = "correctCount", source = "progress.correctCount")
    @Mapping(target = "incorrectCount", source = "progress.incorrectCount")
    @Mapping(target = "lastReviewedAt", source = "progress.lastReviewedAt")
    @Mapping(target = "nextReviewAt", source = "progress.nextReviewAt")
    @Mapping(target = "intervalDays", source = "progress.intervalDays")
    @Mapping(target = "repetitions", source = "progress.repetitions")
    CardWithProgressResponse toDto(Card card, CardProgress progress);
}