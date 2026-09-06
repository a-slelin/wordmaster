package a.slelin.work.word.master.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class ComplexityConverter implements AttributeConverter<Complexity, String> {

    @Override
    public String convertToDatabaseColumn(Complexity attribute) {
        return attribute.getDisplayName();
    }

    @Override
    public Complexity convertToEntityAttribute(String dbData) {
        return Complexity.of(dbData);
    }
}
