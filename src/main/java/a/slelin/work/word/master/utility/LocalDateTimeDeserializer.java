package a.slelin.work.word.master.utility;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeParseException;

public class LocalDateTimeDeserializer extends ValueDeserializer<LocalDateTime> {

    @Override
    public LocalDateTime deserialize(JsonParser p,
                                     DeserializationContext ctxt) throws JacksonException {
        String str = p.getValueAsString();
        try {
            // ISO-8601 with an offset ("2026-10-03T12:00:00+03:00") - converted to the application time zone.
            return OffsetDateTime.parse(str).atZoneSameInstant(ZoneId.systemDefault()).toLocalDateTime();
        } catch (DateTimeParseException withoutOffset) {
            try {
                return LocalDateTime.parse(str, DateTimeUtil.UNIVERSE_DATETIME_FORMATTER);
            } catch (DateTimeParseException notUniverse) {
                return LocalDateTime.parse(str);
            }
        }
    }
}