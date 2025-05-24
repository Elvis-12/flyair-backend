package flyair.booking.service;

import flyair.booking.dto.request.CreateTicketRequest;
import flyair.booking.dto.response.TicketResponse;
import flyair.booking.exception.BadRequestException;
import flyair.booking.exception.ResourceNotFoundException;
import flyair.booking.model.*;
import flyair.booking.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class TicketService {
    
    private final TicketRepository ticketRepository;
    private final BookingRepository bookingRepository;
    private final FlightSeatRepository flightSeatRepository;
    private final FlightSeatService flightSeatService;
    private final ModelMapper modelMapper;
    
    /**
     * Create a new ticket
     */
    public TicketResponse createTicket(CreateTicketRequest request) {
        // Get booking
        Booking booking = bookingRepository.findById(request.getBookingId())
                .orElseThrow(() -> new ResourceNotFoundException("Booking not found"));
        
        // Get flight seat
        FlightSeat flightSeat = flightSeatRepository.findById(request.getFlightSeatId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight seat not found"));
        
        // Validate seat is available
        if (!flightSeat.getIsAvailable() || flightSeat.getIsOccupied()) {
            throw new BadRequestException("Selected seat is not available");
        }
        
        // Generate ticket number
        String ticketNumber = generateTicketNumber();
        
        Ticket ticket = Ticket.builder()
                .ticketNumber(ticketNumber)
                .booking(booking)
                .flightSeat(flightSeat)
                .passengerName(request.getPassengerName())
                .passengerEmail(request.getPassengerEmail())
                .passengerPhone(request.getPassengerPhone())
                .passportNumber(request.getPassportNumber())
                .ticketStatus(Ticket.TicketStatus.ISSUED)
                .build();
        
        Ticket savedTicket = ticketRepository.save(ticket);
        
        // Mark seat as booked
        flightSeatService.bookSeat(flightSeat.getId());
        
        log.info("Ticket created successfully: {}", savedTicket.getTicketNumber());
        
        return modelMapper.map(savedTicket, TicketResponse.class);
    }
    
    /**
     * Get all tickets with pagination
     */
    @Transactional(readOnly = true)
    public Page<TicketResponse> getAllTickets(Pageable pageable) {
        return ticketRepository.findAll(pageable)
                .map(ticket -> modelMapper.map(ticket, TicketResponse.class));
    }
    
    /**
     * Search tickets
     */
    @Transactional(readOnly = true)
    public Page<TicketResponse> searchTickets(String searchTerm, Pageable pageable) {
        return ticketRepository.searchTickets(searchTerm, pageable)
                .map(ticket -> modelMapper.map(ticket, TicketResponse.class));
    }
    
    /**
     * Get ticket by ID
     */
    @Transactional(readOnly = true)
    public TicketResponse getTicketById(Long id) {
        Ticket ticket = ticketRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with id: " + id));
        return modelMapper.map(ticket, TicketResponse.class);
    }
    
    /**
     * Get ticket by ticket number
     */
    @Transactional(readOnly = true)
    public TicketResponse getTicketByNumber(String ticketNumber) {
        Ticket ticket = ticketRepository.findByTicketNumber(ticketNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found with number: " + ticketNumber));
        return modelMapper.map(ticket, TicketResponse.class);
    }
    
    /**
     * Check in passenger
     */
    public TicketResponse checkInPassenger(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        
        if (ticket.getTicketStatus() != Ticket.TicketStatus.ISSUED) {
            throw new BadRequestException("Ticket is not in issued status");
        }
        
        ticket.setTicketStatus(Ticket.TicketStatus.CHECKED_IN);
        ticket.setCheckInTime(LocalDateTime.now());
        
        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Passenger checked in successfully: {}", savedTicket.getTicketNumber());
        
        return modelMapper.map(savedTicket, TicketResponse.class);
    }
    
    /**
     * Board passenger
     */
    public TicketResponse boardPassenger(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        
        if (ticket.getTicketStatus() != Ticket.TicketStatus.CHECKED_IN) {
            throw new BadRequestException("Passenger must be checked in before boarding");
        }
        
        ticket.setTicketStatus(Ticket.TicketStatus.BOARDED);
        ticket.setBoardingTime(LocalDateTime.now());
        
        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Passenger boarded successfully: {}", savedTicket.getTicketNumber());
        
        return modelMapper.map(savedTicket, TicketResponse.class);
    }
    
    /**
     * Cancel ticket
     */
    public TicketResponse cancelTicket(Long ticketId) {
        Ticket ticket = ticketRepository.findById(ticketId)
                .orElseThrow(() -> new ResourceNotFoundException("Ticket not found"));
        
        if (ticket.getTicketStatus() == Ticket.TicketStatus.BOARDED) {
            throw new BadRequestException("Cannot cancel boarded ticket");
        }
        
        ticket.setTicketStatus(Ticket.TicketStatus.CANCELLED);
        
        // Free up the seat
        FlightSeat flightSeat = ticket.getFlightSeat();
        flightSeat.setIsOccupied(false);
        flightSeat.setIsAvailable(true);
        flightSeatRepository.save(flightSeat);
        
        Ticket savedTicket = ticketRepository.save(ticket);
        log.info("Ticket cancelled successfully: {}", savedTicket.getTicketNumber());
        
        return modelMapper.map(savedTicket, TicketResponse.class);
    }
    
    /**
     * Get tickets by booking
     */
    @Transactional(readOnly = true)
    public List<TicketResponse> getTicketsByBooking(Long bookingId) {
        List<Ticket> tickets = ticketRepository.findByBookingId(bookingId);
        return tickets.stream()
                .map(ticket -> modelMapper.map(ticket, TicketResponse.class))
                .toList();
    }
    
    /**
     * Get tickets by username
     */
    @Transactional(readOnly = true)
    public List<TicketResponse> getUserTickets(String username) {
        List<Ticket> tickets = ticketRepository.findByBooking_User_Username(username); // Assuming this repository method exists
        return tickets.stream()
                .map(ticket -> modelMapper.map(ticket, TicketResponse.class))
                .toList();
    }
    
    private String generateTicketNumber() {
        return "TKT" + UUID.randomUUID().toString().substring(0, 10).toUpperCase();
    }
}