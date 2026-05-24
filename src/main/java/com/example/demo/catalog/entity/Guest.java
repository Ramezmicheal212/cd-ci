package com.example.demo.catalog.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import com.example.demo.Booking.entityBooking.Booking;
import com.example.demo.auth.entity.User;

@Entity
@Table(name = "guests")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Guest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    private String phone;

    @OneToMany(mappedBy = "guest", cascade = CascadeType.ALL)
    private List<Booking> bookings;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user;
}
