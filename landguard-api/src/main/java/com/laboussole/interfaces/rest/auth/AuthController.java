package com.laboussole.interfaces.rest.auth;

import com.laboussole.domain.port.in.AuthenticateUserUseCase;
import com.laboussole.domain.port.in.GetCurrentUserUseCase;
import com.laboussole.domain.port.in.LogoutUseCase;
import com.laboussole.domain.port.in.RefreshAccessTokenUseCase;
import com.laboussole.domain.port.in.RegisterUserUseCase;
import com.laboussole.infrastructure.security.AuthenticatedPrincipal;
import com.laboussole.interfaces.rest.auth.dto.AuthResponse;
import com.laboussole.interfaces.rest.auth.dto.LoginRequest;
import com.laboussole.interfaces.rest.auth.dto.LogoutRequest;
import com.laboussole.interfaces.rest.auth.dto.RefreshRequest;
import com.laboussole.interfaces.rest.auth.dto.RegisterRequest;
import com.laboussole.interfaces.rest.auth.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "Account registration, login, refresh, logout, and current-user lookup.")
class AuthController {

    private final RegisterUserUseCase registerUseCase;
    private final AuthenticateUserUseCase authenticateUseCase;
    private final RefreshAccessTokenUseCase refreshUseCase;
    private final LogoutUseCase logoutUseCase;
    private final GetCurrentUserUseCase getCurrentUserUseCase;

    AuthController(
            RegisterUserUseCase registerUseCase,
            AuthenticateUserUseCase authenticateUseCase,
            RefreshAccessTokenUseCase refreshUseCase,
            LogoutUseCase logoutUseCase,
            GetCurrentUserUseCase getCurrentUserUseCase) {
        this.registerUseCase = registerUseCase;
        this.authenticateUseCase = authenticateUseCase;
        this.refreshUseCase = refreshUseCase;
        this.logoutUseCase = logoutUseCase;
        this.getCurrentUserUseCase = getCurrentUserUseCase;
    }

    @Operation(summary = "Register a new account and return an authenticated session.")
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        var result = registerUseCase.execute(new RegisterUserUseCase.Command(
                request.email(),
                request.fullName(),
                request.password(),
                request.role()));
        return ResponseEntity.status(HttpStatus.CREATED).body(AuthResponse.of(result.user(), result.tokens()));
    }

    @Operation(summary = "Authenticate with email + password.")
    @PostMapping("/login")
    public AuthResponse login(@Valid @RequestBody LoginRequest request) {
        var result = authenticateUseCase.execute(new AuthenticateUserUseCase.Command(
                request.email(),
                request.password()));
        return AuthResponse.of(result.user(), result.tokens());
    }

    @Operation(summary = "Rotate the refresh token and mint a fresh access token.")
    @PostMapping("/refresh")
    public AuthResponse refresh(@Valid @RequestBody RefreshRequest request) {
        var tokens = refreshUseCase.execute(new RefreshAccessTokenUseCase.Command(request.refreshToken()));
        return AuthResponse.tokensOnly(tokens);
    }

    @Operation(summary = "Revoke a refresh token. Idempotent.")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestBody(required = false) LogoutRequest request) {
        var token = request == null ? null : request.refreshToken();
        logoutUseCase.execute(new LogoutUseCase.Command(token));
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Return the authenticated principal's profile.")
    @SecurityRequirement(name = "bearerAuth")
    @GetMapping("/me")
    public UserResponse me(@AuthenticationPrincipal AuthenticatedPrincipal principal) {
        var user = getCurrentUserUseCase.execute(principal.userId());
        return UserResponse.from(user);
    }
}
