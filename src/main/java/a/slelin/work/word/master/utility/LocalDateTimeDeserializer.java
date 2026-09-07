package a.slelin.work.word.master.utility;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.time.LocalDateTime;

public class LocalDateTimeDeserializer extends ValueDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser p,
                                     DeserializationContext ctxt) throws JacksonException {
        String str = p.getValueAsString();
        try {
            return LocalDateTime.parse(str, DateTimeUtil.UNIVERSE_DATETIME_FORMATTER);
        } catch (Exception e) {
            return LocalDateTime.parse(str);
        }
    }
}