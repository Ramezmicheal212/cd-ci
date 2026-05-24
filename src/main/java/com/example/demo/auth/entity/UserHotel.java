package com.example.demo.auth.entity;

import com.example.demo.catalog.entity.Hotel;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(
    name = "user_hotels",
    uniqueConstraints = @UniqueConstraint(
        name = "uc_user_hotel",
        columnNames = {"user_id", "hotel_id"}
    )
)
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Getter
@Setter
public class UserHotel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    /**
     * The hotel-level job title, e.g. "MANAGER", "HR", "STAFF", "RECEPTIONIST".
     * This is independent from the system-level RBAC Role on the User entity.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hotel_role_id", nullable = false)
    private HotelRole hotelRole;
        @Enumerated(EnumType.STRING)
       private EmploymentStatus status;
}
