package com.example.demo.catalog.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.catalog.entity.Hotel;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.List;

public interface HotelRepository extends JpaRepository<Hotel, Long>, JpaSpecificationExecutor<Hotel> {

    Optional<Hotel> findByIdAndIsActiveTrue(Long id);

    boolean existsByIdAndIsActiveTrue(Long id);

    @Query("select distinct h from Hotel h left join fetch h.images")
    List<Hotel> findAllWithImages();

}
