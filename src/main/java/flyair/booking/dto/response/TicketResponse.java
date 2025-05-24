package flyair.booking.dto.response;

import flyair.booking.model.Ticket;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketResponse {
    
    private Long id;
    private String ticketNumber;
    private FlightSeatResponse flightSeat;
    private String passengerName;
    private String passengerEmail;
    private String passengerPhone;
    private String passportNumber;
    private Ticket.TicketStatus ticketStatus;
    private LocalDateTime checkInTime;
    private LocalDateTime boardingTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}