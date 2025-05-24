package flyair.booking.security;

import flyair.booking.model.User;
import flyair.booking.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional
    public UserDetails loadUserByUsername(String usernameOrEmail) throws UsernameNotFoundException {
        log.info("Attempting to load user by username/email: {}", usernameOrEmail);
        
        if (usernameOrEmail == null || usernameOrEmail.trim().isEmpty()) {
            log.error("Username/email is null or empty");
            throw new UsernameNotFoundException("Username/email cannot be empty");
        }
        
        try {
            User user = userRepository.findByUsernameOrEmail(usernameOrEmail, usernameOrEmail)
                    .orElseThrow(() -> {
                        log.error("User not found with username/email: {}", usernameOrEmail);
                        return new UsernameNotFoundException("User not found with username or email: " + usernameOrEmail);
                    });
            
            log.info("User found: {} (ID: {})", user.getUsername(), user.getId());
            log.debug("User details - Role: {}, Enabled: {}, Account Non Expired: {}, Account Non Locked: {}, Credentials Non Expired: {}",
                    user.getRole(),
                    user.isEnabled(),
                    user.isAccountNonExpired(),
                    user.isAccountNonLocked(),
                    user.isCredentialsNonExpired());
            
            return user;
        } catch (Exception e) {
            log.error("Error loading user by username/email: {}", usernameOrEmail, e);
            throw new UsernameNotFoundException("Error loading user: " + e.getMessage());
        }
    }
}