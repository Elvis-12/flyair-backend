package flyair.booking.dto.request;

import flyair.booking.model.Seat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateSeatRequest {
    
    private String seatNumber;
    private Seat.SeatClass seatClass;
}