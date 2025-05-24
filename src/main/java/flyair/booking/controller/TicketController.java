package flyair.booking.controller;

import flyair.booking.dto.request.CreateTicketRequest;
import flyair.booking.dto.response.ApiResponse;
import flyair.booking.dto.response.PageResponse;
import flyair.booking.dto.response.TicketResponse;
import flyair.booking.service.TicketService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tickets")
@RequiredArgsConstructor
public class TicketController {

    private final TicketService ticketService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TicketResponse>> createTicket(
            @RequestBody CreateTicketRequest request) {
        TicketResponse ticket = ticketService.createTicket(request);
        return ResponseEntity.ok(ApiResponse.success("Ticket created successfully", ticket));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<TicketResponse>>> getAllTickets(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {
        
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
                Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<TicketResponse> tickets = ticketService.getAllTickets(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(tickets)));
    }

    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PageResponse<TicketResponse>>> searchTickets(
            @RequestParam String searchTerm,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Pageable pageable = PageRequest.of(page, size);
        Page<TicketResponse> tickets = ticketService.searchTickets(searchTerm, pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.of(tickets)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicketById(@PathVariable Long id) {
        TicketResponse ticket = ticketService.getTicketById(id);
        return ResponseEntity.ok(ApiResponse.success(ticket));
    }

    @GetMapping("/number/{ticketNumber}")
    public ResponseEntity<ApiResponse<TicketResponse>> getTicketByNumber(@PathVariable String ticketNumber) {
        TicketResponse ticket = ticketService.getTicketByNumber(ticketNumber);
        return ResponseEntity.ok(ApiResponse.success(ticket));
    }

    @GetMapping("/booking/{bookingId}")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getTicketsByBooking(@PathVariable Long bookingId) {
        List<TicketResponse> tickets = ticketService.getTicketsByBooking(bookingId);
        return ResponseEntity.ok(ApiResponse.success(tickets));
    }

    @PatchMapping("/{id}/check-in")
    public ResponseEntity<ApiResponse<TicketResponse>> checkInPassenger(@PathVariable Long id) {
        TicketResponse ticket = ticketService.checkInPassenger(id);
        return ResponseEntity.ok(ApiResponse.success("Passenger checked in successfully", ticket));
    }

    @PatchMapping("/{id}/board")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<TicketResponse>> boardPassenger(@PathVariable Long id) {
        TicketResponse ticket = ticketService.boardPassenger(id);
        return ResponseEntity.ok(ApiResponse.success("Passenger boarded successfully", ticket));
    }

    @PatchMapping("/{id}/cancel")
    public ResponseEntity<ApiResponse<TicketResponse>> cancelTicket(@PathVariable Long id) {
        TicketResponse ticket = ticketService.cancelTicket(id);
        return ResponseEntity.ok(ApiResponse.success("Ticket cancelled successfully", ticket));
    }

    @GetMapping("/my-tickets")
    public ResponseEntity<ApiResponse<List<TicketResponse>>> getUserTickets() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        String username = userDetails.getUsername();

        List<TicketResponse> userTickets = ticketService.getUserTickets(username);

        return ResponseEntity.ok(ApiResponse.success("User tickets retrieved successfully.", userTickets));
    }
}