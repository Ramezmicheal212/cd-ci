package com.example.demo.auth.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;   

import lombok.RequiredArgsConstructor;

import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import com.example.demo.auth.service.JwtTokenService;
import com.example.demo.auth.repository.UserRepository;
import com.example.demo.auth.repository.RoleRepository;
import com.example.demo.auth.entity.User;
import com.example.demo.auth.entity.Role;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.transaction.annotation.Transactional;
import java.util.Set;
import java.util.UUID;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class CustomOAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenService jwtService;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void onAuthenticationSuccess(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) throws IOException {

        OAuth2User user = (OAuth2User) authentication.getPrincipal();

        String email = user.getAttribute("email");
        String name = user.getAttribute("name");
        // image profile
        String picture = user.getAttribute("picture");

        // 1️⃣ check if user exists
        User appUser = userRepository.findByEmail(email)
                .orElseGet(() -> {

                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setFullName(name);
                    newUser.setUsername(email.split("@")[0]);

                    // dummy password (not used)
                    newUser.setPassword(
                            passwordEncoder.encode(UUID.randomUUID().toString())
                    );

                    // default role
                    Role userRole = roleRepository.findByName("USER")
                            .orElseThrow(() -> new RuntimeException("Default role USER not found"));

                    newUser.setRoles(Set.of(userRole));

                    return userRepository.save(newUser);
                });

        // 2️⃣ generate JWT
        String token = jwtService.generateToken(appUser.getEmail());

        // 3️⃣ redirect to frontend
        String redirectUrl
                = "http://localhost:5173/oauth-success"
                + "?token=" + token
                + "&username=" + appUser.getUsername()
                + "&authorities=" + String.join(",", appUser.getRoles().stream()
                        .map(Role::getName)
                        .toList());
                

        response.sendRedirect(redirectUrl);
    }
}
