package com.example.demo.auth.config;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.demo.auth.entity.HotelPermission;
import com.example.demo.auth.entity.HotelRole;
import com.example.demo.auth.entity.Permission;
import com.example.demo.auth.entity.Role;
import com.example.demo.auth.entity.User;
import com.example.demo.auth.repository.HotelPermissionRepository;
import com.example.demo.auth.repository.HotelRoleRepository;
import com.example.demo.auth.repository.PermissionRepository;
import com.example.demo.auth.repository.RoleRepository;
import com.example.demo.auth.repository.UserRepository;
import com.example.demo.catalog.entity.Hotel;
import com.example.demo.catalog.entity.HotelImage;
import com.example.demo.catalog.entity.Room;
import com.example.demo.catalog.repository.HotelRepository;
import com.example.demo.catalog.repository.RoomRepository;

@Configuration
public class RbacDataInitializer {

    @Bean
    CommandLineRunner seedRbacDat(RoleRepository roleRepository,
            PermissionRepository permissionRepository,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            HotelPermissionRepository hotelPermissionRepository,
            HotelRoleRepository hotelRoleRepository,
            HotelRepository hotelRepository,
            RoomRepository roomRepository) {
        return args -> {

            // System-level permissions.
            Permission hotelCreate = findOrCreatePermission(permissionRepository, "SYSTEM_CREATE_HOTEL");
            Permission hotelUpdate = findOrCreatePermission(permissionRepository, "SYSTEM_UPDATE_HOTEL");
            Permission hotelDelete = findOrCreatePermission(permissionRepository, "SYSTEM_DELETE_HOTEL");
            Permission roomDelete = findOrCreatePermission(permissionRepository, "SYSTEM_DELETE_ROOM");
            Permission roleManage = findOrCreatePermission(permissionRepository, "SYSTEM_MANAGE_ROLES");

            // Booking permissions.
            Permission bookingCreate = findOrCreatePermission(permissionRepository, "SYSTEM_CREATE_BOOKING");
            Permission bookingView = findOrCreatePermission(permissionRepository, "SYSTEM_VIEW_BOOKING");
            Permission bookingUpdate = findOrCreatePermission(permissionRepository, "SYSTEM_UPDATE_BOOKING");
            Permission bookingDelete = findOrCreatePermission(permissionRepository, "SYSTEM_DELETE_BOOKING");
            Permission bookingPay = findOrCreatePermission(permissionRepository, "SYSTEM_PAY_BOOKING");
            Permission bookingCancel = findOrCreatePermission(permissionRepository, "SYSTEM_CANCEL_BOOKING");
            Permission bookingFilter = findOrCreatePermission(permissionRepository, "SYSTEM_FILTER_BOOKINGS");
            Permission bookingAvailability = findOrCreatePermission(permissionRepository, "SYSTEM_CHECK_BOOKING_AVAILABILITY");
            Permission bookingQuote = findOrCreatePermission(permissionRepository, "SYSTEM_GET_BOOKING_QUOTE");
            Permission bookingGuestHistory = findOrCreatePermission(permissionRepository, "SYSTEM_VIEW_GUEST_BOOKING_HISTORY");
            Permission bookingUpcoming = findOrCreatePermission(permissionRepository, "SYSTEM_VIEW_UPCOMING_BOOKINGS");
            Permission bookingExpire = findOrCreatePermission(permissionRepository, "SYSTEM_EXPIRE_PENDING_BOOKINGS");

            // Hotel-scoped permissions.
            HotelPermission hotelCreatePermission = findOrCreateHotelPermission(hotelPermissionRepository, "HOTEL_CREATE");
            HotelPermission hotelUpdatePermission = findOrCreateHotelPermission(hotelPermissionRepository, "HOTEL_UPDATE");
            HotelPermission hotelDeletePermission = findOrCreateHotelPermission(hotelPermissionRepository, "HOTEL_DELETE");
            HotelPermission hotelViewManagerPermission = findOrCreateHotelPermission(hotelPermissionRepository, "HOTEL_VIEW_MANAGER");
            HotelPermission hotelViewEmployeesPermission = findOrCreateHotelPermission(hotelPermissionRepository, "HOTEL_VIEW_EMPLOYEES");
            HotelPermission addEmployeePermission = findOrCreateHotelPermission(hotelPermissionRepository, "ADD_EMPLOYEE");
            HotelPermission removeEmployeePermission = findOrCreateHotelPermission(hotelPermissionRepository, "REMOVE_EMPLOYEE");
            HotelPermission viewBookingsOfHotelPermission = findOrCreateHotelPermission(hotelPermissionRepository, "VIEW_BOOKINGS_OF_HOTEL");

            Set<Permission> adminPermissions = Set.of(
                    hotelCreate,
                    hotelUpdate,
                    hotelDelete,
                    roomDelete,
                    roleManage,
                    bookingCreate,
                    bookingView,
                    bookingUpdate,
                    bookingDelete,
                    bookingPay,
                    bookingCancel,
                    bookingFilter,
                    bookingAvailability,
                    bookingQuote,
                    bookingGuestHistory,
                    bookingUpcoming,
                    bookingExpire
            );

            Set<HotelPermission> hotelManagerPermissions = Set.of(
                    hotelCreatePermission,
                    hotelUpdatePermission,
                    hotelDeletePermission,
                    hotelViewManagerPermission,
                    hotelViewEmployeesPermission,
                    addEmployeePermission,
                    removeEmployeePermission,
                    viewBookingsOfHotelPermission
            );

            Set<HotelPermission> hotelHrPermissions = Set.of(
                    hotelViewManagerPermission,
                    hotelViewEmployeesPermission,
                    addEmployeePermission,
                    removeEmployeePermission
            );

            Set<HotelPermission> hotelReceptionistPermissions = Set.of(
                    hotelViewManagerPermission,
                    hotelViewEmployeesPermission
            );

            upsertRole(roleRepository, "ADMIN", adminPermissions);
            upsertRole(roleRepository, "USER", Set.of());
            upsertHotelRole(hotelRoleRepository, "HR", hotelHrPermissions);
            upsertHotelRole(hotelRoleRepository, "MANAGER", hotelManagerPermissions);
            upsertHotelRole(hotelRoleRepository, "RECEPTIONIST", hotelReceptionistPermissions);

            registerAdminUser(userRepository, roleRepository, passwordEncoder);

            seedHotelsAndRooms(hotelRepository, roomRepository, 20, 10);

        };

    }

