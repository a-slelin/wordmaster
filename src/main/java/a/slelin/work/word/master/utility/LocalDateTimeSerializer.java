package a.slelin.work.word.master.utility;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.time.LocalDateTime;

public class LocalDateTimeSerializer extends ValueSerializer<LocalDateTime> {

    @Override
    public void serialize(LocalDateTime value,
                          JsonGenerator gen,
                          SerializationContext ctxt) throws JacksonException {
        gen.writeString(value.format(DateTimeUtil.UNIVERSE_DATETIME_FORMATTER));
    }
}