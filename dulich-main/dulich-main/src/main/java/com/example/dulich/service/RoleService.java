package com.example.dulich.service;

import java.util.List;
import java.util.Optional;

import com.example.dulich.entity.Role;
import com.example.dulich.entity.RoleName;

public interface RoleService {
    
    List<Role> findAll();
    
    Optional<Role> findById(Long id);
    
    Optional<Role> findByName(RoleName name);
    
    Role save(Role role);
    
    void delete(Long id);
}
