package flyair.booking.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateAirportRequest {
    
    private String airportCode;
    private String airportName;
    private String city;
    private String country;
    private String countryCode;
    private String timeZone;
    private Double latitude;
    private Double longitude;

    public String getAirportCode() {
        return airportCode;
    }

    public String getAirportName() {
        return airportName;
    }
}