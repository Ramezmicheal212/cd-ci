package com.example.demo.catalog.service;
import org.springframework.stereotype.Service;
import lombok.AllArgsConstructor;
import com.example.demo.catalog.repository.*;
import com.example.demo.catalog.dto.*;
import com.example.demo.catalog.entity.Amenity;
import com.example.demo.catalog.mapper.AmenityMapper;
import com.example.demo.ExceptionHandler.ResourceNotFoundException;
import java.util.List;  
import java.util.stream.Collectors;
@Service
@AllArgsConstructor
public class AmenityService {

    private final AmenityRepository amenityRepository;

    public List<AmenityResponseDTO> getAllAmenities() {
        return amenityRepository.findAll()
                .stream()
                .map(AmenityMapper::toResponseDTO)
                .collect(Collectors.toList());
    }
 

    public AmenityResponseDTO  getAmenityById(Long id) {
        Amenity amenity = amenityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Amenity not found with id: " + id));   

        return AmenityMapper.toResponseDTO(amenity);
    }

    public AmenityResponseDTO createAmenity(AmenityRequestDTO amenityRequestDTO) {
        Amenity amenity = AmenityMapper.toEntity(amenityRequestDTO);
        Amenity savedAmenity = amenityRepository.save(amenity);
        return AmenityMapper.toResponseDTO(savedAmenity);
    }





}
