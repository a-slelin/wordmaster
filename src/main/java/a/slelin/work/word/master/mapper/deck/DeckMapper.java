package a.slelin.work.word.master.mapper.deck;

import a.slelin.work.word.master.dto.deck.*;
import a.slelin.work.word.master.entity.Deck;
import a.slelin.work.word.master.mapper.common.MapstructConfig;
import a.slelin.work.word.master.mapper.common.UuidMapper;
import a.slelin.work.word.master.mapper.language.LanguageMapper;
import a.slelin.work.word.master.mapper.tag.TagMapper;
import a.slelin.work.word.master.mapper.user.UserMapper;
import org.mapstruct.*;

import java.util.UUID;

@SuppressWarnings("unused")
@Mapper(config = MapstructConfig.class,
        uses = {UuidMapper.class, UserMapper.class, LanguageMapper.class, TagMapper.class})
public interface DeckMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "sourceDeck", ignore = true)
    @Mapping(target = "sourceLanguage", ignore = true)
    @Mapping(target = "targetLanguage", ignore = true)
    @Mapping(target = "isOfficial", constant = "false")
    @Mapping(target = "likesCount", constant = "0L")
    @Mapping(target = "copiesCount", constant = "0L")
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "deckLikes", ignore = true)
    @Mapping(target = "trainingSessions", ignore = true)
    Deck toEntity(DeckCreateRequest request);

    @Mapping(target = "id", source = "deck.id")
    @Mapping(target = "title", source = "deck.title")
    @Mapping(target = "description", source = "deck.description")
    @Mapping(target = "icon", source = "deck.icon")
    @Mapping(target = "color", source = "deck.color")
    @Mapping(target = "owner", source = "deck.owner")
    @Mapping(target = "sourceLanguage", source = "deck.sourceLanguage")
    @Mapping(target = "targetLanguage", source = "deck.targetLanguage")
    @Mapping(target = "isPublic", source = "deck.isPublic")
    @Mapping(target = "isOfficial", source = "deck.isOfficial")
    @Mapping(target = "owned", source = "owned")
    @Mapping(target = "likedByMe", source = "likedByMe")
    @Mapping(target = "likesCount", source = "deck.likesCount")
    @Mapping(target = "copiesCount", source = "deck.copiesCount")
    @Mapping(target = "cardsCount", source = "cardsCount")
    @Mapping(target = "tags", source = "deck.tags")
    @Mapping(target = "sourceDeckId", source = "deck.sourceDeck.id")
    @Mapping(target = "progress", source = "progress")
    @Mapping(target = "createdAt", source = "deck.createdAt")
    @Mapping(target = "updatedAt", source = "deck.updatedAt")
    DeckResponse toDto(Deck deck, long cardsCount, boolean owned, boolean likedByMe, DeckProgressResponse progress);

    @Mapping(target = "id", source = "deck.id")
    @Mapping(target = "title", source = "deck.title")
    @Mapping(target = "description", source = "deck.description")
    @Mapping(target = "icon", source = "deck.icon")
    @Mapping(target = "color", source = "deck.color")
    @Mapping(target = "sourceLanguage", source = "deck.sourceLanguage")
    @Mapping(target = "targetLanguage", source = "deck.targetLanguage")
    @Mapping(target = "isPublic", source = "deck.isPublic")
    @Mapping(target = "isOfficial", source = "deck.isOfficial")
    @Mapping(target = "cardsCount", source = "cardsCount")
    @Mapping(target = "dueCount", source = "dueCount")
    @Mapping(target = "knownCount", source = "knownCount")
    @Mapping(target = "percentLearned", expression = "java(calculatePercentLearned(cardsCount, knownCount))")
    @Mapping(target = "tags", source = "deck.tags")
    @Mapping(target = "sourceDeckId", source = "deck.sourceDeck.id")
    @Mapping(target = "updatedAt", source = "deck.updatedAt")
    DeckSummaryResponse toSummaryDto(Deck deck, long cardsCount, long dueCount, long knownCount);

    @Mapping(target = "id", source = "deck.id")
    @Mapping(target = "title", source = "deck.title")
    @Mapping(target = "description", source = "deck.description")
    @Mapping(target = "icon", source = "deck.icon")
    @Mapping(target = "color", source = "deck.color")
    @Mapping(target = "ownerUsername", source = "deck.owner.username")
    @Mapping(target = "sourceLanguage", source = "deck.sourceLanguage")
    @Mapping(target = "targetLanguage", source = "deck.targetLanguage")
    @Mapping(target = "isOfficial", source = "deck.isOfficial")
    @Mapping(target = "likedByMe", source = "likedByMe")
    @Mapping(target = "owned", source = "owned")
    @Mapping(target = "cardsCount", source = "cardsCount")
    @Mapping(target = "likesCount", source = "deck.likesCount")
    @Mapping(target = "copiesCount", source = "deck.copiesCount")
    @Mapping(target = "tags", source = "deck.tags")
    @Mapping(target = "createdAt", source = "deck.createdAt")
    PublicDeckCatalogItemResponse toCatalogDto(Deck deck, long cardsCount, boolean owned, boolean likedByMe);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "owner", ignore = true)
    @Mapping(target = "sourceDeck", ignore = true)
    @Mapping(target = "sourceLanguage", ignore = true)
    @Mapping(target = "targetLanguage", ignore = true)
    @Mapping(target = "isOfficial", ignore = true)
    @Mapping(target = "likesCount", ignore = true)
    @Mapping(target = "copiesCount", ignore = true)
    @Mapping(target = "tags", ignore = true)
    @Mapping(target = "cards", ignore = true)
    @Mapping(target = "deckLikes", ignore = true)
    @Mapping(target = "trainingSessions", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patch(@MappingTarget Deck deck, DeckUpdateRequest request);

    @Mapping(target = "deckId", source = "deckId")
    @Mapping(target = "totalCards", source = "totalCards")
    @Mapping(target = "newCards", expression = "java(Math.max(0L, totalCards - learningCards - knownCards))")
    @Mapping(target = "learningCards", source = "learningCards")
    @Mapping(target = "knownCards", source = "knownCards")
    @Mapping(target = "dueCards", source = "dueCards")
    @Mapping(target = "percentLearned", expression = "java(calculatePercentLearned(totalCards, knownCards))")
    DeckProgressResponse toProgressDto(UUID deckId, long totalCards, long learningCards,
                                       long knownCards, long dueCards);

    @Mapping(target = "deckId", source = "deckId")
    @Mapping(target = "liked", source = "liked")
    @Mapping(target = "likesCount", source = "likesCount")
    DeckLikeResponse toLikeDto(UUID deckId, boolean liked, long likesCount);

    default Integer calculatePercentLearned(long totalCards, long knownCards) {
        if (totalCards == 0) {
            return 0;
        }

        return (int) Math.min(100, Math.round((knownCards * 100.0) / totalCards));
    }
}
