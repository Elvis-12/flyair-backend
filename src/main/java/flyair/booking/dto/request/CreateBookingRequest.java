package flyair.booking.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBookingRequest {
    
    private Long flightId;
    private List<Long> flightSeatIds;
    private List<PassengerRequest> passengers;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class PassengerRequest {
        private String passengerName;
        private String passengerEmail;
        private String passengerPhone;
        private String passportNumber;
    }
}