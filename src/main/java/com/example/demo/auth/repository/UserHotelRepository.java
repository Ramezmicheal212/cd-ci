package com.example.demo.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.Modifying;

import com.example.demo.auth.entity.*;
import java.util.Optional;
import java.util.List;

public interface UserHotelRepository extends JpaRepository<UserHotel, Long> {

    boolean existsByUserIdAndHotelId(Long userId, Long hotelId);

    Optional<UserHotel> findByUserIdAndHotelId(Long userId, Long hotelId);

    void deleteByUserId(Long userId);

    boolean existsByUserUsernameAndHotelIdAndHotelRolePermissionsNameAndStatus(String username, Long hotelId, String permissionName, EmploymentStatus status);

    Optional<List<UserHotel>> findAllByUserId(Long userId);

    List<UserHotel> findByHotelId(Long hotelId);

    void deleteByHotelId(Long hotelId);

    boolean existsByHotelRoleId(Long hotelRoleId);

    UserHotel findByUserIdAndHotelIdAndStatus(Long userId, Long hotelId, EmploymentStatus status);

    @Modifying
    @Query("UPDATE UserHotel u SET u.status = :status WHERE u.hotel.id = :hotelId")
    void updateStatusByHotelId(Long hotelId, EmploymentStatus status);
}
