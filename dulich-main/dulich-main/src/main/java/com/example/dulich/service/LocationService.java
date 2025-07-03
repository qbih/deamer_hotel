package com.example.dulich.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.dulich.entity.Location;

public interface LocationService {
    
    List<Location> findAll();
    
    List<Location> findAllActiveLocations();
    
    Optional<Location> findById(Long id);
    
    Location save(Location location);
    
    void delete(Long id);
    
    // Additional methods for admin management
    Page<Location> findPaginated(Pageable pageable);
    
    Page<Location> searchByName(String name, Pageable pageable);
    
    Optional<Location> findBySlug(String slug);
}
