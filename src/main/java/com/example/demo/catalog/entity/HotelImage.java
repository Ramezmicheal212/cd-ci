package com.example.demo.catalog.entity;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.*;


@Entity
@Table(name = "hotel_images")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HotelImage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String url;
    private Boolean isPrimary; 
     @ManyToOne
    private Hotel hotel;
    
}
