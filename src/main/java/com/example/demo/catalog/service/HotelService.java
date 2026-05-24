package com.example.demo.catalog.service;

import com.example.demo.PagedResponse;
import com.example.demo.catalog.dto.*;

import java.util.List;

import java.util.Map;

import org.springframework.data.domain.Pageable;

public interface HotelService {

    PagedResponse<HotelResponseDTO> getAllHotels(Pageable pageable, String name, String city, String country);

    HotelResponseDTO getHotelById(Long id);

    HotelResponseDTO createHotel(HotelRequestDTO hotelRequestDTO) throws Exception;

    HotelResponseDTO updateHotel(Long id, HotelRequestDTO hotelRequestDTO) throws Exception;

    void deleteHotel(Long id);

    void archiveHotel(Long id);

    void restoreHotel(Long id);

    HotelResponseDTO partialUpdateHotel(Long id, HotelPartialUpdateDTO partialUpdateDTO) throws Exception;

    Map<String, String> getHotelManager(Long hotelId);

    void addEmployeeToHotel(Long hotelId, AssignEmp assignEmpToHotelDTO);

    void removeEmployeeFromHotel(Long hotelId, Long userId);

    List<EmployeeResponseDTO> listHotelEmployees(Long hotelId);
}
