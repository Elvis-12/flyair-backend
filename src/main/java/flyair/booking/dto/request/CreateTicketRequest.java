package flyair.booking.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateTicketRequest {
    
    private Long bookingId;
    private Long flightSeatId;
    private String passengerName;
    private String passengerEmail;
    private String passengerPhone;
    private String passportNumber;
}