package flyair.booking.service;

import flyair.booking.dto.request.CreateSeatRequest;
import flyair.booking.dto.response.SeatResponse;
import flyair.booking.exception.BadRequestException;
import flyair.booking.exception.ResourceNotFoundException;
import flyair.booking.model.Seat;
import flyair.booking.repository.SeatRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SeatService {
    
    private final SeatRepository seatRepository;
    private final ModelMapper modelMapper;
    
    /**
     * Create a new seat
     */
    public SeatResponse createSeat(CreateSeatRequest request) {
        // Check if seat number already exists
        if (seatRepository.existsBySeatNumber(request.getSeatNumber())) {
            throw new BadRequestException("Seat number already exists: " + request.getSeatNumber());
        }
        
        Seat seat = Seat.builder()
                .seatNumber(request.getSeatNumber().toUpperCase())
                .seatClass(request.getSeatClass())
                .build();
        
        Seat savedSeat = seatRepository.save(seat);
        log.info("Seat created successfully: {}", savedSeat.getSeatNumber());
        
        return modelMapper.map(savedSeat, SeatResponse.class);
    }
    
    /**
     * Get all seats with pagination
     */
    @Transactional(readOnly = true)
    public Page<SeatResponse> getAllSeats(Pageable pageable) {
        return seatRepository.findAll(pageable)
                .map(seat -> modelMapper.map(seat, SeatResponse.class));
    }
    
    /**
     * Search seats
     */
    @Transactional(readOnly = true)
    public Page<SeatResponse> searchSeats(String searchTerm, Pageable pageable) {
        return seatRepository.searchSeats(searchTerm, pageable)
                .map(seat -> modelMapper.map(seat, SeatResponse.class));
    }
    
    /**
     * Get seat by ID
     */
    @Transactional(readOnly = true)
    public SeatResponse getSeatById(Long id) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + id));
        return modelMapper.map(seat, SeatResponse.class);
    }
    
    /**
     * Get seats by class
     */
    @Transactional(readOnly = true)
    public Page<SeatResponse> getSeatsByClass(Seat.SeatClass seatClass, Pageable pageable) {
        return seatRepository.findBySeatClass(seatClass, pageable)
                .map(seat -> modelMapper.map(seat, SeatResponse.class));
    }
    
    /**
     * Update seat
     */
    public SeatResponse updateSeat(Long id, CreateSeatRequest request) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + id));
        
        // Check if seat number is being changed and if it already exists
        if (!seat.getSeatNumber().equals(request.getSeatNumber().toUpperCase()) &&
            seatRepository.existsBySeatNumber(request.getSeatNumber())) {
            throw new BadRequestException("Seat number already exists: " + request.getSeatNumber());
        }
        
        seat.setSeatNumber(request.getSeatNumber().toUpperCase());
        seat.setSeatClass(request.getSeatClass());
        
        Seat savedSeat = seatRepository.save(seat);
        log.info("Seat updated successfully: {}", savedSeat.getSeatNumber());
        
        return modelMapper.map(savedSeat, SeatResponse.class);
    }
    
    /**
     * Delete seat
     */
    public void deleteSeat(Long id) {
        Seat seat = seatRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Seat not found with id: " + id));
        
        // Check if seat has flight seats
        if (!seat.getFlightSeats().isEmpty()) {
            throw new BadRequestException("Cannot delete seat with existing flight seat assignments");
        }
        
        seatRepository.delete(seat);
        log.info("Seat deleted successfully: {}", seat.getSeatNumber());
    }
}