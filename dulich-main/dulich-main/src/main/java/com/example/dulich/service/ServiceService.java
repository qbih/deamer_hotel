package com.example.dulich.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.dulich.entity.Service;

public interface ServiceService {
    
    List<Service> findAll();
    
    Page<Service> findAllPaged(Pageable pageable);
    
    Optional<Service> findById(Long id);
    
    List<Service> findByStatus(Integer status);
    
    Page<Service> findByStatus(Integer status, Pageable pageable);
    
    List<Service> findByCategory(String category);
    
    List<Service> findByCategoryAndStatus(String category, Integer status);
    
    List<Service> findIncludedServices();
    
    List<Service> findPaidServices();
    
    List<Service> findAvailable24hServices();
    
    List<Service> findByPriceRange(Double minPrice, Double maxPrice);
    
    List<String> findDistinctCategories();
    
    Service save(Service service);
    
    Service update(Service service);
    
    void delete(Long id);
    
    long countByStatus(Integer status);
}
