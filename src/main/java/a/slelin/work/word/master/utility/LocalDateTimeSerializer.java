package a.slelin.work.word.master.utility;

import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ValueSerializer;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

public class LocalDateTimeSerializer extends ValueSerializer<LocalDateTime> {

    @Override
    public void serialize(LocalDateTime value,
                          JsonGenerator gen,
                          SerializationContext ctxt) throws JacksonException {
        // Timestamps are kept in the time zone of the application (the JVM default one): the offset is sent
        // explicitly, otherwise a client in another time zone reads them shifted by several hours.
        gen.writeString(value.atZone(ZoneId.systemDefault()).toOffsetDateTime()
                .truncatedTo(ChronoUnit.SECONDS)
                .format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
    }
}