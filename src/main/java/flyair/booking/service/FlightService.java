package flyair.booking.service;

import flyair.booking.dto.request.CreateFlightRequest;
import flyair.booking.dto.request.FlightSearchRequest;
import flyair.booking.dto.request.UpdateFlightRequest;
import flyair.booking.dto.response.FlightResponse;
import flyair.booking.exception.BadRequestException;
import flyair.booking.exception.ResourceNotFoundException;
import flyair.booking.model.Airport;
import flyair.booking.model.Flight;
import flyair.booking.repository.AirportRepository;
import flyair.booking.repository.FlightRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FlightService {
    
    private final FlightRepository flightRepository;
    private final AirportRepository airportRepository;
    private final ModelMapper modelMapper;
    
    /**
     * Create a new flight
     */
    public FlightResponse createFlight(CreateFlightRequest request) {
        // Validate departure and arrival airports are different
        if (request.getDepartureAirportId().equals(request.getArrivalAirportId())) {
            throw new BadRequestException("Departure and arrival airports cannot be the same");
        }
        
        // Validate departure time is in the future
        if (request.getDepartureTime().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Departure time must be in the future");
        }
        
        // Validate arrival time is after departure time
        if (request.getArrivalTime().isBefore(request.getDepartureTime())) {
            throw new BadRequestException("Arrival time must be after departure time");
        }
        
        // Check if flight number already exists
        if (flightRepository.findByFlightNumber(request.getFlightNumber()).isPresent()) {
            throw new BadRequestException("Flight number already exists");
        }
        
        // Get departure airport
        Airport departureAirport = airportRepository.findById(request.getDepartureAirportId())
                .orElseThrow(() -> new ResourceNotFoundException("Departure airport not found"));
        
        // Get arrival airport
        Airport arrivalAirport = airportRepository.findById(request.getArrivalAirportId())
                .orElseThrow(() -> new ResourceNotFoundException("Arrival airport not found"));
        
        // Calculate duration
        long durationMinutes = ChronoUnit.MINUTES.between(request.getDepartureTime(), request.getArrivalTime());
        
        // Create flight
        Flight flight = Flight.builder()
                .flightNumber(request.getFlightNumber())
                .departureAirport(departureAirport)
                .arrivalAirport(arrivalAirport)
                .departureTime(request.getDepartureTime())
                .arrivalTime(request.getArrivalTime())
                .durationMinutes((int) durationMinutes)
                .status(Flight.FlightStatus.SCHEDULED)
                .gateNumber(request.getGateNumber())
                .terminal(request.getTerminal())
                .aircraftType(request.getAircraftType())
                .build();
        
        Flight savedFlight = flightRepository.save(flight);
        log.info("Flight created successfully: {}", savedFlight.getFlightNumber());
        
        return modelMapper.map(savedFlight, FlightResponse.class);
    }
    
    /**
     * Get all flights with pagination
     */
    @Transactional(readOnly = true)
    public Page<FlightResponse> getAllFlights(Pageable pageable) {
        return flightRepository.findAll(pageable)
                .map(flight -> modelMapper.map(flight, FlightResponse.class));
    }
    
    /**
     * Search flights
     */
    @Transactional(readOnly = true)
    public Page<FlightResponse> searchFlights(FlightSearchRequest request, Pageable pageable) {
        if (request.getDepartureAirportCode() != null && request.getArrivalAirportCode() != null && request.getDepartureDate() != null) {
            // Search by specific criteria
            Airport departureAirport = airportRepository.findByAirportCode(request.getDepartureAirportCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Departure airport not found"));
            
            Airport arrivalAirport = airportRepository.findByAirportCode(request.getArrivalAirportCode())
                    .orElseThrow(() -> new ResourceNotFoundException("Arrival airport not found"));
            
            return flightRepository.findFlights(departureAirport, arrivalAirport, request.getDepartureDate(), pageable)
                    .map(flight -> modelMapper.map(flight, FlightResponse.class));
        } else if (request.getSearchTerm() != null) {
            // General search
            return flightRepository.searchFlights(request.getSearchTerm(), pageable)
                    .map(flight -> modelMapper.map(flight, FlightResponse.class));
        } else {
            // Return all flights
            return getAllFlights(pageable);
        }
    }
    
    /**
     * Get flight by ID
     */
    @Transactional(readOnly = true)
    public FlightResponse getFlightById(Long id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id: " + id));
        return modelMapper.map(flight, FlightResponse.class);
    }
    
    /**
     * Get flight by flight number
     */
    @Transactional(readOnly = true)
    public FlightResponse getFlightByNumber(String flightNumber) {
        Flight flight = flightRepository.findByFlightNumber(flightNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with number: " + flightNumber));
        return modelMapper.map(flight, FlightResponse.class);
    }
    
    /**
     * Update flight
     */
    public FlightResponse updateFlight(Long id, UpdateFlightRequest request) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id: " + id));
        
        // Validate departure and arrival airports are different
        if (request.getDepartureAirportId().equals(request.getArrivalAirportId())) {
            throw new BadRequestException("Departure and arrival airports cannot be the same");
        }
        
        // Validate times
        if (request.getArrivalTime().isBefore(request.getDepartureTime())) {
            throw new BadRequestException("Arrival time must be after departure time");
        }
        
        // Check if flight number is being changed and if it already exists
        if (!flight.getFlightNumber().equals(request.getFlightNumber()) &&
            flightRepository.findByFlightNumber(request.getFlightNumber()).isPresent()) {
            throw new BadRequestException("Flight number already exists");
        }
        
        // Get airports
        Airport departureAirport = airportRepository.findById(request.getDepartureAirportId())
                .orElseThrow(() -> new ResourceNotFoundException("Departure airport not found"));
        
        Airport arrivalAirport = airportRepository.findById(request.getArrivalAirportId())
                .orElseThrow(() -> new ResourceNotFoundException("Arrival airport not found"));
        
        // Calculate duration
        long durationMinutes = ChronoUnit.MINUTES.between(request.getDepartureTime(), request.getArrivalTime());
        
        // Update flight
        flight.setFlightNumber(request.getFlightNumber());
        flight.setDepartureAirport(departureAirport);
        flight.setArrivalAirport(arrivalAirport);
        flight.setDepartureTime(request.getDepartureTime());
        flight.setArrivalTime(request.getArrivalTime());
        flight.setDurationMinutes((int) durationMinutes);
        flight.setGateNumber(request.getGateNumber());
        flight.setTerminal(request.getTerminal());
        flight.setAircraftType(request.getAircraftType());
        
        Flight savedFlight = flightRepository.save(flight);
        log.info("Flight updated successfully: {}", savedFlight.getFlightNumber());
        
        return modelMapper.map(savedFlight, FlightResponse.class);
    }
    
    /**
     * Update flight status
     */
    public FlightResponse updateFlightStatus(Long id, Flight.FlightStatus status) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id: " + id));
        
        flight.setStatus(status);
        Flight savedFlight = flightRepository.save(flight);
        
        log.info("Flight status updated to {} for flight: {}", status, savedFlight.getFlightNumber());
        
        return modelMapper.map(savedFlight, FlightResponse.class);
    }
    
    /**
     * Delete flight
     */
    public void deleteFlight(Long id) {
        Flight flight = flightRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found with id: " + id));
        
        // Check if flight has bookings
        if (!flight.getBookings().isEmpty()) {
            throw new BadRequestException("Cannot delete flight with existing bookings");
        }
        
        flightRepository.delete(flight);
        log.info("Flight deleted successfully: {}", flight.getFlightNumber());
    }
    
    /**
     * Get flights by status
     */
    @Transactional(readOnly = true)
    public Page<FlightResponse> getFlightsByStatus(Flight.FlightStatus status, Pageable pageable) {
        return flightRepository.findByStatus(status, pageable)
                .map(flight -> modelMapper.map(flight, FlightResponse.class));
    }
    
    /**
     * Get flights by date range
     */
    @Transactional(readOnly = true)
    public Page<FlightResponse> getFlightsByDateRange(LocalDateTime startTime, LocalDateTime endTime, Pageable pageable) {
        return flightRepository.findFlightsByDepartureTimeBetween(startTime, endTime, pageable)
                .map(flight -> modelMapper.map(flight, FlightResponse.class));
    }
    
    /**
     * Get flights by airport
     */
    @Transactional(readOnly = true)
    public Page<FlightResponse> getFlightsByAirport(Long airportId, Pageable pageable) {
        return flightRepository.findByAirportId(airportId, pageable)
                .map(flight -> modelMapper.map(flight, FlightResponse.class));
    }
    
    /**
     * Get upcoming flights
     */
    @Transactional(readOnly = true)
    public List<FlightResponse> getUpcomingFlights() {
        List<Flight> flights = flightRepository.findUpcomingFlights(LocalDateTime.now());
        return flights.stream()
                .map(flight -> modelMapper.map(flight, FlightResponse.class))
                .toList();
    }
    
    /**
     * Get flight statistics
     */
    @Transactional(readOnly = true)
    public FlightStatsResponse getFlightStats() {
        long totalFlights = flightRepository.count();
        long scheduledFlights = flightRepository.countByStatus(Flight.FlightStatus.SCHEDULED);
        long delayedFlights = flightRepository.countByStatus(Flight.FlightStatus.DELAYED);
        long cancelledFlights = flightRepository.countByStatus(Flight.FlightStatus.CANCELLED);
        
        LocalDateTime thirtyDaysAgo = LocalDateTime.now().minusDays(30);
        long flightsLast30Days = flightRepository.countFlightsBetween(thirtyDaysAgo, LocalDateTime.now());
        
        return FlightStatsResponse.builder()
                .totalFlights(totalFlights)
                .scheduledFlights(scheduledFlights)
                .delayedFlights(delayedFlights)
                .cancelledFlights(cancelledFlights)
                .flightsLast30Days(flightsLast30Days)
                .build();
    }
    
    @lombok.Data
    @lombok.Builder
    public static class FlightStatsResponse {
        private long totalFlights;
        private long scheduledFlights;
        private long delayedFlights;
        private long cancelledFlights;
        private long flightsLast30Days;
    }
}