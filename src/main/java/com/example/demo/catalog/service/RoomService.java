package com.example.demo.catalog.service;

import com.example.demo.catalog.dto.RoomPartialUpdateDTO;
import com.example.demo.catalog.dto.RoomRequestDTO;
import com.example.demo.catalog.dto.RoomResponseDTO;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

public interface RoomService {

    RoomResponseDTO createRoom(Long hotelId, RoomRequestDTO requestDTO) throws IOException;

    List<RoomResponseDTO> getAllRoomsByHotelId(Long hotelId, Integer capacity, BigDecimal minPrice, BigDecimal maxPrice, String roomType, Set<String> amenities);

    RoomResponseDTO getRoomById(Long roomId);

    RoomResponseDTO updateRoom(Long roomId, Long hotelId, RoomRequestDTO requestDTO);

    RoomResponseDTO patchRoom(Long roomId, Long hotelId, RoomPartialUpdateDTO partialUpdateDTO);

    void deleteRoom(Long roomId);

    void archiveRoom(Long roomId);
}
