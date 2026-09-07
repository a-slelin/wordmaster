package a.slelin.work.word.master.utility;

import org.springframework.http.HttpMethod;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.ValueDeserializer;

public class HttpMethodDeserializer extends ValueDeserializer<HttpMethod> {

    @Override
    public HttpMethod deserialize(JsonParser p,
                                  DeserializationContext ctxt) throws JacksonException {
        String str = p.getValueAsString();
        return str == null ? null : HttpMethod.valueOf(str);
    }
}