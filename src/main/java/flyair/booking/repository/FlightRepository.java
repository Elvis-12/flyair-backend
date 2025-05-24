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
public interface FlightRepository extends JpaRepository<Flight, Long> {
    
    Optional<Flight> findByFlightNumber(String flightNumber);
    
    @Query("SELECT f FROM Flight f WHERE " +
           "f.departureAirport = :departureAirport AND " +
           "f.arrivalAirport = :arrivalAirport AND " +
           "DATE(f.departureTime) = DATE(:departureDate)")
    Page<Flight> findFlights(@Param("departureAirport") Airport departureAirport,
                           @Param("arrivalAirport") Airport arrivalAirport,
                           @Param("departureDate") LocalDateTime departureDate,
                           Pageable pageable);
    
    @Query("SELECT f FROM Flight f WHERE " +
           "LOWER(f.flightNumber) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(f.departureAirport.airportCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(f.arrivalAirport.airportCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(f.departureAirport.city) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(f.arrivalAirport.city) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(f.aircraftType) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Flight> searchFlights(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT f FROM Flight f WHERE f.departureTime >= :startTime AND f.departureTime <= :endTime")
    Page<Flight> findFlightsByDepartureTimeBetween(@Param("startTime") LocalDateTime startTime,
                                                 @Param("endTime") LocalDateTime endTime,
                                                 Pageable pageable);
    
    @Query("SELECT f FROM Flight f WHERE f.status = :status")
    Page<Flight> findByStatus(@Param("status") Flight.FlightStatus status, Pageable pageable);
    
    @Query("SELECT f FROM Flight f WHERE f.departureAirport.id = :airportId OR f.arrivalAirport.id = :airportId")
    Page<Flight> findByAirportId(@Param("airportId") Long airportId, Pageable pageable);
    
    @Query("SELECT COUNT(f) FROM Flight f WHERE f.status = :status")
    Long countByStatus(@Param("status") Flight.FlightStatus status);
    
    @Query("SELECT COUNT(f) FROM Flight f WHERE " +
           "f.departureTime >= :startDate AND f.departureTime <= :endDate")
    Long countFlightsBetween(@Param("startDate") LocalDateTime startDate,
                           @Param("endDate") LocalDateTime endDate);
    
    @Query("SELECT f FROM Flight f WHERE " +
           "f.departureTime > :now AND " +
           "f.status IN ('SCHEDULED', 'BOARDING')")
    List<Flight> findUpcomingFlights(@Param("now") LocalDateTime now);
}
