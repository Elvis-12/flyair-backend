package flyair.booking.repository;

import flyair.booking.model.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface TicketRepository extends JpaRepository<Ticket, Long> {
    
    Optional<Ticket> findByTicketNumber(String ticketNumber);
    
    List<Ticket> findByBooking(Booking booking);
    
    List<Ticket> findByBookingId(Long bookingId);
    
    List<Ticket> findByBooking_User_Username(String username);
    
    @Query("SELECT t FROM Ticket t WHERE " +
           "LOWER(t.ticketNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.passengerName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.passengerEmail) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(t.flightSeat.seat.seatNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Ticket> searchTickets(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT t FROM Ticket t WHERE t.flightSeat.seat.seatClass = :seatClass")
    Page<Ticket> findBySeatClass(@Param("seatClass") Seat.SeatClass seatClass, Pageable pageable);
    
    @Query("SELECT t FROM Ticket t WHERE t.ticketStatus = :status")
    Page<Ticket> findByTicketStatus(@Param("status") Ticket.TicketStatus status, Pageable pageable);
    
    @Query("SELECT t FROM Ticket t WHERE t.booking.flight.id = :flightId")
    List<Ticket> findByFlightId(@Param("flightId") Long flightId);
    
    @Query("SELECT t.flightSeat.seat.seatNumber FROM Ticket t WHERE " +
           "t.booking.flight.id = :flightId AND " +
           "t.ticketStatus NOT IN ('CANCELLED')")
    List<String> findOccupiedSeatsByFlightId(@Param("flightId") Long flightId);
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE t.ticketStatus = :status")
    Long countByTicketStatus(@Param("status") Ticket.TicketStatus status);
    
    @Query("SELECT COUNT(t) FROM Ticket t WHERE " +
           "t.booking.flight.id = :flightId AND " +
           "t.flightSeat.seat.seatClass = :seatClass AND " +
           "t.ticketStatus NOT IN ('CANCELLED')")
    Long countTicketsByFlightAndSeatClass(@Param("flightId") Long flightId,
                                        @Param("seatClass") Seat.SeatClass seatClass);
    
    boolean existsByTicketNumber(String ticketNumber);
    
    @Query("SELECT CASE WHEN COUNT(t) > 0 THEN true ELSE false END FROM Ticket t WHERE " +
           "t.flightSeat.seat.seatNumber = :seatNumber AND " +
           "t.booking.flight.id = :flightId AND " +
           "t.ticketStatus NOT IN ('CANCELLED')")
    boolean existsBySeatNumberAndFlightId(@Param("seatNumber") String seatNumber, 
                                        @Param("flightId") Long flightId);
}
