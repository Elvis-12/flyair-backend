package flyair.booking.controller;

import flyair.booking.dto.request.*;
import flyair.booking.dto.response.ApiResponse;
import flyair.booking.dto.response.AuthenticationResponse;
import flyair.booking.service.AuthenticationService;
import flyair.booking.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;
    private final UserService userService;
    private static final Logger log = LoggerFactory.getLogger(AuthenticationController.class);

    @PostMapping("/register")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> register(
            @Valid @RequestBody RegisterRequest request) {
        AuthenticationResponse response = authenticationService.register(request);
        return ResponseEntity.ok(ApiResponse.success("User registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
            @Valid @RequestBody LoginRequest request) {
        log.info("Received login request for user: {}", request.getUsername());
        log.debug("Login request details: {}", request);
        
        try {
            AuthenticationResponse response = authenticationService.login(request);
            return ResponseEntity.ok(ApiResponse.success("Login successful", response));
        } catch (Exception e) {
            log.error("Login failed for user: {}. Error: {}", request.getUsername(), e.getMessage());
            throw e;
        }
    }

    @PostMapping("/verify-2fa")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> verifyTwoFactor(
            @Valid @RequestBody TwoFactorVerificationRequest request) {
        AuthenticationResponse response = authenticationService.verifyTwoFactor(request);
        return ResponseEntity.ok(ApiResponse.success("Two-factor verification successful", response));
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request) {
        AuthenticationResponse response = authenticationService.refreshToken(request.getRefreshToken());
        return ResponseEntity.ok(ApiResponse.success("Token refreshed successfully", response));
    }

    @PostMapping("/admin/register")
    public ResponseEntity<ApiResponse<AuthenticationResponse>> registerAdmin(
            @Valid @RequestBody RegisterRequest request) {
        AuthenticationResponse response = authenticationService.registerAdmin(request);
        return ResponseEntity.ok(ApiResponse.success("Admin registered successfully", response));
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse<String>> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        userService.requestPasswordReset(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success("Password reset email sent", "Please check your email for reset instructions"));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse<String>> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        userService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.success("Password reset successful", "Your password has been reset successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<String>> logout(@RequestHeader("Authorization") String token) {
        if (token != null && token.startsWith("Bearer ")) {
            authenticationService.logout(token.substring(7));
        }
        return ResponseEntity.ok(ApiResponse.success("Logout successful", "You have been logged out successfully"));
    }
}