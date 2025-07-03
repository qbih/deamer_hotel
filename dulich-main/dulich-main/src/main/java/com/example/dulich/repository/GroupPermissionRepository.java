package com.example.dulich.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.dulich.entity.GroupPermission;

@Repository
public interface GroupPermissionRepository extends JpaRepository<GroupPermission, Long> {
    
    Optional<GroupPermission> findByName(String name);
    
    List<GroupPermission> findByNameContaining(String name);
    
    boolean existsByName(String name);
}
