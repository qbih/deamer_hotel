package com.example.dulich.service;

import java.util.List;
import java.util.Optional;

import com.example.dulich.entity.GroupPermission;

public interface GroupPermissionService {
    
    List<GroupPermission> findAll();
    
    Optional<GroupPermission> findById(Long id);
    
    GroupPermission save(GroupPermission groupPermission);
    
    void delete(Long id);
}
