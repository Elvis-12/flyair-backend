package flyair.booking.seeder;

import flyair.booking.model.User;
import flyair.booking.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Slf4j
public class AdminSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AdminSeeder(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Checking if admin user exists...");
        
        // Check if admin already exists
        if (userRepository.findByEmail("admin@flyair.com").isEmpty()) {
            log.info("Admin user not found, creating default admin...");
            
            String rawPassword = "Admin@123";
            String encodedPassword = passwordEncoder.encode(rawPassword);
            
            log.info("Creating admin user with username: admin, email: admin@flyair.com");
            
            User admin = User.builder()
                    .username("admin")
                    .email("admin@flyair.com")
                    .password(encodedPassword)
                    .firstName("System")
                    .lastName("Administrator")
                    .role(User.Role.ADMIN)
                    .isEnabled(true)
                    .isAccountNonExpired(true)
                    .isAccountNonLocked(true)
                    .isCredentialsNonExpired(true)
                    .isTwoFactorEnabled(false)
                    .build();

            User savedAdmin = userRepository.save(admin);
            log.info("Default administrator created successfully with ID: {}", savedAdmin.getId());
            
            // Verify the admin was created correctly
            verifyAdminUser(savedAdmin);
        } else {
            log.info("Admin user already exists, skipping creation");
            // Verify existing admin
            userRepository.findByEmail("admin@flyair.com").ifPresent(this::verifyAdminUser);
        }
    }
    
    private void verifyAdminUser(User admin) {
        log.info("Verifying admin user:");
        log.info("ID: {}", admin.getId());
        log.info("Username: {}", admin.getUsername());
        log.info("Email: {}", admin.getEmail());
        log.info("Role: {}", admin.getRole());
        log.info("Enabled: {}", admin.isEnabled());
        log.info("Account Non Expired: {}", admin.isAccountNonExpired());
        log.info("Account Non Locked: {}", admin.isAccountNonLocked());
        log.info("Credentials Non Expired: {}", admin.isCredentialsNonExpired());
        log.info("2FA Enabled: {}", admin.isTwoFactorEnabled());
    }
} 