package com.example.demo.catalog.Specification;

import org.springframework.data.jpa.domain.Specification;
import com.example.demo.catalog.entity.Hotel;

public class HotelSpec {

    public static Specification<Hotel> isActive() {
        return (root, query, criteriaBuilder) -> criteriaBuilder.isTrue(root.get("isActive"));
    }

    public static Specification<Hotel> hasName(String name) {

        return (root, query, criteriaBuilder) -> {
            if (name == null || name.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("name"), "%" + name + "%");
        };
    }

    public static Specification<Hotel> hasCity(String city) {

        return (root, query, criteriaBuilder) -> {
            if (city == null || city.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("city"), "%" + city + "%");
        };
    }

    public static Specification<Hotel> hasCountry(String country) {

        return (root, query, criteriaBuilder) -> {
            if (country == null || country.isEmpty()) {
                return criteriaBuilder.conjunction();
            }
            return criteriaBuilder.like(root.get("country"), "%" + country + "%");
        };
    }

}
