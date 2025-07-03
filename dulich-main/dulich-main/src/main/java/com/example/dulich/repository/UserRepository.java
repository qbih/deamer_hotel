package com.example.dulich.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.dulich.entity.Role;
import com.example.dulich.entity.User;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    
    Optional<User> findByUsername(String username);
    
    Optional<User> findByEmail(String email);
    
    boolean existsByUsername(String username);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRoles(@Param("username") String username);
    
    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsernameWithRolesLeftJoin(@Param("username") String username);
    
    @Query("SELECT u FROM User u JOIN FETCH u.roles WHERE u.id = :id")
    Optional<User> findByIdWithRoles(@Param("id") Long id);
    
    @Query("SELECT u FROM User u JOIN FETCH u.roles")
    java.util.List<User> findAllWithRoles();
    
    // Find users by role
    List<User> findByRolesContaining(Role role);
    
    // Find users by role with pagination
    Page<User> findByRolesContaining(Role role, Pageable pageable);
    
    // Find staff with filters
    @Query("SELECT DISTINCT u FROM User u JOIN u.roles r WHERE r.id = :roleId " +
           "AND (:name IS NULL OR LOWER(u.fullName) LIKE LOWER(CONCAT('%', :name, '%'))) " +
           "AND (:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))) " +
           "AND (:phone IS NULL OR u.phone LIKE CONCAT('%', :phone, '%'))")
    Page<User> findStaffWithFilters(@Param("name") String name, 
                                   @Param("email") String email, 
                                   @Param("phone") String phone, 
                                   @Param("roleId") Long roleId, 
                                   Pageable pageable);
}
