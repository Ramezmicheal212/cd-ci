package com.example.demo.auth.entity;
import java.util.HashSet;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.util.Set;
import com.example.demo.auth.entity.HotelPermission;


@Entity
@Table(name = "hotel_roles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HotelRole {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(unique = true)
    private String name;
      @Builder.Default
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "hotel_role_permissions",
            joinColumns = @JoinColumn(name = "hotel_role_id"),
            inverseJoinColumns = @JoinColumn(name = "permission_id")
    )
    private Set<HotelPermission> permissions = new HashSet<>();

    public void addPermission(HotelPermission permission) {
        this.permissions.add(permission);
        permission.getHotelRoles().add(this);
    }

    
    


}

