package a.slelin.work.word.master.mapper.tag;

import a.slelin.work.word.master.dto.tag.TagRequest;
import a.slelin.work.word.master.dto.tag.TagResponse;
import a.slelin.work.word.master.entity.Tag;
import a.slelin.work.word.master.mapper.common.MapstructConfig;
import org.mapstruct.*;

import java.util.List;

@SuppressWarnings("unused")
@Mapper(config = MapstructConfig.class)
public interface TagMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "decks", ignore = true)
    Tag toEntity(TagRequest request);

    TagResponse toDto(Tag tag);

    List<TagResponse> toDtoList(List<Tag> tags);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "decks", ignore = true)
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    Tag patch(@MappingTarget Tag tag, TagRequest request);
}