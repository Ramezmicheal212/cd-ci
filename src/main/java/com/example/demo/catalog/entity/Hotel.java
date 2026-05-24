package com.example.demo.catalog.entity;

import java.util.LinkedHashSet;
import java.util.HashSet;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;
import com.example.demo.auth.entity.UserHotel;
import jakarta.persistence.CascadeType;

@Entity
@Table(name = "hotels")
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@Setter
public class Hotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private String address;
    @Column(nullable = false)
    private String city;
    @Column(nullable = false)
    private String country;
    @Column(length = 1000)
    private String description;
    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<HotelImage> images = new LinkedHashSet<>();

    @Column(nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "hotel")
    @Builder.Default
    private Set<Room> rooms = new HashSet<>();

    @OneToMany(mappedBy = "hotel")
    @Builder.Default
    private Set<UserHotel> users = new HashSet<>();

}
