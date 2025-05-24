package flyair.booking.dto.response;

import flyair.booking.model.Flight;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FlightResponse {
    
    private Long id;
    private String flightNumber;
    private AirportResponse departureAirport;
    private AirportResponse arrivalAirport;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private Integer durationMinutes;
    private Flight.FlightStatus status;
    private String gateNumber;
    private String terminal;
    private String aircraftType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}