package a.slelin.work.word.master.mapper.language;

import a.slelin.work.word.master.dto.language.LanguageRequest;
import a.slelin.work.word.master.dto.language.LanguageResponse;
import a.slelin.work.word.master.entity.Language;
import a.slelin.work.word.master.mapper.common.MapstructConfig;
import org.mapstruct.*;

import java.util.List;

@SuppressWarnings("unused")
@Mapper(config = MapstructConfig.class)
public interface LanguageMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "decksBySourceLanguage", ignore = true)
    @Mapping(target = "decksByTargetLanguage", ignore = true)
    Language toEntity(LanguageRequest request);

    LanguageResponse toDto(Language language);

    List<LanguageResponse> toDtoList(List<Language> languages);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "decksBySourceLanguage", ignore = true)
    @Mapping(target = "decksByTargetLanguage", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void patch(@MappingTarget Language language, LanguageRequest request);
}
