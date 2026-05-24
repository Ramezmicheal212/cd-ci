package com.example.demo.auth.service;


import org.springframework.security.oauth2.jwt.Jwt;
import java.util.List;

import com.example.demo.PagedResponse;
import com.example.demo.auth.UserDTOs.*;
import org.springframework.data.domain.Pageable;


public interface UserService {

    public ResponseDTO getMyUser(Jwt jwt);
    public PagedResponse<UserResponseDTO> getAllUsers(Pageable pageable, UserFilter filter);
    public UserResponseDTO getUserById(Long id);
    public void deleteUser(Long id);
    public void assignRoleToUser(Long userId, AssignRoleRequestDTO request) ;
    public void removeRoleFromUser(Long userId, RemoveRoleRequestDTO request) ;
    public void updateUserPassword(UpdatePasswordDTO dto, Jwt jwt) ;
    public UpdateUsernameResponseDTO updateUsername(UpdateUsernameDTO dto, Jwt jwt) ;
}
