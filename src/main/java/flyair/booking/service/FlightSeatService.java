package flyair.booking.service;

import flyair.booking.dto.request.CreateFlightSeatRequest;
import flyair.booking.dto.request.UpdateFlightSeatRequest;
import flyair.booking.dto.request.CreateBulkFlightSeatsRequest;
import flyair.booking.dto.response.FlightSeatResponse;
import flyair.booking.exception.BadRequestException;
import flyair.booking.exception.ResourceNotFoundException;
import flyair.booking.model.Flight;
import flyair.booking.model.FlightSeat;
import flyair.booking.model.Seat;
import flyair.booking.repository.FlightRepository;
import flyair.booking.repository.FlightSeatRepository;
import flyair.booking.repository.SeatRepository;
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
public class FlightSeatService {
    
    private final FlightSeatRepository flightSeatRepository;
    private final FlightRepository flightRepository;
    private final SeatRepository seatRepository;
    private final ModelMapper modelMapper;
    
    /**
     * Create flight seat assignment
     */
    public FlightSeatResponse createFlightSeat(CreateFlightSeatRequest request) {
        // Get flight
        Flight flight = flightRepository.findById(request.getFlightId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));
        
        // Get seat
        Seat seat = seatRepository.findById(request.getSeatId())
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found"));
        
        // Check if this flight-seat combination already exists
        if (flightSeatRepository.findByFlightIdAndSeatId(request.getFlightId(), request.getSeatId()).isPresent()) {
            throw new BadRequestException("This seat is already assigned to this flight");
        }
        
        FlightSeat flightSeat = FlightSeat.builder()
                .flight(flight)
                .seat(seat)
                .price(request.getPrice())
                .isAvailable(true)
                .isOccupied(false)
                .build();
        
        FlightSeat savedFlightSeat = flightSeatRepository.save(flightSeat);
        log.info("Flight seat created successfully for flight: {} seat: {}", 
                flight.getFlightNumber(), seat.getSeatNumber());
        
        return modelMapper.map(savedFlightSeat, FlightSeatResponse.class);
    }
    
    /**
     * Get available seats for a flight
     */
    @Transactional(readOnly = true)
    public List<FlightSeatResponse> getAvailableSeatsForFlight(Long flightId) {
        List<FlightSeat> flightSeats = flightSeatRepository.findAvailableByFlightId(flightId);
        return flightSeats.stream()
                .map(fs -> modelMapper.map(fs, FlightSeatResponse.class))
                .toList();
    }
    
    /**
     * Get available seats for a flight by class
     */
    @Transactional(readOnly = true)
    public List<FlightSeatResponse> getAvailableSeatsForFlightByClass(Long flightId, Seat.SeatClass seatClass) {
        List<FlightSeat> flightSeats = flightSeatRepository.findAvailableByFlightIdAndSeatClass(flightId, seatClass);
        return flightSeats.stream()
                .map(fs -> modelMapper.map(fs, FlightSeatResponse.class))
                .toList();
    }
    
    /**
     * Search flight seats
     */
    @Transactional(readOnly = true)
    public Page<FlightSeatResponse> searchFlightSeats(String searchTerm, Pageable pageable) {
        return flightSeatRepository.searchFlightSeats(searchTerm, pageable)
                .map(fs -> modelMapper.map(fs, FlightSeatResponse.class));
    }
    
    /**
     * Update flight seat
     */
    public FlightSeatResponse updateFlightSeat(Long id, UpdateFlightSeatRequest request) {
        FlightSeat flightSeat = flightSeatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Flight seat not found with id: " + id));
        
        if (request.getPrice() != null) {
            flightSeat.setPrice(request.getPrice());
        }
        if (request.getIsAvailable() != null) {
            flightSeat.setIsAvailable(request.getIsAvailable());
        }
        
        FlightSeat savedFlightSeat = flightSeatRepository.save(flightSeat);
        log.info("Flight seat updated successfully: {}", savedFlightSeat.getId());
        
        return modelMapper.map(savedFlightSeat, FlightSeatResponse.class);
    }
    
    /**
     * Book a seat (mark as occupied)
     */
    public FlightSeatResponse bookSeat(Long flightSeatId) {
        FlightSeat flightSeat = flightSeatRepository.findById(flightSeatId)
                .orElseThrow(() -> new ResourceNotFoundException("Flight seat not found"));
        
        if (!flightSeat.getIsAvailable()) {
            throw new BadRequestException("Seat is not available");
        }
        
        if (flightSeat.getIsOccupied()) {
            throw new BadRequestException("Seat is already occupied");
        }
        
        flightSeat.setIsOccupied(true);
        flightSeat.setIsAvailable(false);
        
        FlightSeat savedFlightSeat = flightSeatRepository.save(flightSeat);
        log.info("Seat booked successfully: {} on flight: {}", 
                savedFlightSeat.getSeat().getSeatNumber(), 
                savedFlightSeat.getFlight().getFlightNumber());
        
        return modelMapper.map(savedFlightSeat, FlightSeatResponse.class);
    }

    /**
     * Create multiple flight seat assignments
     */
    @Transactional
    public List<FlightSeatResponse> createBulkFlightSeats(CreateBulkFlightSeatsRequest request) {
        // Get flight
        Flight flight = flightRepository.findById(request.getFlightId())
                .orElseThrow(() -> new ResourceNotFoundException("Flight not found"));

        List<FlightSeat> flightSeatsToSave = request.getSeats().stream()
                .map(seatDetails -> {
                    // Get seat
                    Seat seat = seatRepository.findById(seatDetails.getSeatId())
                            .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + seatDetails.getSeatId()));

                    // Check if this flight-seat combination already exists
                    if (flightSeatRepository.findByFlightIdAndSeatId(request.getFlightId(), seatDetails.getSeatId()).isPresent()) {
                        log.warn("Flight seat already exists for flight {} and seat {}", request.getFlightId(), seatDetails.getSeatId());
                        // Optionally skip or update. For now, let's skip duplicates.
                        return null; // Skip this seat assignment
                    }

                    FlightSeat flightSeat = FlightSeat.builder()
                            .flight(flight)
                            .seat(seat)
                            .price(seatDetails.getPrice()) // Get price from seatDetails DTO
                            .isAvailable(seatDetails.getIsAvailable())
                            .isOccupied(false)
                            .build();

                    return flightSeat;
                })
                .filter(flightSeat -> flightSeat != null) // Filter out nulls from skipped duplicates
                .collect(java.util.stream.Collectors.toList());

        List<FlightSeat> savedFlightSeats = flightSeatRepository.saveAll(flightSeatsToSave);
        log.info("Created {} flight seat assignments for flight: {}", 
                savedFlightSeats.size(), flight.getFlightNumber());

        return savedFlightSeats.stream()
                .map(flightSeat -> modelMapper.map(flightSeat, FlightSeatResponse.class))
                .toList();
    }
}