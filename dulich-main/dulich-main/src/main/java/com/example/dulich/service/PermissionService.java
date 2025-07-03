package com.example.dulich.service;

import java.util.List;
import java.util.Optional;

import com.example.dulich.entity.Permission;

public interface PermissionService {
    
    List<Permission> findAll();
    
    Optional<Permission> findById(Long id);
    
    Permission save(Permission permission);
    
    void delete(Long id);
}
