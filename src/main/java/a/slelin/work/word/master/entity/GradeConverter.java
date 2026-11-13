package a.slelin.work.word.master.entity;

import jakarta.persistence.Converter;

@Converter
public class GradeConverter extends StandardEnumConverter<Grade> {

    public GradeConverter() {
        super(Grade.class);
    }
}
