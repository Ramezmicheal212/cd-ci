package com.example.demo.catalog.mapper;
import com.example.demo.catalog.dto.*;
import com.example.demo.catalog.entity.Amenity;
import org.springframework.stereotype.Component;

@Component
public class AmenityMapper {

    public static AmenityResponseDTO toResponseDTO(Amenity amenity) {
        if (amenity == null) {
            return null;
        }
        return new AmenityResponseDTO(
            amenity.getId(),
            amenity.getName(),
            amenity.getDescription()
        );
    }

    public static Amenity toEntity(AmenityRequestDTO dto) {
        if (dto == null) {
            return null;
        }
        Amenity amenity = new Amenity();
        amenity.setName(dto.name());
        amenity.setDescription(dto.description());
        return amenity;
    }

}
