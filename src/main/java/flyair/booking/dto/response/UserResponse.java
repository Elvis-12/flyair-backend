package flyair.booking.dto.response;

import flyair.booking.model.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    
    private Long id;
    private String username;
    private String email;
    private String firstName;
    private String lastName;
    private String phoneNumber;
    private User.Role role;
    private boolean isEnabled;
    private boolean isTwoFactorEnabled;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}