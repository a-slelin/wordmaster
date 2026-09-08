package a.slelin.work.word.master.mapper.stats;

import a.slelin.work.word.master.dto.stats.HardWordResponse;
import a.slelin.work.word.master.dto.stats.WordOfTheDayResponse;
import a.slelin.work.word.master.entity.Card;
import a.slelin.work.word.master.mapper.common.MapstructConfig;
import a.slelin.work.word.master.mapper.common.UuidMapper;
import org.mapstruct.*;

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
    @Mapping(target = "errorRate", expression = "java(calculateErrorRate(correctCount, incorrectCount))")
    HardWordResponse toHardWordDto(Card card, long correctCount, long incorrectCount);

    @Mapping(target = "cardId", source = "id")
    @Mapping(target = "deckId", source = "deck.id")
    @Mapping(target = "deckTitle", source = "deck.title")
    WordOfTheDayResponse toWordOfTheDayDto(Card card);

    default Double calculateErrorRate(long correctCount, long incorrectCount) {
        long total = correctCount + incorrectCount;
        if (total == 0) {
            return 0.0;
        }

        return incorrectCount / (double) total;
    }
}