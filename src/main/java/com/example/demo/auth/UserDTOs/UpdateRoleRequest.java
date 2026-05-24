package com.example.demo.auth.UserDTOs;
import java.util.List;

public record UpdateRoleRequest(
    String roleName,
     List<String> addPermissions,
    List<String> removePermissions
) {
    
}
