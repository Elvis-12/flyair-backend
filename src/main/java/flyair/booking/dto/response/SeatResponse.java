package flyair.booking.dto.response;

import flyair.booking.model.Seat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SeatResponse {
    
    private Long id;
    private String seatNumber;
    private Seat.SeatClass seatClass;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}