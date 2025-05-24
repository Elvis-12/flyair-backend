package flyair.booking.controller;

import flyair.booking.dto.request.CreateFlightSeatRequest;
import flyair.booking.dto.request.UpdateFlightSeatRequest;
import flyair.booking.dto.request.CreateBulkFlightSeatsRequest;
import flyair.booking.dto.response.ApiResponse;
import flyair.booking.dto.response.FlightSeatResponse;
import flyair.booking.dto.response.PageResponse;
import flyair.booking.model.Seat;
import flyair.booking.service.FlightSeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flight-seats")
@RequiredArgsConstructor
public class FlightSeatController {

    private final FlightSeatService flightSeatService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FlightSeatResponse>> createFlightSeat(
            @RequestBody CreateFlightSeatRequest request) {
        FlightSeatResponse flightSeat = flightSeatService.createFlightSeat(request);
        return ResponseEntity.ok(ApiResponse.success("Flight seat created successfully", flightSeat));
    }

    @GetMapping("/flight/{flightId}/available")
    public ResponseEntity<ApiResponse<List<FlightSeatResponse>>> getAvailableSeatsForFlight(
            @PathVariable Long flightId) {
        List<FlightSeatResponse> seats = flightSeatService.getAvailableSeatsForFlight(flightId);
        return ResponseEntity.ok(ApiResponse.success(seats));
    }

    @GetMapping("/flight/{flightId}/class/{seatClass}/available")
    public ResponseEntity<ApiResponse<List<FlightSeatResponse>>> getAvailableSeatsForFlightByClass(
            @PathVariable Long flightId,
            @PathVariable Seat.SeatClass seatClass) {
        List<FlightSeatResponse> seats = flightSeatService.getAvailableSeatsForFlightByClass(flightId, seatClass);
        return ResponseEntity.ok(ApiResponse.success(seats));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<FlightSeatResponse>>> searchFlightSeats(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<FlightSeatResponse> flightSeats = flightSeatService.searchFlightSeats(searchTerm, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(flightSeats)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FlightSeatResponse>> updateFlightSeat(
            @PathVariable Long id,
            @RequestBody UpdateFlightSeatRequest request) {
        FlightSeatResponse flightSeat = flightSeatService.updateFlightSeat(id, request);
        return ResponseEntity.ok(ApiResponse.success("Flight seat updated successfully", flightSeat));
    }

    @PostMapping("/{id}/book")
    public ResponseEntity<ApiResponse<FlightSeatResponse>> bookSeat(@PathVariable Long id) {
        FlightSeatResponse flightSeat = flightSeatService.bookSeat(id);
        return ResponseEntity.ok(ApiResponse.success("Seat booked successfully", flightSeat));
    }

    @PostMapping("/bulk")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<FlightSeatResponse>>> createBulkFlightSeats(
            @RequestBody CreateBulkFlightSeatsRequest request) {
        List<FlightSeatResponse> flightSeats = flightSeatService.createBulkFlightSeats(request);
        return ResponseEntity.ok(ApiResponse.success("Flight seats created successfully", flightSeats));
    }
}