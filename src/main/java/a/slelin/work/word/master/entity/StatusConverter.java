package a.slelin.work.word.master.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StatusConverter implements AttributeConverter<Status, String> {

    @Override
    public String convertToDatabaseColumn(Status attribute) {
        return attribute.getDisplayName();
    }

    @Override
    public Status convertToEntityAttribute(String dbData) {
        return Status.of(dbData);
    }
}
