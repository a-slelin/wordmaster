package a.slelin.work.word.master.config;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.time.Duration;
import java.util.List;

/**
 * Application settings (prefix "wordmaster").
 */
@Validated
@ConfigurationProperties(prefix = "wordmaster")
public record WordMasterProperties(@NotBlank String timeZone,
                                   @NotNull @Valid Jwt jwt,
                                   @NotNull @Valid Cors cors,
                                   @NotNull @Valid Admin admin) {

    public record Jwt(@NotBlank @Size(min = 32) String secret,
                      @NotBlank String issuer,
                      @NotNull Duration accessTokenTtl,
                      @NotNull Duration refreshTokenTtl) {
    }

    public record Cors(@NotNull List<String> allowedOrigins) {
    }

    /**
     * Optional account created on start-up if it does not exist yet (empty values - skip).
     */
    public record Admin(String username,
                        String email,
                        String password) {

        public boolean isConfigured() {
            return username != null && !username.isBlank()
                    && email != null && !email.isBlank()
                    && password != null && !password.isBlank();
        }
    }
}
