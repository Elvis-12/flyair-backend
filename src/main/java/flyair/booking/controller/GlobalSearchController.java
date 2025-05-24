package flyair.booking.controller;

import flyair.booking.dto.response.ApiResponse;
import flyair.booking.dto.response.PageResponse;
import flyair.booking.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/search")
@RequiredArgsConstructor
public class GlobalSearchController {

    private final UserService userService;
    private final FlightService flightService;
    private final BookingService bookingService;
    private final TicketService ticketService;
    private final AirportService airportService;
    private final SeatService seatService;

    @GetMapping("/global")
    public ResponseEntity<ApiResponse<Map<String, Object>>> globalSearch(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Map<String, Object> results = new HashMap<>();
        
        try {
            // Search flights
            results.put("flights", PageResponse.of(
                flightService.searchFlights(
                    flyair.booking.dto.request.FlightSearchRequest.builder()
                        .searchTerm(query)
                        .build(), 
                    pageable
                )
            ));
            
            // Search airports
            results.put("airports", PageResponse.of(airportService.searchAirports(query, pageable)));
            
            // Search bookings (admin only)
            try {
                results.put("bookings", PageResponse.of(bookingService.searchBookings(query, pageable)));
            } catch (Exception e) {
                // User doesn't have admin access
            }
            
            // Search tickets (admin only)
            try {
                results.put("tickets", PageResponse.of(ticketService.searchTickets(query, pageable)));
            } catch (Exception e) {
                // User doesn't have admin access
            }
            
            // Search users (admin only)
            try {
                results.put("users", PageResponse.of(userService.searchUsers(query, pageable)));
            } catch (Exception e) {
                // User doesn't have admin access
            }
            
            // Search seats
            results.put("seats", PageResponse.of(seatService.searchSeats(query, pageable)));
            
        } catch (Exception e) {
            // Handle any search errors gracefully
        }
        
        return ResponseEntity.ok(ApiResponse.success("Global search completed", results));
    }
}