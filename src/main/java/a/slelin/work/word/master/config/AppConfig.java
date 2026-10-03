package a.slelin.work.word.master.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.time.Clock;
import java.time.ZoneId;
import java.util.TimeZone;

@Configuration
@EnableScheduling
@EnableConfigurationProperties(WordMasterProperties.class)
public class AppConfig {

    /**
     * Single source of "now" for the application: days (streaks, daily goal, word of the day)
     * are counted in the configured time zone.
     */
    @Bean
    public Clock clock(WordMasterProperties properties) {
        ZoneId zone = ZoneId.of(properties.timeZone());
        // Audit timestamps (created/updated at) use the JVM default zone - keep it the same as the clock.
        TimeZone.setDefault(TimeZone.getTimeZone(zone));
        return Clock.system(zone);
    }
}
