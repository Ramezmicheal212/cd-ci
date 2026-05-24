package com.example.demo.auth.service;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.ExceptionHandler.IncorrectCredentialsException;
import com.example.demo.ExceptionHandler.RoleNotFoundException;
import com.example.demo.ExceptionHandler.UserAlreadyTakenException;
import com.example.demo.ExceptionHandler.*;
import com.example.demo.auth.UserDTOs.LoginResponse;
import com.example.demo.auth.UserDTOs.UserRequestDTO;
import com.example.demo.auth.entity.Permission;
import com.example.demo.auth.entity.RefreshToken;
import com.example.demo.auth.entity.Role;
import com.example.demo.auth.entity.User;
import com.example.demo.auth.repository.RefreshTokenRepository;
import com.example.demo.auth.repository.RoleRepository;
import com.example.demo.auth.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {
private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepo;
    @Value("${security.jwt.refresh-token-days:7}")
    private long refreshTokenDays;
    @Transactional
    @Override
    public LoginResponse login(String username, String password) {
       
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IncorrectCredentialsException());
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IncorrectCredentialsException();
        }

        if (!user.isEnabled()) {
            throw new RuntimeException("User account is disabled");
        }

        Set<String> authorities = getUserRolesAndPermissions(user);
     // all refresh tokens must be revoked on login to prevent reuse of old tokens
         refreshTokenRepo.revokeAllByUserId(user.getId());
        String accessToken = tokenService.generateAccessToken(user.getUsername(), authorities, user.getId());
        String refreshToken = createRefreshToken(user);
        return new LoginResponse(
                accessToken,
                refreshToken,
                "Bearer",
                tokenService.getAccessTokenExpiresInSeconds(),
                user.getUsername(),
                authorities
        );
    }
    @Transactional
    @Override
    public void register(UserRequestDTO userRequestDTO) {
        String username = userRequestDTO.username();
        String password = userRequestDTO.password();
        String fullName = userRequestDTO.fullName();
        String email = userRequestDTO.email();
        String phoneNumber = userRequestDTO.phoneNumber();

        String requestedRole = "USER";

        if (userRepository.existsByUsername(username)) {
            throw new UserAlreadyTakenException("Username is already taken");
        }
            if (userRepository.existsByEmail(email)) {
                throw new EmailAlreadyTakenException("Email is already taken");
            }

        Role role = roleRepository.findByName(requestedRole)
                .orElseThrow(() -> new RoleNotFoundException(requestedRole));

        User user = User.builder()
                .username(username)
                .password(passwordEncoder.encode(password))
                .roles(Set.of(role))
                .fullName(fullName)
                .email(email)
                .phoneNumber(phoneNumber)
                .enabled(true)
                .build();

        userRepository.save(user);
    }
   @Transactional
    @Override
    public void logout(Jwt jwt) {
        String username = jwt.getSubject();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username));
       refreshTokenRepo.revokeAllByUserId(user.getId());
 
    }   



    @Transactional
    @Override
    public LoginResponse refreshToken(String refreshTokenValue) {
        RefreshToken refreshToken = refreshTokenRepo.findByToken(refreshTokenValue)
                .orElseThrow(() -> new IncorrectCredentialsException());
if (refreshToken.isRevoked()) {
    // 🚨 هذا reuse أو token مسروق
    throw new RuntimeException("Refresh token reuse detected");
}

        if (refreshToken.isExpired()) {
            refreshTokenRepo.delete(refreshToken);
            throw new RuntimeException("Refresh token has expired");
        }

        User user = refreshToken.getUser();
        if (!user.isEnabled()) {
            throw new RuntimeException("User account is disabled");
        }

       Set<String> authorities = getUserRolesAndPermissions(user);

        String accessToken = tokenService.generateAccessToken(user.getUsername(), authorities, user.getId());

        // Optionally rotate refresh token (create new one and delete old)
        String newRefreshToken = rotateRefreshToken(refreshToken);

        return new LoginResponse(
                accessToken,
                newRefreshToken,
                "Bearer",
                tokenService.getAccessTokenExpiresInSeconds(),
                user.getUsername(),
                authorities
        );

    }
  @Transactional
   
    private String createRefreshToken(User user) {
        String token = UUID.randomUUID().toString();
        Instant expiryDate = Instant.now().plusSeconds(refreshTokenDays * 24 * 60 * 60);

        RefreshToken refreshToken = new RefreshToken(token, user, expiryDate);
        refreshTokenRepo.save(refreshToken);

        return token;
    }
  @Transactional
   
    private String rotateRefreshToken(RefreshToken oldToken) {
        // Delete old refresh token
      oldToken.setRevoked(true);
refreshTokenRepo.save(oldToken);

        // Create new refresh token
        return createRefreshToken(oldToken.getUser());
    }

    @Transactional
    @Override
    public void revokeRefreshToken(String token) {
        RefreshToken refreshToken = refreshTokenRepo.findByToken(token)
                .orElseThrow(() -> new RuntimeException("Refresh token not found"));

        refreshToken.setRevoked(true);
        refreshTokenRepo.save(refreshToken);
    }

public static Set<String> getUserRolesAndPermissions(User user ) {


        Set<String> authorities = user.getRoles().stream()
                .flatMap(role -> {
                    Set<String> auths = new HashSet<>();

                    // Role authority
                    auths.add("ROLE_" + role.getName());

                    // Permissions
                    auths.addAll(
                            role.getPermissions().stream()
                                    .map(Permission::getName)
                                    .collect(Collectors.toSet())
                    );

                    return auths.stream();
                })
                .collect(Collectors.toSet());

        return authorities;
            }

    // Hotel access
    /*public String assignRoleAndHotelToUser(Long userId, HotelAccessRequest hotelAccessRequest) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        HotelRole role = hotelRoleRepository.findById(hotelAccessRequest.hotelRoleId())
                .orElseThrow(() -> new RoleNotFoundException(hotelAccessRequest.hotelRoleId()));

        

        if (hotelAccessRequest.hotelId() != null) {
            Hotel hotel = hotelRepository.findById(hotelAccessRequest.hotelId())
                    .orElseThrow(() -> new HotelNotFoundException(hotelAccessRequest.hotelId()));
            UserHotel userHotel = userhotelRepository.findByUserIdAndHotelId(userId, hotelAccessRequest.hotelId())
                   .orElseGet(() -> new UserHotel());
                userHotel.setUser(user);
                userHotel.setHotel(hotel);
                userHotel.setHotelRole(role);
                userhotelRepository.save(userHotel);


        }
        return "Role and hotel assigned to user successfully";
    }*/
}
