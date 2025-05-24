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
public interface AirportRepository extends JpaRepository<Airport, Long> {
    
    Optional<Airport> findByAirportCode(String airportCode);
    
    @Query("SELECT a FROM Airport a WHERE " +
           "LOWER(a.airportCode) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.airportName) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.city) LIKE LOWER(CONCAT('%', :searchTerm, '%')) OR " +
           "LOWER(a.country) LIKE LOWER(CONCAT('%', :searchTerm, '%'))")
    Page<Airport> searchAirports(@Param("searchTerm") String searchTerm, Pageable pageable);
    
    @Query("SELECT a FROM Airport a WHERE LOWER(a.country) = LOWER(:country)")
    Page<Airport> findByCountry(@Param("country") String country, Pageable pageable);
    
    @Query("SELECT a FROM Airport a WHERE LOWER(a.city) = LOWER(:city)")
    Page<Airport> findByCity(@Param("city") String city, Pageable pageable);
    
    @Query("SELECT a FROM Airport a WHERE a.isActive = :isActive")
    Page<Airport> findByIsActive(@Param("isActive") Boolean isActive, Pageable pageable);
    
    @Query("SELECT DISTINCT a.country FROM Airport a ORDER BY a.country")
    List<String> findAllCountries();
    
    @Query("SELECT DISTINCT a.city FROM Airport a WHERE LOWER(a.country) = LOWER(:country) ORDER BY a.city")
    List<String> findCitiesByCountry(@Param("country") String country);
    
    @Query("SELECT COUNT(a) FROM Airport a WHERE a.isActive = true")
    Long countActiveAirports();
    
    boolean existsByAirportCode(String airportCode);
}

