package flyair.booking.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateBulkFlightSeatsRequest {

    private Long flightId;
    private List<FlightSeatDetails> seats;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FlightSeatDetails {
        private Long seatId;
        private BigDecimal price;
        private Boolean isAvailable = true;
    }
} 