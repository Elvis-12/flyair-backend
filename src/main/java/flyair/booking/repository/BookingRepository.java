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
public interface BookingRepository extends JpaRepository<Booking, Long> {
    
    Optional<Booking> findByBookingReference(String bookingReference);
    
    Page<Booking> findByUser(User user, Pageable pageable);
    
    Page<Booking> findByUserId(Long userId, Pageable pageable);
    
    @Query("SELECT b FROM Booking b WHERE " +
           "LOWER(b.bookingReference) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.user.firstName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.user.lastName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.user.email) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(b.flight.flightNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Booking> searchBookings(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT b FROM Booking b WHERE b.bookingStatus = :status")
    Page<Booking> findByBookingStatus(@Param("status") Booking.BookingStatus status, Pageable pageable);
    
    @Query("SELECT b FROM Booking b WHERE b.paymentStatus = :status")
    Page<Booking> findByPaymentStatus(@Param("status") Booking.PaymentStatus status, Pageable pageable);
    
    @Query("SELECT b FROM Booking b WHERE " +
           "b.bookingDate >= :startDate AND b.bookingDate <= :endDate")
    Page<Booking> findBookingsBetween(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate,
                                    Pageable pageable);
    
    @Query("SELECT b FROM Booking b WHERE b.flight.id = :flightId")
    Page<Booking> findByFlightId(@Param("flightId") Long flightId, Pageable pageable);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.bookingStatus = :status")
    Long countByBookingStatus(@Param("status") Booking.BookingStatus status);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE b.paymentStatus = :status")
    Long countByPaymentStatus(@Param("status") Booking.PaymentStatus status);
    
    @Query("SELECT SUM(b.totalAmount) FROM Booking b WHERE " +
           "b.paymentStatus = 'PAID' AND " +
           "b.bookingDate >= :startDate AND b.bookingDate <= :endDate")
    BigDecimal getTotalRevenueBetween(@Param("startDate") LocalDateTime startDate,
                                    @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT COUNT(b) FROM Booking b WHERE " +
           "b.bookingDate >= :startDate AND b.bookingDate <= :endDate")
    Long countBookingsBetween(@Param("startDate") LocalDateTime startDate,
                            @Param("endDate") LocalDateTime endDate);
}