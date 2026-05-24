package com.example.demo.auth.Mapper;

import java.util.List;

import com.example.demo.auth.UserDTOs.HotelRoleResponseDTO;
import com.example.demo.auth.entity.HotelPermission;
import com.example.demo.auth.entity.HotelRole;

public class HotelRoleMapper {

    private HotelRoleMapper() {
    }

    public static HotelRoleResponseDTO toHotelRoleResponseDTO(HotelRole hotelRole) {
        List<String> permissionNames = hotelRole.getPermissions().stream()
                .map(HotelPermission::getName)
                .toList();

        return new HotelRoleResponseDTO(hotelRole.getId(), hotelRole.getName(), permissionNames);
    }
}
