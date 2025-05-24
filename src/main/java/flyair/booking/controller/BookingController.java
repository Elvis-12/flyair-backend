package flyair.booking.controller;

import flyair.booking.dto.request.CreateBookingRequest;
import flyair.booking.dto.response.ApiResponse;
import flyair.booking.dto.response.BookingResponse;
import flyair.booking.dto.response.PageResponse;
import flyair.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    public ResponseEntity<ApiResponse<BookingResponse>> createBooking(
            @RequestBody CreateBookingRequest request) {
        BookingResponse booking = bookingService.createBooking(request);
        return ResponseEntity.ok(ApiResponse.success("Booking created successfully", booking));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> getAllBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "bookingDate") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<BookingResponse> bookings = bookingService.getAllBookings(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(bookings)));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> searchBookings(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<BookingResponse> bookings = bookingService.searchBookings(searchTerm, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(bookings)));
    }

    @GetMapping("/my-bookings")
    public ResponseEntity<ApiResponse<PageResponse<BookingResponse>>> getUserBookings(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size, Sort.by("bookingDate").descending());
        Page<BookingResponse> bookings = bookingService.getUserBookings(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(bookings)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingById(@PathVariable Long id) {
        BookingResponse booking = bookingService.getBookingById(id);
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    @GetMapping("/reference/{bookingReference}")
    public ResponseEntity<ApiResponse<BookingResponse>> getBookingByReference(@PathVariable String bookingReference) {
        BookingResponse booking = bookingService.getBookingByReference(bookingReference);
        return ResponseEntity.ok(ApiResponse.success(booking));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<BookingResponse>> cancelBooking(@PathVariable Long id) {
        BookingResponse booking = bookingService.cancelBooking(id);
        return ResponseEntity.ok(ApiResponse.success("Booking cancelled successfully", booking));
    }

    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<BookingService.BookingStatsResponse>> getBookingStats() {
        BookingService.BookingStatsResponse stats = bookingService.getBookingStats();
        return ResponseEntity.ok(ApiResponse.success(stats));
    }
}