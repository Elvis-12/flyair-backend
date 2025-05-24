package flyair.booking.service;

import flyair.booking.dto.request.CreateAirportRequest;
import flyair.booking.dto.response.AirportResponse;
import flyair.booking.exception.BadRequestException;
import flyair.booking.exception.ResourceNotFoundException;
import flyair.booking.model.Airport;
import flyair.booking.repository.AirportRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class AirportService {
    
    private final AirportRepository airportRepository;
    private final ModelMapper modelMapper;
    
    /**
     * Create a new airport
     */
    public AirportResponse createAirport(CreateAirportRequest request) {
        // Check if airport code already exists
        if (airportRepository.existsByAirportCode(request.getAirportCode())) {
            throw new BadRequestException("Airport code already exists: " + request.getAirportCode());
        }
        
        Airport airport = Airport.builder()
                .airportCode(request.getAirportCode().toUpperCase())
                .airportName(request.getAirportName())
                .city(request.getCity())
                .country(request.getCountry())
                .countryCode(request.getCountryCode())
                .timeZone(request.getTimeZone())
                .latitude(request.getLatitude())
                .longitude(request.getLongitude())
                .isActive(true)
                .build();
        
        Airport savedAirport = airportRepository.save(airport);
        log.info("Airport created successfully: {}", savedAirport.getAirportCode());
        
        return modelMapper.map(savedAirport, AirportResponse.class);
    }
    
    /**
     * Get all airports with pagination
     */
    @Transactional(readOnly = true)
    public Page<AirportResponse> getAllAirports(Pageable pageable) {
        return airportRepository.findAll(pageable)
                .map(airport -> modelMapper.map(airport, AirportResponse.class));
    }
    
    /**
     * Search airports
     */
    @Transactional(readOnly = true)
    public Page<AirportResponse> searchAirports(String searchTerm, Pageable pageable) {
        return airportRepository.searchAirports(searchTerm, pageable)
                .map(airport -> modelMapper.map(airport, AirportResponse.class));
    }
    
    /**
     * Get airport by ID
     */
    @Transactional(readOnly = true)
    public AirportResponse getAirportById(Long id) {
        Airport airport = airportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Airport not found with id: " + id));
        return modelMapper.map(airport, AirportResponse.class);
    }
    
    /**
     * Update airport
     */
    public AirportResponse updateAirport(Long id, CreateAirportRequest request) {
        Airport airport = airportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Airport not found with id: " + id));
        
        // Check if airport code is being changed and if it already exists
        if (!airport.getAirportCode().equals(request.getAirportCode().toUpperCase()) &&
            airportRepository.existsByAirportCode(request.getAirportCode())) {
            throw new BadRequestException("Airport code already exists: " + request.getAirportCode());
        }
        
        airport.setAirportCode(request.getAirportCode().toUpperCase());
        airport.setAirportName(request.getAirportName());
        airport.setCity(request.getCity());
        airport.setCountry(request.getCountry());
        airport.setCountryCode(request.getCountryCode());
        airport.setTimeZone(request.getTimeZone());
        airport.setLatitude(request.getLatitude());
        airport.setLongitude(request.getLongitude());
        
        Airport savedAirport = airportRepository.save(airport);
        log.info("Airport updated successfully: {}", savedAirport.getAirportCode());
        
        return modelMapper.map(savedAirport, AirportResponse.class);
    }
    
    /**
     * Delete airport
     */
    public void deleteAirport(Long id) {
        Airport airport = airportRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Airport not found with id: " + id));
        
        // Check if airport has flights
        if (!airport.getDepartureFlights().isEmpty() || !airport.getArrivalFlights().isEmpty()) {
            throw new BadRequestException("Cannot delete airport with existing flights");
        }
        
        airportRepository.delete(airport);
        log.info("Airport deleted successfully: {}", airport.getAirportCode());
    }
    
    /**
     * Get airports by country
     */
    @Transactional(readOnly = true)
    public Page<AirportResponse> getAirportsByCountry(String country, Pageable pageable) {
        return airportRepository.findByCountry(country, pageable)
                .map(airport -> modelMapper.map(airport, AirportResponse.class));
    }
    
    /**
     * Get all countries
     */
    @Transactional(readOnly = true)
    public List<String> getAllCountries() {
        return airportRepository.findAllCountries();
    }
}