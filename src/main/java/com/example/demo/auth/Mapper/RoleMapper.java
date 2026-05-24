package com.example.demo.auth.Mapper;
import java.util.LinkedHashSet;
import java.util.List;
import com.example.demo.auth.UserDTOs.RoleResponseDTO;
import java.util.Set;

import com.example.demo.auth.entity.Permission;
import com.example.demo.auth.entity.Role;
import com.example.demo.auth.UserDTOs.UserResponseDTO;
import com.example.demo.auth.entity.User;
import java.util.stream.Collectors;




public class RoleMapper {

    public  static RoleResponseDTO toRoleResponseDTO(Role role) {
        List<String> permissionNames = role.getPermissions().stream()
                .map(Permission::getName)
                .toList();
        return new RoleResponseDTO(role.getId(), role.getName(), permissionNames);
    }
        
    public static  UserResponseDTO toUserResponseDTO(User user) {
        List<Long> hotelIds = user.getUserHotels().stream()
                .map(uh -> uh.getId())
                .toList();
       Set<String> roleNames = user.getRoles().stream()
        .map(Role::getName)
        .sorted()
        .collect(Collectors.toCollection(LinkedHashSet::new));


        return new UserResponseDTO(user.getId(), user.getUsername(), roleNames, hotelIds);
    }
    
}
