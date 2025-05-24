package flyair.booking.dto.request;

import flyair.booking.model.Booking;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateBookingRequest {
    
    private Booking.BookingStatus bookingStatus;
    private Booking.PaymentStatus paymentStatus;
}