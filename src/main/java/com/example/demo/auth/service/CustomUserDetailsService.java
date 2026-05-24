package com.example.demo.auth.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.example.demo.auth.entity.User;

import com.example.demo.auth.repository.UserRepository;
import org.springframework.stereotype.Service;
import lombok.*;

@RequiredArgsConstructor
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
// why when username is not found, tell me authorization failed instead of user not found? because the exception is thrown in the authentication process, and the authentication manager catches it and returns a generic authentication failed message to avoid leaking information about which usernames exist in the system. This is a security best practice 
//to prevent attackers from enumerating valid usernames.
        List<SimpleGrantedAuthority> authorities = new ArrayList<>();

// System roles + permissions
        user.getRoles().forEach(role -> {

            // ROLE_
            authorities.add(new SimpleGrantedAuthority("ROLE_" + role.getName()));

            // Permissions
            role.getPermissions().forEach(permission -> {
                authorities.add(new SimpleGrantedAuthority(permission.getName()));
            });
        });

// Hotel roles + permissions
        user.getUserHotels().forEach(userHotel -> {

            userHotel.getHotelRole().getPermissions().forEach(permission -> {
                authorities.add(new SimpleGrantedAuthority(permission.getName()));
            });

            // Optional: if you treat hotel role as authority
            authorities.add(new SimpleGrantedAuthority(
                    "ROLE_" + userHotel.getHotelRole().getName()
            ));
        });

        return new CustomUserDetails(
                user.getId(),
                user.getUsername(),
                user.getPassword(),
                authorities);
    }

    @Getter
    @RequiredArgsConstructor
    public static class CustomUserDetails implements UserDetails {

        private final Long id;
        private final String username;
        private final String password;
        private final Collection<? extends GrantedAuthority> authorities;

        @Override
        public Collection<? extends GrantedAuthority> getAuthorities() {
            return authorities;
        }

        @Override
        public String getPassword() {
            return password;
        }

        @Override
        public String getUsername() {
            return username;
        }

        @Override
        public boolean isAccountNonExpired() {
            return true;
        }

        @Override
        public boolean isAccountNonLocked() {
            return true;
        }

        @Override
        public boolean isCredentialsNonExpired() {
            return true;
        }

        @Override
        public boolean isEnabled() {
            return true;
        }
    }
}
