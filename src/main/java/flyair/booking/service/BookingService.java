package flyair.booking.service;

import flyair.booking.dto.request.CreateBookingRequest;
import flyair.booking.dto.request.CreateTicketRequest;
import flyair.booking.dto.response.BookingResponse;
import flyair.booking.exception.BadRequestException;
import flyair.booking.exception.ResourceNotFoundException;
import flyair.booking.model.*;
import flyair.booking.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class BookingService {
    
    private final BookingRepository bookingRepository;
    private final FlightRepository flightRepository;
    private final UserRepository userRepository;
    private final FlightSeatRepository flightSeatRepository;
    private final TicketService ticketService;
    private final EmailService emailService;
    private final ModelMapper modelMapper;
    
    /**
     * Create a new booking
     */
    public BookingResponse createBooking(CreateBookingRequest request) {
        // Get flight
        Flight flight = flightRepository.findById(request.getFlightId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));
        
        // Validate flight is available for booking
        if (flight.getStatus() != Flight.FlightStatus.SCHEDULED) {
            throw new BadRequestException("Flight is not available for booking");
        }
        
        // Validate departure time is in the future
        if (flight.getDepartureTime().isBefore(LocalDateTime.now().plusHours(2))) {
            throw new BadRequestException("Cannot book flight less than 2 hours before departure");
        }
        
        // Get current user
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        // Validate flight seats are available
        List<FlightSeat> flightSeats = validateAndGetFlightSeats(request.getFlightSeatIds());
        
        // Calculate total amount
        BigDecimal totalAmount = flightSeats.stream()
                .map(FlightSeat::getPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        
        // Generate booking reference
        String bookingReference = generateBookingReference();
        
        // Create booking
        Booking booking = Booking.builder()
                .bookingReference(bookingReference)
                .user(user)
                .flight(flight)
                .totalAmount(totalAmount)
                .bookingStatus(Booking.BookingStatus.PENDING)
                .paymentStatus(Booking.PaymentStatus.PENDING)
                .bookingDate(LocalDateTime.now())
                .build();
        
        Booking savedBooking = bookingRepository.save(booking);
        
        // Create tickets for each passenger and flight seat
        for (int i = 0; i < request.getPassengers().size() && i < flightSeats.size(); i++) {
            CreateBookingRequest.PassengerRequest passenger = request.getPassengers().get(i);
            FlightSeat flightSeat = flightSeats.get(i);
            
            CreateTicketRequest ticketRequest = CreateTicketRequest.builder()
                    .bookingId(savedBooking.getId())
                    .flightSeatId(flightSeat.getId())
                    .passengerName(passenger.getPassengerName())
                    .passengerEmail(passenger.getPassengerEmail())
                    .passengerPhone(passenger.getPassengerPhone())
                    .passportNumber(passenger.getPassportNumber())
                    .build();
            
            ticketService.createTicket(ticketRequest);
        }
        
        // Send confirmation email
        emailService.sendBookingConfirmationEmail(
                user.getEmail(),
                user.getFirstName(),
                bookingReference,
                flight.getFlightNumber(),
                flight.getDepartureTime().toString()
        );
        
        log.info("Booking created successfully: {}", bookingReference);
        
        return modelMapper.map(savedBooking, BookingResponse.class);
    }
    
    /**
     * Get all bookings with pagination
     */
    @Transactional(readOnly = true)
    public Page<BookingResponse> getAllBookings(Pageable pageable) {
        return bookingRepository.findAll(pageable)
                .map(booking -> modelMapper.map(booking, BookingResponse.class));
    }
    
    /**
     * Search bookings
     */
    @Transactional(readOnly = true)
    public Page<BookingResponse> searchBookings(String searchTerm, Pageable pageable) {
        return bookingRepository.searchBookings(searchTerm, pageable)
                .map(booking -> modelMapper.map(booking, BookingResponse.class));
    }
    
    /**
     * Get booking by ID
     */
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        return modelMapper.map(booking, BookingResponse.class);
    }
    
    /**
     * Get booking by reference
     */
    @Transactional(readOnly = true)
    public BookingResponse getBookingByReference(String bookingReference) {
        Booking booking = bookingRepository.findByBookingReference(bookingReference)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with reference: " + bookingReference));
        return modelMapper.map(booking, BookingResponse.class);
    }
    
    /**
     * Get user bookings
     */
    @Transactional(readOnly = true)
    public Page<BookingResponse> getUserBookings(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        return bookingRepository.findByUser(user, pageable)
                .map(booking -> modelMapper.map(booking, BookingResponse.class));
    }
    
    /**
     * Cancel booking
     */
    public BookingResponse cancelBooking(Long id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found with id: " + id));
        
        // Check if booking can be cancelled
        if (booking.getBookingStatus() == Booking.BookingStatus.CANCELLED) {
            throw new BadRequestException("Booking is already cancelled");
        }
        
        if (booking.getBookingStatus() == Booking.BookingStatus.COMPLETED) {
            throw new BadRequestException("Cannot cancel completed booking");
        }
        
        // Check if flight departure is not too close
        if (booking.getFlight().getDepartureTime().isBefore(LocalDateTime.now().plusHours(24))) {
            throw new BadRequestException("Cannot cancel booking less than 24 hours before departure");
        }
        
        // Cancel booking
        booking.setBookingStatus(Booking.BookingStatus.CANCELLED);
        
        // Cancel all tickets (this will free up seats)
        booking.getTickets().forEach(ticket -> ticketService.cancelTicket(ticket.getId()));
        
        // Update payment status if paid
        if (booking.getPaymentStatus() == Booking.PaymentStatus.PAID) {
            booking.setPaymentStatus(Booking.PaymentStatus.REFUNDED);
        }
        
        Booking savedBooking = bookingRepository.save(booking);
        log.info("Booking cancelled successfully: {}", savedBooking.getBookingReference());
        
        return modelMapper.map(savedBooking, BookingResponse.class);
    }
    
    /**
     * Get booking statistics
     */
    @Transactional(readOnly = true)
    public BookingStatsResponse getBookingStats() {
        long totalBookings = bookingRepository.count();
        long confirmedBookings = bookingRepository.countByBookingStatus(Booking.BookingStatus.CONFIRMED);
        long pendingBookings = bookingRepository.countByBookingStatus(Booking.BookingStatus.PENDING);
        long cancelledBookings = bookingRepository.countByBookingStatus(Booking.BookingStatus.CANCELLED);
        
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long bookingsLast30Days = bookingRepository.countBookingsBetween(thirtyDaysAgo, LocalDateTime.now());
        
        BigDecimal totalRevenue = bookingRepository.getTotalRevenueBetween(
                LocalDateTime.now().minusYears(1), LocalDateTime.now());
        
        BigDecimal revenueThisMonth = bookingRepository.getTotalRevenueBetween(
                LocalDateTime.now().withDayOfMonth(1), LocalDateTime.now());
        
        return BookingStatsResponse.builder()
                .totalBookings(totalBookings)
                .confirmedBookings(confirmedBookings)
                .pendingBookings(pendingBookings)
                .cancelledBookings(cancelledBookings)
                .bookingsLast30Days(bookingsLast30Days)
                .totalRevenue(totalRevenue != null ? totalRevenue : BigDecimal.ZERO)
                .revenueThisMonth(revenueThisMonth != null ? revenueThisMonth : BigDecimal.ZERO)
                .build();
    }
    
    // Helper methods
    private List<FlightSeat> validateAndGetFlightSeats(List<Long> flightSeatIds) {
        List<FlightSeat> flightSeats = new ArrayList<>();
        
        for (Long flightSeatId : flightSeatIds) {
            FlightSeat flightSeat = flightSeatRepository.findById(flightSeatId)
                    .orElseThrow(() -> new ResourceNotFoundException("Flight seat not found with id: " + flightSeatId));
            
            if (!flightSeat.getIsAvailable() || flightSeat.getIsOccupied()) {
                throw new BadRequestException("Flight seat is not available: " + flightSeat.getSeat().getSeatNumber());
            }
            
            flightSeats.add(flightSeat);
        }
        
        return flightSeats;
    }
    
    private String generateBookingReference() {
        return "FLY" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
    
    @lombok.Data
    @lombok.Builder
    public static class BookingStatsResponse {
        private long totalBookings;
        private long confirmedBookings;
        private long pendingBookings;
        private long cancelledBookings;
        private long bookingsLast30Days;
        private BigDecimal totalRevenue;
        private BigDecimal revenueThisMonth;
    }
}