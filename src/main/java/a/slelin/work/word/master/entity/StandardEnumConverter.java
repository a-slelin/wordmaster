package a.slelin.work.word.master.entity;

import a.slelin.work.word.master.utility.EnumUtil;
import a.slelin.work.word.master.utility.StandardEnum;
import jakarta.persistence.AttributeConverter;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public abstract class StandardEnumConverter<E extends Enum<E> & StandardEnum> implements AttributeConverter<E, String> {

    private final Class<E> enumClass;

    @Override
    public String convertToDatabaseColumn(E attribute) {
        return attribute == null ? null : attribute.getDisplayName();
    }

    @Override
    public E convertToEntityAttribute(String dbData) {
        return EnumUtil.of(enumClass, dbData);
    }
}
