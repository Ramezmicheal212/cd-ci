package com.example.demo.catalog.repository;

import com.example.demo.catalog.entity.Amenity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AmenityRepository extends JpaRepository<Amenity, Long> {

}
