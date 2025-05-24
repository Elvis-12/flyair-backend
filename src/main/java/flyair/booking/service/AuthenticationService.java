package flyair.booking.service;

import flyair.booking.dto.request.LoginRequest;
import flyair.booking.dto.request.RegisterRequest;
import flyair.booking.dto.request.ResetPasswordRequest;
import flyair.booking.dto.request.TwoFactorVerificationRequest;
import flyair.booking.dto.response.AuthenticationResponse;
import flyair.booking.dto.response.UserResponse;
import flyair.booking.exception.BadRequestException;
import flyair.booking.exception.UnauthorizedException;
import flyair.booking.model.User;
import flyair.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AuthenticationService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final TwoFactorService twoFactorService;
    private final EmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final ModelMapper modelMapper;
    
    @Value("${app.jwt.secret}")
    private String jwtSecret;
    
    /**
     * Register a new user
     */
    public AuthenticationResponse register(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already taken");
        }
        
        // Create new user
        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .role(request.getRole() != null ? request.getRole() : User.Role.USER)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .isTwoFactorEnabled(false)
                .build();
        
        User savedUser = userRepository.save(user);
        
        // Generate JWT tokens
        String accessToken = jwtService.generateToken(savedUser);
        String refreshToken = jwtService.generateRefreshToken(savedUser);
        
        // Send welcome email
        emailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getFirstName());
        
        log.info("User registered successfully: {}", savedUser.getUsername());
        
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(modelMapper.map(savedUser, UserResponse.class))
                .requiresTwoFactor(false)
                .build();
    }
    
    /**
     * Authenticate user login
     */
    public AuthenticationResponse login(LoginRequest request) {
        log.info("Attempting login for user: {}", request.getUsername());
        
        try {
            // Authenticate user credentials
            UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                    request.getUsername(),
                    request.getPassword()
            );
            
            log.debug("Created authentication token for user: {}", request.getUsername());
            authenticationManager.authenticate(authToken);
            log.debug("Authentication successful for user: {}", request.getUsername());
            
            // Find user by username or email
            User user = userRepository.findByUsernameOrEmail(request.getUsername(), request.getUsername())
                    .orElseThrow(() -> {
                        log.error("User not found after authentication: {}", request.getUsername());
                        return new UnauthorizedException("Invalid credentials");
                    });
            
            log.info("User found: {}", user.getUsername());
            
            // Check if account is enabled
            if (!user.isEnabled()) {
                log.error("Account is disabled for user: {}", user.getUsername());
                throw new UnauthorizedException("Account is disabled");
            }
            
            // Check if 2FA is enabled
            if (user.isTwoFactorEnabled()) {
                log.info("2FA is enabled for user: {}", user.getUsername());
                // Return response indicating 2FA is required
                return AuthenticationResponse.builder()
                        .requiresTwoFactor(true)
                        .temporaryToken(generateTemporaryToken(user))
                        .message("Two-factor authentication required")
                        .build();
            }
            
            // Generate JWT tokens
            String accessToken = jwtService.generateToken(user);
            String refreshToken = jwtService.generateRefreshToken(user);
            
            log.info("User logged in successfully: {}", user.getUsername());
            
            return AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(jwtService.getExpirationTime())
                    .user(modelMapper.map(user, UserResponse.class))
                    .requiresTwoFactor(false)
                    .build();
        } catch (Exception e) {
            log.error("Authentication failed for user: {}. Error: {}", request.getUsername(), e.getMessage());
            throw new UnauthorizedException("Invalid credentials");
        }
    }
    
    /**
     * Verify two-factor authentication and complete login
     */
    public AuthenticationResponse verifyTwoFactor(TwoFactorVerificationRequest request) {
        // Verify temporary token
        String username = extractUsernameFromTemporaryToken(request.getTemporaryToken());
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("Invalid temporary token"));
        
        // Verify 2FA code
        if (!twoFactorService.verifyCode(user.getTwoFactorSecret(), request.getCode())) {
            throw new UnauthorizedException("Invalid verification code");
        }
        
        // Generate JWT tokens
        String accessToken = jwtService.generateToken(user);
        String refreshToken = jwtService.generateRefreshToken(user);
        
        log.info("Two-factor authentication successful for user: {}", user.getUsername());
        
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(modelMapper.map(user, UserResponse.class))
                .requiresTwoFactor(false)
                .build();
    }
    
    /**
     * Refresh access token
     */
    public AuthenticationResponse refreshToken(String refreshToken) {
        if (refreshToken == null) {
            throw new UnauthorizedException("Refresh token is required");
        }
        
        String username = jwtService.extractUsername(refreshToken);
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
        
        if (!jwtService.isTokenValid(refreshToken, user)) {
            throw new UnauthorizedException("Invalid refresh token");
        }
        
        String newAccessToken = jwtService.generateToken(user);
        String newRefreshToken = jwtService.generateRefreshToken(user);
        
        log.info("Token refreshed for user: {}", user.getUsername());
        
        return AuthenticationResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(modelMapper.map(user, UserResponse.class))
                .requiresTwoFactor(false)
                .build();
    }
    
    /**
     * Register admin user (only for initial setup)
     */
    public AuthenticationResponse registerAdmin(RegisterRequest request) {
        // Check if any admin already exists
        if (userRepository.countByRole(User.Role.ADMIN) > 0) {
            throw new BadRequestException("Admin user already exists. Contact existing admin for new admin creation.");
        }
        
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already taken");
        }
        
        // Create admin user
        User admin = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .role(User.Role.ADMIN)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .isTwoFactorEnabled(false)
                .build();
        
        User savedAdmin = userRepository.save(admin);
        
        // Generate JWT tokens
        String accessToken = jwtService.generateToken(savedAdmin);
        String refreshToken = jwtService.generateRefreshToken(savedAdmin);
        
        // Send welcome email
        emailService.sendWelcomeEmail(savedAdmin.getEmail(), savedAdmin.getFirstName());
        
        log.info("Admin user registered successfully: {}", savedAdmin.getUsername());
        
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtService.getExpirationTime())
                .user(modelMapper.map(savedAdmin, UserResponse.class))
                .requiresTwoFactor(false)
                .build();
    }
    
    /**
     * Create admin user (by existing admin)
     */
    public UserResponse createAdmin(RegisterRequest request) {
        // Check if username already exists
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }
        
        // Check if email already exists
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already taken");
        }
        
        // Create admin user
        User admin = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .phoneNumber(request.getPhoneNumber())
                .role(User.Role.ADMIN)
                .isEnabled(true)
                .isAccountNonExpired(true)
                .isAccountNonLocked(true)
                .isCredentialsNonExpired(true)
                .isTwoFactorEnabled(false)
                .build();
        
        User savedAdmin = userRepository.save(admin);
        
        // Send welcome email
        emailService.sendWelcomeEmail(savedAdmin.getEmail(), savedAdmin.getFirstName());
        
        log.info("Admin user created successfully: {}", savedAdmin.getUsername());
        
        return modelMapper.map(savedAdmin, UserResponse.class);
    }
    
    /**
     * Generate temporary token for 2FA process
     */
    private String generateTemporaryToken(User user) {
        // Create a temporary token with shorter expiration (5 minutes)
        return buildToken(
                Map.of("temp", true),
                user,
                300000L // 5 minutes
        );
    }
    
    /**
     * Extract username from temporary token
     */
    private String extractUsernameFromTemporaryToken(String token) {
        try {
            return jwtService.extractUsername(token);
        } catch (Exception e) {
            throw new UnauthorizedException("Invalid temporary token");
        }
    }
    
    /**
     * Build token with custom claims and expiration
     */
    public String buildToken(
            Map<String, Object> extraClaims,
            org.springframework.security.core.userdetails.UserDetails userDetails,
            long expiration
    ) {
        return io.jsonwebtoken.Jwts
                .builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSignInKey(), io.jsonwebtoken.SignatureAlgorithm.HS256)
                .compact();
    }
    
    private Key getSignInKey() {
        byte[] keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(jwtSecret);
        return io.jsonwebtoken.security.Keys.hmacShaKeyFor(keyBytes);
    }
    
    /**
     * Logout user (token blacklisting would be implemented here if needed)
     */
    public void logout(String token) {
        // In a production environment, you might want to implement token blacklisting
        // For now, we'll just log the logout
        try {
            String username = jwtService.extractUsername(token);
            log.info("User logged out: {}", username);
        } catch (Exception e) {
            log.warn("Invalid token provided for logout");
        }
    }
}