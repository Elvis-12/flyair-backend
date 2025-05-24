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
public interface FlightSeatRepository extends JpaRepository<FlightSeat, Long> {
    
    @Query("SELECT fs FROM FlightSeat fs WHERE fs.flight.id = :flightId")
    List<FlightSeat> findByFlightId(@Param("flightId") Long flightId);
    
    @Query("SELECT fs FROM FlightSeat fs WHERE fs.seat.id = :seatId")
    List<FlightSeat> findBySeatId(@Param("seatId") Long seatId);
    
    @Query("SELECT fs FROM FlightSeat fs WHERE " +
           "fs.flight.id = :flightId AND fs.seat.seatClass = :seatClass")
    List<FlightSeat> findByFlightIdAndSeatClass(@Param("flightId") Long flightId, 
                                               @Param("seatClass") Seat.SeatClass seatClass);
    
    @Query("SELECT fs FROM FlightSeat fs WHERE " +
           "fs.flight.id = :flightId AND fs.isAvailable = true")
    List<FlightSeat> findAvailableByFlightId(@Param("flightId") Long flightId);
    
    @Query("SELECT fs FROM FlightSeat fs WHERE " +
           "fs.flight.id = :flightId AND fs.seat.seatClass = :seatClass AND fs.isAvailable = true")
    List<FlightSeat> findAvailableByFlightIdAndSeatClass(@Param("flightId") Long flightId,
                                                        @Param("seatClass") Seat.SeatClass seatClass);
    
    @Query("SELECT COUNT(fs) FROM FlightSeat fs WHERE " +
           "fs.flight.id = :flightId AND fs.isAvailable = true")
    Long countAvailableByFlightId(@Param("flightId") Long flightId);
    
    @Query("SELECT COUNT(fs) FROM FlightSeat fs WHERE " +
           "fs.flight.id = :flightId AND fs.seat.seatClass = :seatClass AND fs.isAvailable = true")
    Long countAvailableByFlightIdAndSeatClass(@Param("flightId") Long flightId,
                                            @Param("seatClass") Seat.SeatClass seatClass);
    
    @Query("SELECT fs FROM FlightSeat fs WHERE " +
           "LOWER(fs.flight.flightNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(fs.seat.seatNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<FlightSeat> searchFlightSeats(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    Optional<FlightSeat> findByFlightIdAndSeatId(Long flightId, Long seatId);
}
