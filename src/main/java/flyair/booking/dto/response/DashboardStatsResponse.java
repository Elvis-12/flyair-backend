package flyair.booking.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {
    
    private long totalUsers;
    private long totalFlights;
    private long totalBookings;
    private long totalTickets;
    private long newUsersThisMonth;
    private long flightsThisMonth;
    private long bookingsThisMonth;
    private BigDecimal revenueThisMonth;
    private BigDecimal totalRevenue;
    private long activeFlights;
    private long cancelledBookings;
    private long pendingBookings;
}