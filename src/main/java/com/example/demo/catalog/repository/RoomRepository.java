package com.example.demo.catalog.repository;

import com.example.demo.catalog.entity.Room;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface RoomRepository extends JpaRepository<Room, Long>, JpaSpecificationExecutor<Room> {
    List<Room> findByHotelId(Long hotelId);

    List<Room> findByHotelIdAndIsActiveTrue(Long hotelId);

    Optional<Room> findByIdAndIsActiveTrue(Long id);

    boolean existsByHotelIdAndIsActiveTrue(Long hotelId);
    @Modifying
@Query("UPDATE Room r SET r.isActive = false WHERE r.hotel.id = :hotelId AND r.isActive = true")
void deactivateRoomsByHotelId(Long hotelId);

@Modifying
@Query("UPDATE Room r SET r.isActive = true WHERE r.hotel.id = :hotelId")
void activateRoomsByHotelId(Long hotelId);
}