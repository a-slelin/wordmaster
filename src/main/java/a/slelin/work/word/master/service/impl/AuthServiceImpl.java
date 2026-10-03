package a.slelin.work.word.master.service.impl;

import a.slelin.work.word.master.config.WordMasterProperties;
import a.slelin.work.word.master.dto.auth.AuthResponse;
import a.slelin.work.word.master.dto.auth.LoginRequest;
import a.slelin.work.word.master.dto.auth.RefreshTokenRequest;
import a.slelin.work.word.master.dto.auth.RegisterRequest;
import a.slelin.work.word.master.dto.user.UserResponse;
import a.slelin.work.word.master.entity.RefreshToken;
import a.slelin.work.word.master.entity.User;
import a.slelin.work.word.master.mapper.user.UserMapper;
import a.slelin.work.word.master.repository.RefreshTokenRepository;
import a.slelin.work.word.master.repository.UserRepository;
import a.slelin.work.word.master.security.TokenService;
import a.slelin.work.word.master.service.AuthService;
import a.slelin.work.word.master.service.GamificationService;
import a.slelin.work.word.master.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.UUID;

@Slf4j
@Service
@Validated
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthServiceImpl implements AuthService {

    private static final String TOKEN_TYPE = "Bearer";

    private final UserService userService;

    private final UserRepository userRepository;

    private final RefreshTokenRepository refreshTokenRepository;

    private final GamificationService gamificationService;

    private final TokenService tokenService;

    private final PasswordEncoder passwordEncoder;

    private final UserMapper userMapper;

    private final WordMasterProperties properties;

    private final Clock clock;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        UserResponse created = userService.register(request);
        User user = userService.getEntityById(UUID.fromString(created.id()));
        gamificationService.getOrCreate(user.getId());
        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        String login = request.usernameOrEmail().trim();
        User user = userRepository.findByUsernameIgnoreCaseOrEmailIgnoreCase(login, login)
                .orElseThrow(() -> new BadCredentialsException("Invalid username/email or password."));

        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid username/email or password.");
        }

        return issueTokens(user);
    }

    @Override
    @Transactional
    public AuthResponse refresh(RefreshTokenRequest request) {
        RefreshToken token = refreshTokenRepository.findByTokenHash(tokenService.hash(request.refreshToken()))
                .filter(t -> !Boolean.TRUE.equals(t.getRevoked()))
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now(clock)))
                .orElseThrow(() -> new BadCredentialsException("Refresh token is invalid or expired."));

        token.setRevoked(true);
        return issueTokens(token.getUser());
    }

    @Override
    @Transactional
    public void logout(RefreshTokenRequest request) {
        refreshTokenRepository.findByTokenHash(tokenService.hash(request.refreshToken()))
                .ifPresent(token -> token.setRevoked(true));
    }

    @Transactional
    @Scheduled(cron = "0 0 4 * * *")
    public void cleanUpTokens() {
        int deleted = refreshTokenRepository.deleteExpiredOrRevoked(LocalDateTime.now(clock));
        log.info("Removed {} expired or revoked refresh tokens.", deleted);
    }

    private AuthResponse issueTokens(User user) {
        String refreshToken = tokenService.generateRefreshToken();
        refreshTokenRepository.save(RefreshToken.builder()
                .user(user)
                .tokenHash(tokenService.hash(refreshToken))
                .expiresAt(LocalDateTime.now(clock).plus(properties.jwt().refreshTokenTtl()))
                .revoked(false)
                .build());

        return AuthResponse.builder()
                .accessToken(tokenService.createAccessToken(user))
                .refreshToken(refreshToken)
                .tokenType(TOKEN_TYPE)
                .expiresIn(tokenService.accessTokenTtlSeconds())
                .user(userMapper.toDto(user))
                .build();
    }
}
