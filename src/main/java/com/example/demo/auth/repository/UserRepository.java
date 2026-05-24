package com.example.demo.auth.repository;
import java.util.Optional;

import com.example.demo.auth.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface UserRepository extends JpaRepository<User, Long> , JpaSpecificationExecutor<User> {
    Optional<User> findByUsername(String username);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    
    Optional<User> findByEmail(String email);

    // find user by hotel id and role name for hotel manager

    
}
