package a.slelin.work.word.master.service;

import a.slelin.work.word.master.dto.auth.AuthResponse;
import a.slelin.work.word.master.dto.auth.LoginRequest;
import a.slelin.work.word.master.dto.auth.RefreshTokenRequest;
import a.slelin.work.word.master.dto.auth.RegisterRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

public interface AuthService {

    @Valid
    @NotNull
    AuthResponse register(@NotNull @Valid RegisterRequest request);

    @Valid
    @NotNull
    AuthResponse login(@NotNull @Valid LoginRequest request);

    @Valid
    @NotNull
    AuthResponse refresh(@NotNull @Valid RefreshTokenRequest request);

    void logout(@NotNull @Valid RefreshTokenRequest request);
}
