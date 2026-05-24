package com.example.demo.auth.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

import com.example.demo.catalog.entity.Guest;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, unique = true)
    private String username;
    @Column(nullable = false)
    private String password;
    @Column(nullable = false)
    private String fullName;
    @Column(nullable = false, unique = true)
    private String email;
    @Column(length = 15)
    private String phoneNumber;
    @Column(nullable = false)
    private boolean enabled = true;
    @ManyToMany(fetch = FetchType.LAZY)
@JoinTable(
        name = "user_roles",
        joinColumns = @JoinColumn(name = "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
)
@Builder.Default
private Set<Role> roles = new HashSet<>();

    // relation between user and hotel to anyone related to hotel like manager or staff
    @OneToMany(mappedBy = "user")
    @Builder.Default
    private Set<UserHotel> userHotels = new HashSet<>();


    @OneToMany(mappedBy = "user")
    @Builder.Default
    private Set<Guest> guests = new HashSet<>();


    public void addRole(Role role) {
        this.roles.add(role);
    }
    public void removeRole(Role role) {
        this.roles.remove(role);
    }

}
