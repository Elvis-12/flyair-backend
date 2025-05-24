package flyair.booking.controller;

import flyair.booking.dto.request.CreateSeatRequest;
import flyair.booking.dto.response.ApiResponse;
import flyair.booking.dto.response.PageResponse;
import flyair.booking.dto.response.SeatResponse;
import flyair.booking.model.Seat;
import flyair.booking.service.SeatService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/seats")
@RequiredArgsConstructor
public class SeatController {

    private final SeatService seatService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SeatResponse>> createSeat(
            @RequestBody CreateSeatRequest request) {
        SeatResponse seat = seatService.createSeat(request);
        return ResponseEntity.ok(ApiResponse.success("Seat created successfully", seat));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<SeatResponse>>> getAllSeats(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "seatNumber") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<SeatResponse> seats = seatService.getAllSeats(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(seats)));
    }

    @GetMapping("/search")
    public ResponseEntity<ApiResponse<PageResponse<SeatResponse>>> searchSeats(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<SeatResponse> seats = seatService.searchSeats(searchTerm, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(seats)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SeatResponse>> getSeatById(@PathVariable Long id) {
        SeatResponse seat = seatService.getSeatById(id);
        return ResponseEntity.ok(ApiResponse.success(seat));
    }

    @GetMapping("/class/{seatClass}")
    public ResponseEntity<ApiResponse<PageResponse<SeatResponse>>> getSeatsByClass(
            @PathVariable Seat.SeatClass seatClass,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<SeatResponse> seats = seatService.getSeatsByClass(seatClass, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(seats)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SeatResponse>> updateSeat(
            @PathVariable Long id,
            @RequestBody CreateSeatRequest request) {
        SeatResponse seat = seatService.updateSeat(id, request);
        return ResponseEntity.ok(ApiResponse.success("Seat updated successfully", seat));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<String>> deleteSeat(@PathVariable Long id) {
        seatService.deleteSeat(id);
        return ResponseEntity.ok(ApiResponse.success("Seat deleted successfully"));
    }
}