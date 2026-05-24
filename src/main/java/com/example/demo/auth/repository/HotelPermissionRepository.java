package com.example.demo.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.auth.entity.HotelPermission;
import java.util.Optional;
import java.util.Set;

public interface HotelPermissionRepository extends JpaRepository<HotelPermission, Long> {

    Optional<HotelPermission> findByName(String name);

    Set<HotelPermission> findByNameIn(Set<String> names);

}
