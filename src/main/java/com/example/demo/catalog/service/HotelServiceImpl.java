package com.example.demo.catalog.service;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.RequiredArgsConstructor;

import com.example.demo.PagedResponse;
import com.example.demo.ExceptionHandler.*;
import com.example.demo.catalog.Specification.HotelSpec;
import com.example.demo.catalog.entity.Hotel;
import com.example.demo.catalog.mapper.HotelMapper;
import com.example.demo.catalog.repository.*;
import com.example.demo.catalog.dto.*;
import com.example.demo.Booking.bookingRepo.BookingRepository;
import com.example.demo.Booking.entityBooking.BookingStatus;
import java.util.*;

import org.springframework.data.domain.*;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.domain.Specification;

import java.io.IOException;

import com.example.demo.auth.entity.HotelRole;
import com.example.demo.auth.entity.User;
import com.example.demo.auth.entity.UserHotel;
import com.example.demo.auth.entity.EmploymentStatus;
import com.example.demo.auth.repository.UserRepository;
import com.example.demo.auth.repository.HotelRoleRepository;
import com.example.demo.auth.repository.UserHotelRepository;

@Service
@RequiredArgsConstructor
public class HotelServiceImpl implements HotelService {

    private static final Logger log = LoggerFactory.getLogger(HotelServiceImpl.class);

    private final HotelRepository hotelRepository;
    private final UserRepository userRepository;
    private final UserHotelRepository userhotelRepository;
    private final HotelRoleRepository hotelRoleRepository;
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    @Override
    public PagedResponse<HotelResponseDTO> getAllHotels(Pageable pageable, String name, String city, String country) {

        Specification<Hotel> spec = Specification
                .where(HotelSpec.isActive())
                .and(HotelSpec.hasName(name))
                .and(HotelSpec.hasCity(city))
                .and(HotelSpec.hasCountry(country));
        Page<Hotel> hotelsPage = hotelRepository.findAll(spec, pageable);
        Page<HotelResponseDTO> hotelResponseDTOs = hotelsPage.map(HotelMapper::toDTO);

        return PagedResponse.from(hotelsPage, hotelResponseDTOs.getContent());
    }

    @Override
    public HotelResponseDTO getHotelById(Long id) {
        Hotel hotel = hotelRepository.findByIdAndIsActiveTrue(id).orElseThrow(() -> new HotelNotFoundException(id));
        return HotelMapper.toDTO(hotel);
    }

 @Override
public HotelResponseDTO createHotel(HotelRequestDTO dto) throws IOException {

    Hotel hotel = HotelMapper.toEntity(dto);

    Hotel savedHotel = hotelRepository.save(hotel);

    List<MultipartFile> images = collectUploadedImages(dto);

    List<String> imageUrls = new ArrayList<>();

    if (!images.isEmpty()) {
        imageUrls = HotelMapper.saveImages(images);
    }

    log.info("createHotel: applying {} image urls", imageUrls.size());

    HotelMapper.applyImages(savedHotel, imageUrls, true);

    Hotel finalHotel = hotelRepository.save(savedHotel);

    return HotelMapper.toDTO(finalHotel);
}

    @Override
    public HotelResponseDTO updateHotel(Long id, HotelRequestDTO hotelRequestDTO) throws IOException {
        Hotel existingHotel = hotelRepository.findByIdAndIsActiveTrue(id).orElseThrow(() -> new HotelNotFoundException(id));
        existingHotel.setName(hotelRequestDTO.name());
        existingHotel.setAddress(hotelRequestDTO.address());
        existingHotel.setCity(hotelRequestDTO.city());
        existingHotel.setCountry(hotelRequestDTO.country());
        existingHotel.setDescription(hotelRequestDTO.description());

        List<String> imageUrls = HotelMapper.saveImages(collectUploadedImages(hotelRequestDTO));
        if (!imageUrls.isEmpty()) {
            HotelMapper.applyImages(existingHotel, imageUrls, true);
        }

        return HotelMapper.toDTO(hotelRepository.save(existingHotel));
    }

