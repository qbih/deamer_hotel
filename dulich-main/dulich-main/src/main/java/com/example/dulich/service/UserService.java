package com.example.dulich.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.multipart.MultipartFile;

import com.example.dulich.dto.UserRegistrationDto;
import com.example.dulich.entity.Role;
import com.example.dulich.entity.User;

public interface UserService {
    
    User save(UserRegistrationDto registrationDto);
    
    User save(User user);
    
    User findByUsername(String username);
    
    User findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    List<User> findAll();
    
    Optional<User> findById(Long id);
    
    void deleteById(Long id);
    
    void delete(Long id);
    
    UserDetails getCurrentUser();
    
    User updateUser(User user);
    
    /**
     * Find users with filters
     */
    Page<User> findWithFilters(String name, String email, String phone, Long userId, Long roleId, Pageable pageable);
    
    /**
     * Upload user avatar
     */
    String uploadImage(MultipartFile file) throws Exception;
    
    /**
     * Get user's role
     */
    Role getUserRole(Long userId);
    
    /**
     * Assign role to user
     */
    void assignRoleToUser(Long userId, Long roleId);
    
    /**
     * Update user's role
     */
    void updateUserRole(Long userId, Long roleId);
    
    /**
     * Get all staff users (users with STAFF role)
     */
    List<User> getAllStaff();
    
    /**
     * Get staff users with pagination
     */
    Page<User> getStaffWithFilters(String name, String email, String phone, Pageable pageable);
    
    /**
     * Check if user is staff
     */
    boolean isStaff(User user);
    
    /**
     * Promote user to staff
     */
    void promoteToStaff(Long userId);
    
    /**
     * Remove staff role from user
     */
    void removeStaffRole(Long userId);
    
    /**
     * Find customers (users with ROLE_USER)
     */
    Page<User> findCustomers(Pageable pageable);
}
