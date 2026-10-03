package a.slelin.work.word.master.mapper.common;

import a.slelin.work.word.master.entity.Grade;
import a.slelin.work.word.master.entity.TrainingDirection;
import a.slelin.work.word.master.entity.TrainingMode;
import org.mapstruct.Mapper;

@SuppressWarnings("unused")
@Mapper(config = MapstructConfig.class)
public interface StandardEnumMapper {

    default String toDisplayName(TrainingMode mode) {
        return mode == null ? null : mode.getDisplayName();
    }

    default String toDisplayName(TrainingDirection direction) {
        return direction == null ? null : direction.getDisplayName();
    }

    default String toDisplayName(Grade grade) {
        return grade == null ? null : grade.getDisplayName();
    }
}
