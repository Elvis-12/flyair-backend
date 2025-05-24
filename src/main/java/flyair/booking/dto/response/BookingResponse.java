package flyair.booking.dto.response;

import flyair.booking.model.Booking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BookingResponse {
    
    private Long id;
    private String bookingReference;
    private UserResponse user;
    private FlightResponse flight;
    private BigDecimal totalAmount;
    private Booking.BookingStatus bookingStatus;
    private Booking.PaymentStatus paymentStatus;
    private LocalDateTime bookingDate;
    private LocalDateTime paymentDate;
    private List<TicketResponse> tickets;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}