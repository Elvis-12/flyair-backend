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
public interface SeatRepository extends JpaRepository<Seat, Long> {
    
    Optional<Seat> findBySeatNumber(String seatNumber);
    
    @Query("SELECT s FROM Seat s WHERE " +
           "LOWER(s.seatNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Seat> searchSeats(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT s FROM Seat s WHERE s.seatClass = :seatClass")
    Page<Seat> findBySeatClass(@Param("seatClass") Seat.SeatClass seatClass, Pageable pageable);
    
    @Query("SELECT COUNT(s) FROM Seat s WHERE s.seatClass = :seatClass")
    Long countBySeatClass(@Param("seatClass") Seat.SeatClass seatClass);
    
    @Query("SELECT DISTINCT s.seatClass FROM Seat s")
    List<Seat.SeatClass> findAllSeatClasses();
    
    boolean existsBySeatNumber(String seatNumber);
}