package com.example.dulich.service.impl;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.example.dulich.dto.UserRegistrationDto;
import com.example.dulich.entity.Role;
import com.example.dulich.entity.RoleName;
import com.example.dulich.entity.User;
import com.example.dulich.repository.RoleRepository;
import com.example.dulich.repository.UserRepository;
import com.example.dulich.security.UserPrincipal;
import com.example.dulich.service.UserService;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    
    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private RoleRepository roleRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
      @Override
    public User save(UserRegistrationDto registrationDto) {
        User user = new User();        user.setUsername(registrationDto.getUsername());
        user.setPassword(passwordEncoder.encode(registrationDto.getPassword()));
        user.setEmail(registrationDto.getEmail());
        user.setFullName(registrationDto.getFullName());
        user.setPhone(registrationDto.getPhone());
        user.setAddress(registrationDto.getAddress());
        user.setEnabled(true);
        
        // Gán role USER mặc định cho user đăng ký
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy role USER"));
        user.addRole(userRole);
        
        return userRepository.save(user);
    }
    
    @Override
    public User save(User user) {
        return userRepository.save(user);
    }
    
    @Override
    @Transactional(readOnly = true)
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElse(null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElse(null);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByUsername(String username) {
        return userRepository.existsByUsername(username);
    }
    
    @Override
    @Transactional(readOnly = true)
    public boolean existsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<User> findAll() {
        return userRepository.findAll();
    }
      @Override
    @Transactional(readOnly = true)
    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }
    
    @Override
    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public UserDetails getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof UserPrincipal) {
            return (UserPrincipal) authentication.getPrincipal();
        }
        return null;
    }    @Override
    public User updateUser(User user) {
        return userRepository.save(user);
    }
    
    @Override
    public void delete(Long id) {
        userRepository.deleteById(id);
    }
      @Override
    @Transactional(readOnly = true)
    public Page<User> findWithFilters(String name, String email, String phone, Long userId, Long roleId, Pageable pageable) {
        // For now, we'll just return all users paginated since custom repository methods need to be implemented
        // In a real implementation, you would create a specification or custom query
        return userRepository.findAll(pageable);
        
        // TODO: Implement the following in UserRepository:
        // return userRepository.findWithFilters(name, email, phone, userId, roleId, pageable);
    }
    
    private final String UPLOAD_DIR = "src/main/resources/static/images/users/";
    
    @Override
    public String uploadImage(MultipartFile file) throws Exception {
        try {
            // Create upload directory if it doesn't exist
            Path uploadPath = Paths.get(UPLOAD_DIR);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }
            
            // Generate unique filename
            String filename = UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            Path filePath = uploadPath.resolve(filename);
            
            // Save file
            Files.copy(file.getInputStream(), filePath);
            
            return filename;
        } catch (IOException e) {
            throw new Exception("Failed to upload image: " + e.getMessage());
        }
    }
    
    @Override
    @Transactional(readOnly = true)
    public Role getUserRole(Long userId) {
        // This is a stub implementation - you'll need to create the actual implementation
        // For now, we'll find the user and return the first role
        User user = userRepository.findById(userId).orElse(null);
        if (user != null && user.getRoles() != null && !user.getRoles().isEmpty()) {
            return user.getRoles().iterator().next();
        }
        return null;
    }
    
    @Override
    @Transactional
    public void assignRoleToUser(Long userId, Long roleId) {
        // Find user and role
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + userId));
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid role ID: " + roleId));
        
        // Add role to user
        user.addRole(role);
        userRepository.save(user);
    }
      @Override
    @Transactional
    public void updateUserRole(Long userId, Long roleId) {
        // Find user and role
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + userId));
        
        Role role = roleRepository.findById(roleId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid role ID: " + roleId));
        
        // Clear existing roles and add the new one
        user.getRoles().clear();
        user.addRole(role);
        userRepository.save(user);
    }
    
    @Override
    public List<User> getAllStaff() {
        Role staffRole = roleRepository.findByName(RoleName.STAFF)
                .orElseThrow(() -> new RuntimeException("STAFF role not found"));
        return userRepository.findByRolesContaining(staffRole);
    }
    
    @Override
    public Page<User> getStaffWithFilters(String name, String email, String phone, Pageable pageable) {
        Role staffRole = roleRepository.findByName(RoleName.STAFF)
                .orElseThrow(() -> new RuntimeException("STAFF role not found"));
        return userRepository.findStaffWithFilters(name, email, phone, staffRole.getId(), pageable);
    }
    
    @Override
    public boolean isStaff(User user) {
        return user.getRoles().stream()
                .anyMatch(role -> role.getName() == RoleName.STAFF);
    }
    
    @Override
    @Transactional
    public void promoteToStaff(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + userId));
        
        Role staffRole = roleRepository.findByName(RoleName.STAFF)
                .orElseThrow(() -> new RuntimeException("STAFF role not found"));
        
        // Remove USER role if exists and add STAFF role
        user.getRoles().removeIf(role -> role.getName() == RoleName.USER);
        user.addRole(staffRole);
        userRepository.save(user);
    }
    
    @Override
    @Transactional
    public void removeStaffRole(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user ID: " + userId));
        
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new RuntimeException("USER role not found"));
        
        // Remove STAFF role and add USER role
        user.getRoles().removeIf(role -> role.getName() == RoleName.STAFF);
        user.addRole(userRole);
        userRepository.save(user);
    }

    @Override
    public Page<User> findCustomers(Pageable pageable) {
        Role userRole = roleRepository.findByName(RoleName.USER)
                .orElseThrow(() -> new RuntimeException("USER role not found"));
        return userRepository.findByRolesContaining(userRole, pageable);
    }
}
