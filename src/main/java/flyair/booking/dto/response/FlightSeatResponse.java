package flyair.booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightSeatResponse {
    
    private Long id;
    private FlightResponse flight;
    private SeatResponse seat;
    private BigDecimal price;
    private Boolean isAvailable;
    private Boolean isOccupied;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}