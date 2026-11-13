package a.slelin.work.word.master.config;

import a.slelin.work.word.master.entity.Role;
import a.slelin.work.word.master.entity.User;
import a.slelin.work.word.master.entity.UserStats;
import a.slelin.work.word.master.repository.UserRepository;
import a.slelin.work.word.master.repository.UserStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Creates the admin account from "wordmaster.admin.*" properties on start-up if it does not exist yet.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private final WordMasterProperties properties;

    private final UserRepository userRepository;

    private final UserStatsRepository userStatsRepository;

    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        WordMasterProperties.Admin admin = properties.admin();
        if (!admin.isConfigured()) {
            return;
        }

        if (userRepository.existsByUsernameIgnoreCase(admin.username())
                || userRepository.existsByEmailIgnoreCase(admin.email())) {
            return;
        }

        User user = userRepository.save(User.builder()
                .username(admin.username())
                .email(admin.email())
                .passwordHash(passwordEncoder.encode(admin.password()))
                .role(Role.ADMIN)
                .build());
        userStatsRepository.save(UserStats.empty(user.getId()));
        log.info("Admin account '{}' has been created.", admin.username());
    }
}
