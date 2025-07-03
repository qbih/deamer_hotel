package com.example.dulich.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.dulich.entity.RoomType;

public interface RoomTypeService {
    
    List<RoomType> findAll();
    
    Page<RoomType> findAllPaged(Pageable pageable);
    
    Optional<RoomType> findById(Long id);
    
    List<RoomType> findByStatus(Integer status);
    
    List<RoomType> findByNameContaining(String name);
    
    List<RoomType> findByBasePriceRange(Double minPrice, Double maxPrice);
    
    List<RoomType> findByMaxCapacityGreaterThanEqual(Integer minCapacity);
    
    List<RoomType> findByBedType(String bedType);
    
    RoomType save(RoomType roomType);
    
    RoomType update(RoomType roomType);
    
    void delete(Long id);
    
    long countByStatus(Integer status);
}
