package flyair.booking.controller;

import flyair.booking.dto.request.CreateFlightRequest;
import flyair.booking.dto.request.FlightSearchRequest;
import flyair.booking.dto.request.UpdateFlightRequest;
import flyair.booking.dto.response.ApiResponse;
import flyair.booking.dto.response.FlightResponse;
import flyair.booking.dto.response.PageResponse;
import flyair.booking.model.Flight;
import flyair.booking.service.FlightService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/flights")
@RequiredArgsConstructor
public class FlightController {

    private final FlightService flightService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FlightResponse>> createFlight(
            @RequestBody CreateFlightRequest request) {
        FlightResponse flight = flightService.createFlight(request);
        return ResponseEntity.ok(ApiResponse.success("Flight created successfully", flight));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<FlightResponse>>> getAllFlights(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "departureTime") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<FlightResponse> flights = flightService.getAllFlights(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(flights)));
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<FlightResponse>>> searchFlights(
            @RequestBody FlightSearchRequest request,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<FlightResponse> flights = flightService.searchFlights(request, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(flights)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FlightResponse>> getFlightById(@PathVariable Long id) {
        FlightResponse flight = flightService.getFlightById(id);
        return ResponseEntity.ok(ApiResponse.success(flight));
    }

    @GetMapping("/number/{flightNumber}")
    public ResponseEntity<ApiResponse<FlightResponse>> getFlightByNumber(@PathVariable String flightNumber) {
        FlightResponse flight = flightService.getFlightByNumber(flightNumber);
        return ResponseEntity.ok(ApiResponse.success(flight));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FlightResponse>> updateFlight(
            @PathVariable Long id,
            @RequestBody UpdateFlightRequest request) {
        FlightResponse flight = flightService.updateFlight(id, request);
        return ResponseEntity.ok(ApiResponse.success("Flight updated successfully", flight));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FlightResponse>> updateFlightStatus(
            @PathVariable Long id,
            @RequestParam Flight.FlightStatus status) {
        FlightResponse flight = flightService.updateFlightStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success("Flight status updated successfully", flight));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteFlight(@PathVariable Long id) {
        flightService.deleteFlight(id);
        return ResponseEntity.ok(ApiResponse.success("Flight deleted successfully"));
    }

    @GetMapping("/status/{status}")
    public ResponseEntity<ApiResponse<PageResponse<FlightResponse>>> getFlightsByStatus(
            @PathVariable Flight.FlightStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<FlightResponse> flights = flightService.getFlightsByStatus(status, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(flights)));
    }

    @GetMapping("/date-range")
    public ResponseEntity<ApiResponse<PageResponse<FlightResponse>>> getFlightsByDateRange(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startTime,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endTime,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<FlightResponse> flights = flightService.getFlightsByDateRange(startTime, endTime, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(flights)));
    }

    @GetMapping("/airport/{airportId}")
    public ResponseEntity<ApiResponse<PageResponse<FlightResponse>>> getFlightsByAirport(
            @PathVariable Long airportId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<FlightResponse> flights = flightService.getFlightsByAirport(airportId, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(flights)));
    }

    @GetMapping("/upcoming")
    public ResponseEntity<ApiResponse<List<FlightResponse>>> getUpcomingFlights() {
        List<FlightResponse> flights = flightService.getUpcomingFlights();
        return ResponseEntity.ok(ApiResponse.success(flights));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<FlightService.FlightStatsResponse>> getFlightStats() {
        FlightService.FlightStatsResponse stats = flightService.getFlightStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}