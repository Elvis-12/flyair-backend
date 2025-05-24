package flyair.booking.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightSearchRequest {
    
    private String departureAirportCode;
    private String arrivalAirportCode;
    private LocalDateTime departureDate;
    private String searchTerm;
}