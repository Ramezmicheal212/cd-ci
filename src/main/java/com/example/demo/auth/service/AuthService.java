package com.example.demo.auth.service;


import org.springframework.security.oauth2.jwt.Jwt;

import com.example.demo.auth.UserDTOs.*;
import com.example.demo.auth.entity.*;



public interface AuthService {

    public LoginResponse login(String username, String password);
    public void register(UserRequestDTO userRequestDTO);
    public void logout(Jwt jwt);
    public LoginResponse refreshToken(String refreshTokenValue);
    public void revokeRefreshToken(String token);
    // Hotel access
    /*public String assignRoleAndHotelToUser(Long userId, HotelAccessRequest hotelAccessRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        HotelRole role = hotelRoleRepository.findById(hotelAccessRequest.hotelRoleId())
                .orElseThrow(() -> new RoleNotFoundException(hotelAccessRequest.hotelRoleId()));

        

        if (hotelAccessRequest.hotelId() != null) {
            Hotel hotel = hotelRepository.findById(hotelAccessRequest.hotelId())
                    .orElseThrow(() -> new HotelNotFoundException(hotelAccessRequest.hotelId()));
            UserHotel userHotel = userhotelRepository.findByUserIdAndHotelId(userId, hotelAccessRequest.hotelId())
                   .orElseGet(() -> new UserHotel());
                userHotel.setUser(user);
                userHotel.setHotel(hotel);
                userHotel.setHotelRole(role);
                userhotelRepository.save(userHotel);


        }
        return "Role and hotel assigned to user successfully";
    }*/
}