    @Override
    public HotelResponseDTO partialUpdateHotel(Long id, HotelPartialUpdateDTO partialUpdateDTO) throws Exception {
        Hotel existingHotel = hotelRepository.findByIdAndIsActiveTrue(id).orElseThrow(() -> new HotelNotFoundException(id));

        if (partialUpdateDTO.name() != null && !partialUpdateDTO.name().isEmpty()) {
            existingHotel.setName(partialUpdateDTO.name());
        }
        if (partialUpdateDTO.address() != null && !partialUpdateDTO.address().isEmpty()) {
            existingHotel.setAddress(partialUpdateDTO.address());
        }
        if (partialUpdateDTO.city() != null && !partialUpdateDTO.city().isEmpty()) {
            existingHotel.setCity(partialUpdateDTO.city());
        }
        if (partialUpdateDTO.country() != null && !partialUpdateDTO.country().isEmpty()) {
            existingHotel.setCountry(partialUpdateDTO.country());
        }
        if (partialUpdateDTO.description() != null && !partialUpdateDTO.description().isEmpty()) {
            existingHotel.setDescription(partialUpdateDTO.description());
        }
        List<String> imageUrls = HotelMapper.saveImages(collectUploadedImages(partialUpdateDTO));
        if (!imageUrls.isEmpty()) {
            HotelMapper.applyImages(existingHotel, imageUrls, false);
        }

        return HotelMapper.toDTO(hotelRepository.save(existingHotel));
    }

  private List<MultipartFile> collectUploadedImages(HotelRequestDTO dto) {

    if (dto.hotelImages() == null) {
        return new ArrayList<>();
    }

    return Arrays.stream(dto.hotelImages())
            .filter(f -> f != null && !f.isEmpty())
            .collect(java.util.stream.Collectors.toCollection(ArrayList::new));
}
    private List<MultipartFile> collectUploadedImages(HotelPartialUpdateDTO partialUpdateDTO) {
        List<MultipartFile> imageFiles = new ArrayList<>();

        if (partialUpdateDTO.hotelImages() != null) {
            imageFiles.addAll(Arrays.stream(partialUpdateDTO.hotelImages())
                    .filter(imageFile -> imageFile != null && !imageFile.isEmpty())
                    .collect(java.util.stream.Collectors.toCollection(ArrayList::new)));
        }

        return imageFiles;
    }

    @Transactional
    @Override
    public void deleteHotel(Long id) {

        archiveHotel(id);
    }

    @Transactional
    @Override
    public void archiveHotel(Long id) {

        Hotel hotel = hotelRepository.findByIdAndIsActiveTrue(id).orElseThrow(() -> new HotelNotFoundException(id));

        if (bookingRepository.existsActiveBookingsByHotelId(id, BookingStatus.CANCELLED)) {
            throw new BookingException("Hotel has existing bookings and cannot be archived");
        }

        hotel.setIsActive(false);
        hotelRepository.save(hotel);

        roomRepository.deactivateRoomsByHotelId(id);
        userhotelRepository.updateStatusByHotelId(id, EmploymentStatus.INACTIVE);
    }

    @Transactional
    @Override
    public void restoreHotel(Long id) {
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new HotelNotFoundException(id));

        if (Boolean.TRUE.equals(hotel.getIsActive())) {
            return;
        }

        hotel.setIsActive(true);
        hotelRepository.save(hotel);

