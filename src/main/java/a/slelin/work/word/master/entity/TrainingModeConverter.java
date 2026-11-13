package a.slelin.work.word.master.entity;

import jakarta.persistence.Converter;

@Converter
public class TrainingModeConverter extends StandardEnumConverter<TrainingMode> {

    public TrainingModeConverter() {
        super(TrainingMode.class);
    }
}
