package com.example.demo.auth.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.auth.entity.HotelRole;

public interface HotelRoleRepository extends JpaRepository<HotelRole, Long> {

    Optional<HotelRole> findByName(String name);

    boolean existsByName(String name);

}