        roomRepository.activateRoomsByHotelId(id);
        userhotelRepository.updateStatusByHotelId(id, EmploymentStatus.ACTIVE);
    }

    @Override
    public Map<String, String> getHotelManager(Long hotelId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    // Hotel access
    @Override
    public void addEmployeeToHotel(Long hotelId, AssignEmp assignEmpToHotelDTO) {
        User user = userRepository.findById(assignEmpToHotelDTO.userId())
                .orElseThrow(() -> new UserNotFoundException(assignEmpToHotelDTO.userId()));
        HotelRole role = hotelRoleRepository.findById(assignEmpToHotelDTO.hotelRoleId())
                .orElseThrow(() -> new RoleNotFoundException(assignEmpToHotelDTO.hotelRoleId()));

        Hotel hotel = hotelRepository.findByIdAndIsActiveTrue(hotelId)
                .orElseThrow(() -> new HotelNotFoundException(hotelId));
        UserHotel existingUserHotel = userhotelRepository.findByUserIdAndHotelId(user.getId(), hotel.getId()).orElse(null);

        boolean exists = existingUserHotel != null;
        if (exists && EmploymentStatus.ACTIVE.equals(existingUserHotel.getStatus())) {
            throw new RuntimeException("User already has access to this hotel");
        } else if (exists && EmploymentStatus.INACTIVE.equals(existingUserHotel.getStatus())) {
            existingUserHotel.setStatus(EmploymentStatus.ACTIVE);
            existingUserHotel.setHotelRole(role);
            userhotelRepository.save(existingUserHotel);
        } else {
            UserHotel userHotel = new UserHotel();
            userHotel.setUser(user);
            userHotel.setHotel(hotel);
            userHotel.setHotelRole(role);
            userHotel.setStatus(EmploymentStatus.ACTIVE);
            userhotelRepository.save(userHotel);
        }
    }

    @Override
    public void removeEmployeeFromHotel(Long hotelId, Long userId) {
        hotelRepository.findByIdAndIsActiveTrue(hotelId).orElseThrow(() -> new HotelNotFoundException(hotelId));
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        UserHotel userHotel = userhotelRepository.findByUserIdAndHotelIdAndStatus(userId, hotelId, EmploymentStatus.ACTIVE);
        if (userHotel == null) {
            throw new ResourceNotFoundException("User does not have access to this hotel");
        }
        userHotel.setStatus(EmploymentStatus.INACTIVE);
        userhotelRepository.save(userHotel);
    }

    //restor employee access to hotel when hotel is restored
    public void restoreEmployeeAccessToHotel(Long hotelId, Long userId) {
        hotelRepository.findById(hotelId).orElseThrow(() -> new HotelNotFoundException(hotelId));
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));

        UserHotel userHotel = userhotelRepository.findByUserIdAndHotelIdAndStatus(userId, hotelId, EmploymentStatus.INACTIVE);
        if (userHotel == null) {
            throw new ResourceNotFoundException("User does not have access to this hotel");
        }
        userHotel.setStatus(EmploymentStatus.ACTIVE);
        userhotelRepository.save(userHotel);
    }

    // partial update employee access to hotel 
    public void bulkUpdateHotelEmployees(Long hotelId, Long userId, BulkUpdateEmployeesRequest request) {
        hotelRepository.findById(hotelId).orElseThrow(() -> new HotelNotFoundException(hotelId));
        userRepository.findById(userId).orElseThrow(() -> new UserNotFoundException(userId));
        HotelRole role = hotelRoleRepository.findById(request.hotelRoleId()).orElseThrow(() -> new RoleNotFoundException(request.hotelRoleId()));

        UserHotel userHotel = userhotelRepository.findByUserIdAndHotelIdAndStatus(userId, hotelId, EmploymentStatus.ACTIVE);
        if (userHotel == null) {
            throw new ResourceNotFoundException("User does not have access to this hotel");
        }
        userHotel.setHotelRole(role);
        userhotelRepository.save(userHotel);
    }

    @Override
    public List<EmployeeResponseDTO> listHotelEmployees(Long hotelId) {
        hotelRepository.findByIdAndIsActiveTrue(hotelId).orElseThrow(() -> new HotelNotFoundException(hotelId));
        List<UserHotel> userHotels = userhotelRepository.findByHotelId(hotelId);

        return userHotels.stream()
                .map(uh -> new EmployeeResponseDTO(
                uh.getUser().getId(),
                uh.getUser().getUsername(),
                uh.getUser().getEmail(),
                uh.getHotelRole().getName(),
                uh.getStatus()
        ))
                .toList();
    }

}
