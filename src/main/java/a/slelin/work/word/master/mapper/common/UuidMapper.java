package a.slelin.work.word.master.mapper.common;

import org.mapstruct.Mapper;

import java.util.UUID;

@SuppressWarnings("unused")
@Mapper(config = MapstructConfig.class)
public interface UuidMapper {

    default String map(UUID id) {
        return id == null ? null : id.toString();
    }

    default UUID map(String id) {
        return (id == null || id.isBlank()) ? null : UUID.fromString(id);
    }
}