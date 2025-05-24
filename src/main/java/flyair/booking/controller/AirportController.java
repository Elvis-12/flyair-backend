package flyair.booking.controller;

import flyair.booking.dto.request.CreateAirportRequest;
import flyair.booking.dto.response.ApiResponse;
import flyair.booking.dto.response.AirportResponse;
import flyair.booking.dto.response.PageResponse;
import flyair.booking.service.AirportService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/airports")
@RequiredArgsConstructor
public class AirportController {

    private final AirportService airportService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AirportResponse>> createAirport(
            @RequestBody CreateAirportRequest request) {
        AirportResponse airport = airportService.createAirport(request);
        return ResponseEntity.ok(ApiResponse.success("Airport created successfully", airport));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<AirportResponse>>> getAllAirports(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "airportCode") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<AirportResponse> airports = airportService.getAllAirports(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(airports)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<AirportResponse>>> searchAirports(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AirportResponse> airports = airportService.searchAirports(searchTerm, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(airports)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AirportResponse>> getAirportById(@PathVariable Long id) {
        AirportResponse airport = airportService.getAirportById(id);
        return ResponseEntity.ok(ApiResponse.success(airport));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<AirportResponse>> updateAirport(
            @PathVariable Long id,
            @RequestBody CreateAirportRequest request) {
        AirportResponse airport = airportService.updateAirport(id, request);
        return ResponseEntity.ok(ApiResponse.success("Airport updated successfully", airport));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteAirport(@PathVariable Long id) {
        airportService.deleteAirport(id);
        return ResponseEntity.ok(ApiResponse.success("Airport deleted successfully"));
    }

    @GetMapping("/country/{country}")
    public ResponseEntity<ApiResponse<PageResponse<AirportResponse>>> getAirportsByCountry(
            @PathVariable String country,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<AirportResponse> airports = airportService.getAirportsByCountry(country, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(airports)));
    }

    @GetMapping("/countries")
    public ResponseEntity<ApiResponse<List<String>>> getAllCountries() {
        List<String> countries = airportService.getAllCountries();
        return ResponseEntity.ok(ApiResponse.success(countries));
    }
}