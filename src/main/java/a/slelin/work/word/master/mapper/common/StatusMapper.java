package a.slelin.work.word.master.mapper.common;

import a.slelin.work.word.master.entity.Status;
import org.mapstruct.Mapper;
import org.mapstruct.Named;

@SuppressWarnings("unused")
@Mapper(config = MapstructConfig.class)
public interface StatusMapper {

    @Named("statusToDisplayName")
    default String toDisplayName(Status status) {
        return status == null ? null : status.getDisplayName();
    }

    default Status toStatus(String value) {
        return value == null ? null : Status.of(value);
    }
}