package flyair.booking.controller;

import flyair.booking.dto.response.ApiResponse;
import flyair.booking.dto.response.DashboardStatsResponse;
import flyair.booking.service.BookingService;
import flyair.booking.service.FlightService;
import flyair.booking.service.UserService;
import flyair.booking.repository.TicketRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final UserService userService;
    private final FlightService flightService;
    private final BookingService bookingService;
    private final TicketRepository ticketRepository;

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<DashboardStatsResponse>> getDashboardStats() {
        UserService.UserStatsResponse userStats = userService.getUserStats();
        FlightService.FlightStatsResponse flightStats = flightService.getFlightStats();
        BookingService.BookingStatsResponse bookingStats = bookingService.getBookingStats();
        
        long totalTickets = ticketRepository.count();
        
        DashboardStatsResponse dashboardStats = DashboardStatsResponse.builder()
                .totalUsers(userStats.getTotalUsers())
                .totalFlights(flightStats.getTotalFlights())
                .totalBookings(bookingStats.getTotalBookings())
                .totalTickets(totalTickets)
                .newUsersThisMonth(userStats.getNewUsersLast30Days())
                .flightsThisMonth(flightStats.getFlightsLast30Days())
                .bookingsThisMonth(bookingStats.getBookingsLast30Days())
                .revenueThisMonth(bookingStats.getRevenueThisMonth())
                .totalRevenue(bookingStats.getTotalRevenue())
                .activeFlights(flightStats.getScheduledFlights())
                .cancelledBookings(bookingStats.getCancelledBookings())
                .pendingBookings(bookingStats.getPendingBookings())
                .build();
        
        return ResponseEntity.ok(ApiResponse.success(dashboardStats));
    }
}