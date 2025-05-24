package flyair.booking.service;

import flyair.booking.dto.request.ChangePasswordRequest;
import flyair.booking.dto.request.ResetPasswordRequest;
import flyair.booking.dto.request.UpdateUserRequest;
import flyair.booking.dto.response.UserResponse;
import flyair.booking.exception.ResourceNotFoundException;
import flyair.booking.exception.BadRequestException;
import flyair.booking.model.User;
import flyair.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class UserService {
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final TwoFactorService twoFactorService;
    private final ModelMapper modelMapper;
    
    /**
     * Get all users with pagination
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(user -> modelMapper.map(user, UserResponse.class));
    }
    
    /**
     * Search users
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> searchUsers(String searchTerm, Pageable pageable) {
        return userRepository.searchUsers(searchTerm, pageable)
                .map(user -> modelMapper.map(user, UserResponse.class));
    }
    
    /**
     * Get user by ID
     */
    @Transactional(readOnly = true)
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return modelMapper.map(user, UserResponse.class);
    }
    
    /**
     * Get current authenticated user
     */
    @Transactional(readOnly = true)
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with username: " + username));
        
        return modelMapper.map(user, UserResponse.class);
    }
    
    /**
     * Update user profile
     */
    public UserResponse updateUser(Long id, UpdateUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        // Check if email is being changed and if it's already taken
        if (!user.getEmail().equals(request.getEmail()) && 
            userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email is already taken");
        }
        
        // Check if username is being changed and if it's already taken
        if (!user.getUsername().equals(request.getUsername()) && 
            userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }
        
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setUsername(request.getUsername());
        user.setPhoneNumber(request.getPhoneNumber());
        
        User savedUser = userRepository.save(user);
        log.info("User updated successfully: {}", savedUser.getUsername());
        
        return modelMapper.map(savedUser, UserResponse.class);
    }
    
    /**
     * Change user password
     */
    public void changePassword(ChangePasswordRequest request) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Verify current password
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is incorrect");
        }
        
        // Update password
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
        
        log.info("Password changed successfully for user: {}", username);
    }
    
    /**
     * Request password reset
     */
    public void requestPasswordReset(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
        
        // Generate reset token
        String resetToken = UUID.randomUUID().toString();
        user.setResetToken(resetToken);
        user.setResetTokenExpiry(LocalDateTime.now().plusHours(1)); // Token expires in 1 hour
        
        userRepository.save(user);
        
        // Send reset email
        emailService.sendPasswordResetEmail(user.getEmail(), resetToken, user.getFirstName());
        
        log.info("Password reset requested for user: {}", email);
    }
    
    /**
     * Reset password using token
     */
    public void resetPassword(ResetPasswordRequest request) {
        User user = userRepository.findByResetToken(request.getToken())
                .orElseThrow(() -> new BadRequestException("Invalid reset token"));
        
        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Reset token has expired");
        }
        
        // Update password and clear reset token
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);
        
        userRepository.save(user);
        
        log.info("Password reset successfully for user: {}", user.getUsername());
    }
    
    /**
     * Enable two-factor authentication
     */
    public String enableTwoFactorAuth() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (user.isTwoFactorEnabled()) {
            throw new BadRequestException("Two-factor authentication is already enabled");
        }
        
        // Generate secret
        String secret = twoFactorService.generateSecret();
        user.setTwoFactorSecret(secret);
        
        userRepository.save(user);
        
        try {
            return twoFactorService.generateQrCodeDataUrl(user.getEmail(), secret);
        } catch (Exception e) {
            log.error("Failed to generate QR code for user: {}", username, e);
            throw new RuntimeException("Failed to generate QR code", e);
        }
    }
    
    /**
     * Confirm and activate two-factor authentication
     */
    public void confirmTwoFactorAuth(String code) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (user.getTwoFactorSecret() == null) {
            throw new BadRequestException("Two-factor authentication setup not initiated");
        }
        
        // Verify the code
        if (!twoFactorService.verifyCode(user.getTwoFactorSecret(), code)) {
            throw new BadRequestException("Invalid verification code");
        }
        
        // Enable 2FA
        user.setTwoFactorEnabled(true);
        userRepository.save(user);
        
        // Send confirmation email
        emailService.send2FASetupEmail(user.getEmail(), user.getFirstName());
        
        log.info("Two-factor authentication enabled for user: {}", username);
    }
    
    /**
     * Disable two-factor authentication
     */
    public void disableTwoFactorAuth(String code) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String username = authentication.getName();
        
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        if (!user.isTwoFactorEnabled()) {
            throw new BadRequestException("Two-factor authentication is not enabled");
        }
        
        // Verify the code
        if (!twoFactorService.verifyCode(user.getTwoFactorSecret(), code)) {
            throw new BadRequestException("Invalid verification code");
        }
        
        // Disable 2FA
        user.setTwoFactorEnabled(false);
        user.setTwoFactorSecret(null);
        userRepository.save(user);
        
        log.info("Two-factor authentication disabled for user: {}", username);
    }
    
    /**
     * Delete user account
     */
    public void deleteUser(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        
        userRepository.delete(user);
        log.info("User deleted successfully: {}", user.getUsername());
    }
    
    /**
     * Get users by role with pagination
     */
    @Transactional(readOnly = true)
    public Page<UserResponse> getUsersByRole(User.Role role, Pageable pageable) {
        return userRepository.findByRole(role, pageable)
                .map(user -> modelMapper.map(user, UserResponse.class));
    }
    
    /**
     * Get user statistics
     */
    @Transactional(readOnly = true)
    public UserStatsResponse getUserStats() {
        long totalUsers = userRepository.count();
        long adminUsers = userRepository.countByRole(User.Role.ADMIN);
        long regularUsers = userRepository.countByRole(User.Role.USER);
        
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long newUsersLast30Days = userRepository.countUsersRegisteredBetween(thirtyDaysAgo, LocalDateTime.now());
        
        return UserStatsResponse.builder()
                .totalUsers(totalUsers)
                .adminUsers(adminUsers)
                .regularUsers(regularUsers)
                .newUsersLast30Days(newUsersLast30Days)
                .build();
    }
    
    @lombok.Data
    @lombok.Builder
    public static class UserStatsResponse {
        private long totalUsers;
        private long adminUsers;
        private long regularUsers;
        private long newUsersLast30Days;
    }
}