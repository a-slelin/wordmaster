package a.slelin.work.word.master.entity;

import jakarta.persistence.Converter;

@Converter
public class StatusConverter extends StandardEnumConverter<Status> {

    public StatusConverter() {
        super(Status.class);
    }
}
