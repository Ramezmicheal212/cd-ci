package com.example.demo.catalog.mapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import org.springframework.web.multipart.MultipartFile;

import com.example.demo.catalog.dto.*;
import com.example.demo.catalog.entity.Hotel;
import com.example.demo.catalog.entity.HotelImage;

public class HotelMapper {

    public static HotelResponseDTO toDTO(Hotel hotel) {
        List<String> imageUrls = hotel.getImages() == null ? List.of() : hotel.getImages().stream()
                .filter(Objects::nonNull)
                .map(HotelImage::getUrl)
                .filter(Objects::nonNull)
                .toList();

        String primaryImageUrl = hotel.getImages() == null ? null : hotel.getImages().stream()
                .filter(Objects::nonNull)
                .filter(image -> Boolean.TRUE.equals(image.getIsPrimary()))
                .map(HotelImage::getUrl)
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(imageUrls.isEmpty() ? null : imageUrls.get(0));

        return new HotelResponseDTO(
                hotel.getId(),
                hotel.getName(),
                hotel.getAddress(),
                hotel.getCity(),
                hotel.getCountry(),
                hotel.getDescription(),
                primaryImageUrl,
                imageUrls
        );
    }

    public static Hotel toEntity(HotelRequestDTO dto) {
        return Hotel.builder()
                .name(dto.name())
                .address(dto.address())
                .city(dto.city())
                .country(dto.country())
                .description(dto.description())
                .build();
    }
    private static final Logger log = LoggerFactory.getLogger(HotelMapper.class);

    public static String saveImage(MultipartFile imageFile) throws IOException {
        if (imageFile == null || imageFile.isEmpty()) {
            return null;
        }

        String uploadDir = "uploads";
        Files.createDirectories(Paths.get(uploadDir));
        String fileName = UUID.randomUUID() + "_" + imageFile.getOriginalFilename();
        Path filePath = Paths.get(uploadDir, fileName);
        try {
            Files.write(filePath, imageFile.getBytes());
            String url = "/uploads/" + fileName; // Return the URL to access the image
            log.info("Saved hotel image to {}", filePath.toAbsolutePath());
            return url;
        } catch (IOException ex) {
            log.error("Failed to save image to {}", filePath.toAbsolutePath(), ex);
            throw ex;
        }
    }

    public static List<String> saveImages(List<MultipartFile> imageFiles) throws IOException {
        if (imageFiles == null || imageFiles.isEmpty()) {
            return List.of();
        }

        List<String> savedImages = new ArrayList<>();
        for (MultipartFile imageFile : imageFiles) {
            String fileName = saveImage(imageFile);
            if (fileName != null) {
                savedImages.add(fileName);
            }
        }
        return savedImages;
    }

    public static void applyImages(Hotel hotel, List<String> imageUrls, boolean replaceExisting) {

        if (imageUrls == null || imageUrls.isEmpty()) {
            return;
        }

        if (hotel.getImages() == null) {
            hotel.setImages(new LinkedHashSet<>());
        }

        if (replaceExisting) {
            hotel.getImages().clear();
        }

        boolean hasPrimaryImage = hotel.getImages().stream()
                .anyMatch(img -> Boolean.TRUE.equals(img.getIsPrimary()));

        for (int i = 0; i < imageUrls.size(); i++) {

            String url = imageUrls.get(i);

            if (url == null || url.isBlank()) {
                continue;
            }

            HotelImage img = new HotelImage();
            img.setUrl(url);
            img.setHotel(hotel);

            img.setIsPrimary(i == 0 && (replaceExisting || !hasPrimaryImage));

            hotel.getImages().add(img);
        }
    }
}
