package com.example.demo.catalog.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;

import com.example.demo.ExceptionHandler.ResourceNotFoundException;
import com.example.demo.auth.service.CustomUserDetailsService.CustomUserDetails;
import com.example.demo.catalog.dto.Guest.GuestRequestDTO;
import com.example.demo.catalog.dto.Guest.GuestResponseDTO;
import com.example.demo.catalog.entity.Guest;
import com.example.demo.catalog.repository.GuestRepository;
import com.example.demo.auth.entity.User;
import com.example.demo.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class GuestService {

    private final GuestRepository guestRepository;
    private final UserRepository userRepository;

    public GuestResponseDTO createGuest(GuestRequestDTO request) {
        if (guestRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalArgumentException("A guest with email " + request.getEmail() + " already exists.");
        }

    
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }
          Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = jwt.getClaim("userId");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));
     


        Guest guest = Guest.builder()
                .name(request.getName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .user(user)
                .build();

        return toDTO(guestRepository.save(guest));
    }

    public GuestResponseDTO getGuest(Long id) {
        Guest guest = guestRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Guest not found with id: " + id));
        return toDTO(guest);
    }

    public List<GuestResponseDTO> getAllGuests() {
        return guestRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

// my guests 
    public List<GuestResponseDTO> getMyGuests() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user found");
        }
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = jwt.getClaim("userId");
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        return user.getGuests()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    private GuestResponseDTO toDTO(Guest guest) {
        GuestResponseDTO dto = new GuestResponseDTO();
        dto.setId(guest.getId());
        dto.setName(guest.getName());
        dto.setEmail(guest.getEmail());
        dto.setPhone(guest.getPhone());
        return dto;
    }


}
