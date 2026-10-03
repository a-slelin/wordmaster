package a.slelin.work.word.master.entity;

import jakarta.persistence.Converter;

@Converter
public class TrainingDirectionConverter extends StandardEnumConverter<TrainingDirection> {

    public TrainingDirectionConverter() {
        super(TrainingDirection.class);
    }
}
