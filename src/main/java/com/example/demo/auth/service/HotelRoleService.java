package com.example.demo.auth.service;

import java.util.List;

import com.example.demo.auth.UserDTOs.HotelRoleRequestDTO;
import com.example.demo.auth.UserDTOs.HotelRoleResponseDTO;
import com.example.demo.auth.UserDTOs.PermissionResponseDTO;
import com.example.demo.auth.UserDTOs.UpdateHotelRoleRequest;

public interface HotelRoleService {

    HotelRoleResponseDTO createHotelRole(HotelRoleRequestDTO request);

    HotelRoleResponseDTO partialUpdateHotelRole(Long id, UpdateHotelRoleRequest request);

    List<HotelRoleResponseDTO> getAllHotelRoles();

    HotelRoleResponseDTO getHotelRoleById(Long id);

    void deleteHotelRole(Long id);

    List<PermissionResponseDTO> getAllHotelPermissions();
}