    private Permission findOrCreatePermission(PermissionRepository permissionRepository, String name) {
        return permissionRepository.findByName(name)
                .orElseGet(() -> permissionRepository.save(Permission.builder().name(name).build()));
    }

    private HotelPermission findOrCreateHotelPermission(HotelPermissionRepository hotelPermissionRepository, String name) {
        return hotelPermissionRepository.findByName(name)
                .orElseGet(() -> hotelPermissionRepository.save(HotelPermission.builder().name(name).build()));
    }

    private void upsertRole(RoleRepository roleRepository, String roleName, Set<Permission> permissions) {
        Role role = roleRepository.findByName(roleName)
                .orElseGet(() -> Role.builder().name(roleName).build());

        role.setPermissions(permissions);
        roleRepository.save(role);
    }

    private void upsertHotelRole(HotelRoleRepository hotelRoleRepository, String roleName, Set<HotelPermission> permissions) {
        HotelRole hotelRole = hotelRoleRepository.findByName(roleName)
                .orElseGet(() -> HotelRole.builder().name(roleName).build());

        hotelRole.setPermissions(permissions);
        hotelRoleRepository.save(hotelRole);
    }
    //register admin user with username admin and password admin123 with role admin if not exists

    public void registerAdminUser(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        String adminUsername = "tony";
        String adminPassword = "werT6789";
        String adminRoleName = "ADMIN";
        User adminUser = userRepository.findByUsername(adminUsername).orElse(null);
        if (adminUser == null) {
            Role adminRole = roleRepository.findByName(adminRoleName)
                    .orElseThrow(() -> new RuntimeException("Admin role not found"));

            adminUser = User.builder()
                    .username(adminUsername)
                    .password(passwordEncoder.encode(adminPassword))
                    .roles(Set.of(adminRole))
                    .fullName("Tony Q")
                    .email("tony@example.com")
                    .phoneNumber("1234567890")
                    .enabled(true)
                    .build();

            userRepository.save(adminUser);
        }

    }

    private void seedHotelsAndRooms(HotelRepository hotelRepository, RoomRepository roomRepository, int hotelTarget, int roomTarget) {
        List<String> seededImageUrls = loadSeedUploadUrls();
        List<Hotel> existingHotels = new ArrayList<>(hotelRepository.findAllWithImages());
        int currentHotels = existingHotels.size();

        // Backfill existing seed hotels that have no images so list responses include imageUrls.
        for (Hotel existingHotel : existingHotels) {
            if (existingHotel.getImages() == null || existingHotel.getImages().isEmpty()) {
                attachSeedImage(existingHotel, seededImageUrls, existingHotel.getId() == null ? 0 : existingHotel.getId().intValue());
                hotelRepository.save(existingHotel);
            }
        }

        for (int i = currentHotels + 1; i <= hotelTarget; i++) {
            Hotel hotel = Hotel.builder()
                    .name("Seed Hotel " + i)
                    .address("Address " + i)
                    .city("City " + ((i % 5) + 1))
                    .country("Country 1")
                    .description("Auto-seeded hotel #" + i)
                    .build();
            attachSeedImage(hotel, seededImageUrls, i);
            existingHotels.add(hotelRepository.save(hotel));
        }

        if (existingHotels.isEmpty()) {
            return;
        }

        long existingRoomCount = roomRepository.count();
        for (int i = (int) existingRoomCount + 1; i <= roomTarget; i++) {
            Hotel hotel = existingHotels.get((i - 1) % existingHotels.size());

            Room room = new Room();
            room.setRoomType((i % 3 == 0) ? "SUITE" : (i % 2 == 0) ? "DOUBLE" : "SINGLE");
            room.setCapacity((i % 3 == 0) ? 4 : (i % 2 == 0) ? 2 : 1);
            room.setBasePrice(BigDecimal.valueOf(80 + (i * 10L)));
            room.setHotel(hotel);

            roomRepository.save(room);
        }
    }

    private List<String> loadSeedUploadUrls() {
        Path uploadsPath = Path.of("uploads");
        if (!Files.isDirectory(uploadsPath)) {
            return List.of();
        }

        try (var stream = Files.list(uploadsPath)) {
            return stream
                    .filter(Files::isRegularFile)
                    .map(path -> "/uploads/" + path.getFileName())
                    .sorted()
                    .toList();
        } catch (IOException ex) {
            return List.of();
        }
    }

    private void attachSeedImage(Hotel hotel, List<String> seededImageUrls, int seedIndex) {
        if (seededImageUrls == null || seededImageUrls.isEmpty()) {
            return;
        }

        String imageUrl = seededImageUrls.get(Math.floorMod(seedIndex, seededImageUrls.size()));
        HotelImage hotelImage = new HotelImage();
        hotelImage.setUrl(imageUrl);
        hotelImage.setIsPrimary(true);
        hotelImage.setHotel(hotel);
        hotel.getImages().add(hotelImage);
    }
}
