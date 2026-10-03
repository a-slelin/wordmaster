package a.slelin.work.word.master.mapper.stats;

import a.slelin.work.word.master.dto.stats.ActivityDayResponse;
import a.slelin.work.word.master.dto.stats.HardWordResponse;
import a.slelin.work.word.master.dto.stats.WordOfTheDayResponse;
import a.slelin.work.word.master.entity.Card;
import a.slelin.work.word.master.entity.CardProgress;
import a.slelin.work.word.master.entity.DailyActivity;
import a.slelin.work.word.master.mapper.common.MapstructConfig;
import a.slelin.work.word.master.mapper.common.UuidMapper;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@SuppressWarnings("unused")
@Mapper(config = MapstructConfig.class, uses = UuidMapper.class)
public interface StatsMapper {

    @Mapping(target = "cardId", source = "card.id")
    @Mapping(target = "word", source = "card.word")
    @Mapping(target = "translation", source = "card.translation")
    @Mapping(target = "deckId", source = "card.deck.id")
    @Mapping(target = "deckTitle", source = "card.deck.title")
    @Mapping(target = "correctCount", source = "correctCount")
    @Mapping(target = "incorrectCount", source = "incorrectCount")
    @Mapping(target = "errorRate", expression = "java(calculateErrorRate(progress.getCorrectCount(), progress.getIncorrectCount()))")
    HardWordResponse toHardWordDto(CardProgress progress);

    @Mapping(target = "cardId", source = "id")
    @Mapping(target = "languageCode", source = "deck.targetLanguage.code")
    @Mapping(target = "deckId", source = "deck.id")
    @Mapping(target = "deckTitle", source = "deck.title")
    WordOfTheDayResponse toWordOfTheDayDto(Card card);

    @Mapping(target = "date", source = "activityDate")
    ActivityDayResponse toActivityDto(DailyActivity activity);

    default Double calculateErrorRate(Long correctCount, Long incorrectCount) {
        long correct = correctCount == null ? 0 : correctCount;
        long incorrect = incorrectCount == null ? 0 : incorrectCount;
        long total = correct + incorrect;
        if (total == 0) {
            return 0.0;
        }

        return incorrect / (double) total;
    }
}
