package a.slelin.work.word.master.security;

import a.slelin.work.word.master.dto.error.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Writes {@link ErrorResponse} for errors that happen in the security filter chain (outside of controllers).
 */
@Component
@RequiredArgsConstructor
public class JsonErrorWriter {

    private final JsonMapper jsonMapper;

    public void write(HttpServletRequest request,
                      HttpServletResponse response,
                      HttpStatus status,
                      Exception exception,
                      String message) throws IOException {
        ErrorResponse body = ErrorResponse.buildDefault(exception)
                .path(request.getRequestURI())
                .httpMethod(HttpMethod.valueOf(request.getMethod()))
                .httpStatus(status)
                .message(message)
                .build();

        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        jsonMapper.writeValue(response.getOutputStream(), body);
    }
}
