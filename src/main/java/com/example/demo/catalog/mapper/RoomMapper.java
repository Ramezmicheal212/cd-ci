package com.example.demo.catalog.mapper;

import com.example.demo.catalog.dto.RoomRequestDTO;
import com.example.demo.catalog.dto.RoomResponseDTO;
import com.example.demo.catalog.entity.Hotel;
import com.example.demo.catalog.entity.Room;
import org.springframework.stereotype.Component;

@Component
public class RoomMapper {

    public Room toEntity(RoomRequestDTO dto, Hotel hotel) {
        Room room = new Room();
        room.setRoomType(dto.roomType());
        room.setCapacity(dto.capacity());
        room.setBasePrice(dto.basePrice());
        room.setHotel(hotel);
        
        return room;
    }

    public RoomResponseDTO toResponseDTO(Room room) {
        return new RoomResponseDTO(
                room.getId(),
                room.getRoomType(),
                room.getCapacity(),
                room.getBasePrice(),
                room.getAmenities().stream().map(a -> a.getName()).collect(java.util.stream.Collectors.toSet()),
                room.getHotel().getId(),
                room.getImageUrl()
        );
    }
}