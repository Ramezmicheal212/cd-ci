package com.example.demo.catalog.service;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.example.demo.Booking.bookingRepo.BookingRepository;
import com.example.demo.Booking.entityBooking.BookingStatus;
import com.example.demo.ExceptionHandler.BookingException;
import com.example.demo.ExceptionHandler.ResourceNotFoundException;
import com.example.demo.catalog.Specification.RoomSpec;
import com.example.demo.catalog.dto.RoomPartialUpdateDTO;
import com.example.demo.catalog.dto.RoomRequestDTO;
import com.example.demo.catalog.dto.RoomResponseDTO;
import com.example.demo.catalog.entity.Amenity;
import com.example.demo.catalog.entity.Hotel;
import com.example.demo.catalog.entity.Room;
import com.example.demo.catalog.mapper.RoomMapper;
import com.example.demo.catalog.mapper.HotelMapper;
import com.example.demo.catalog.repository.AmenityRepository;
import com.example.demo.catalog.repository.HotelRepository;
import com.example.demo.catalog.repository.RoomRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class RoomServiceImpl implements RoomService {

    private final RoomRepository roomRepository;
    private final HotelRepository hotelRepository;
    private final RoomMapper roomMapper;
    private final AmenityRepository amenityRepository;
    private final BookingRepository bookingRepository;

    @Override
    public RoomResponseDTO createRoom(Long hotelId, RoomRequestDTO requestDTO) throws IOException {
        
        Hotel hotel = hotelRepository.findByIdAndIsActiveTrue(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + hotelId));

                 MultipartFile imageFile = requestDTO.imageUrl() ;     
                String fileName = HotelMapper.saveImage(imageFile);
        Room room = roomMapper.toEntity(requestDTO, hotel);

        if (fileName != null) {
            room.setImageUrl(fileName);
        }
        else {
            room.setImageUrl(null);
        }

        if (requestDTO.amenityIds() != null) {
            Set<Amenity> amenities = new HashSet<>(amenityRepository.findAllById(requestDTO.amenityIds()));
            room.setAmenities(amenities);
        }
        Room savedRoom = roomRepository.save(room);

        return roomMapper.toResponseDTO(savedRoom);
    }

    @Override
    public List<RoomResponseDTO> getAllRoomsByHotelId(Long hotelId, Integer capacity, BigDecimal minPrice, BigDecimal maxPrice, String roomType, Set<String> amenities) {
        if (!hotelRepository.existsByIdAndIsActiveTrue(hotelId)) {
            throw new ResourceNotFoundException("Hotel not found with id: " + hotelId);
        }

        Specification<Room> spec = Specification
                .where(RoomSpec.isActive())
                .and(RoomSpec.belongsToHotel(hotelId))
                .and(RoomSpec.hasCapacity(capacity))
                .and(RoomSpec.hasPriceBetween(minPrice, maxPrice))
                .and(RoomSpec.hasRoomType(roomType))
                .and(RoomSpec.hasAmenities(amenities));

        return roomRepository.findAll(spec)
                .stream()
                .map(roomMapper::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    public RoomResponseDTO getRoomById(Long roomId) {
        Room room = roomRepository.findByIdAndIsActiveTrue(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

        return roomMapper.toResponseDTO(room);
    }

    @Override
    public RoomResponseDTO updateRoom(Long roomId, Long hotelId, RoomRequestDTO requestDTO) {
        Room room = roomRepository.findByIdAndIsActiveTrue(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

        Hotel hotel = hotelRepository.findByIdAndIsActiveTrue(hotelId)
                .orElseThrow(() -> new ResourceNotFoundException("Hotel not found with id: " + hotelId));

        if (!room.getHotel().getId().equals(hotelId)) {
            throw new ResourceNotFoundException("Room not found with id: " + roomId + " in hotel: " + hotelId);
        }

        room.setRoomType(requestDTO.roomType());
        room.setCapacity(requestDTO.capacity());
        room.setBasePrice(requestDTO.basePrice());
        room.setHotel(hotel);

        if (requestDTO.amenityIds() != null) {
            Set<Amenity> amenities = new HashSet<>(amenityRepository.findAllById(requestDTO.amenityIds()));
            room.setAmenities(amenities);
        }

        Room updatedRoom = roomRepository.save(room);

        return roomMapper.toResponseDTO(updatedRoom);
    }

    @Override
    public RoomResponseDTO patchRoom(Long roomId, Long hotelId, RoomPartialUpdateDTO partialUpdateDTO) {
        Room room = roomRepository.findByIdAndIsActiveTrue(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

        if (!hotelRepository.existsByIdAndIsActiveTrue(hotelId)) {
            throw new ResourceNotFoundException("Hotel not found with id: " + hotelId);
        }

        if (!room.getHotel().getId().equals(hotelId)) {
            throw new ResourceNotFoundException("Room not found with id: " + roomId + " in hotel: " + hotelId);
        }

        if (partialUpdateDTO.roomType() != null) {
            room.setRoomType(partialUpdateDTO.roomType());
        }

        if (partialUpdateDTO.capacity() != null) {
            room.setCapacity(partialUpdateDTO.capacity());
        }

        if (partialUpdateDTO.basePrice() != null) {
            room.setBasePrice(partialUpdateDTO.basePrice());
        }

        if (partialUpdateDTO.amenityIds() != null) {
            Set<Amenity> amenities = new HashSet<>(amenityRepository.findAllById(partialUpdateDTO.amenityIds()));
            room.setAmenities(amenities);
        }

        Room updatedRoom = roomRepository.save(room);
        return roomMapper.toResponseDTO(updatedRoom);
    }

    @Override
    public void deleteRoom(Long roomId) {
        archiveRoom(roomId);
    }

    @Override
    public void archiveRoom(Long roomId) {
        Room room = roomRepository.findByIdAndIsActiveTrue(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found with id: " + roomId));

        if (bookingRepository.existsActiveBookingsByRoomId(roomId, BookingStatus.CANCELLED)) {
            throw new BookingException("Room has existing bookings and cannot be archived");
        }

        room.setIsActive(false);
        roomRepository.save(room);
    }
}
