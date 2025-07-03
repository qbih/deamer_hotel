package com.example.dulich.service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.dulich.dto.CategoryDto;
import com.example.dulich.entity.Category;

public interface CategoryService {
    
    List<Category> findAll();
    
    Page<Category> findAllPaged(Pageable pageable);
    
    List<Category> findByActive(Integer active);
    
    Optional<Category> findById(Long id);
    
    Category findBySlug(String slug);
    
    Category createOrUpdate(CategoryDto categoryDto);
    
    Category createOrUpdate(CategoryDto categoryDto, Long id);
    
    void delete(Long id);
    
    CategoryDto convertToDto(Category category);
    
    Category convertToEntity(CategoryDto categoryDto);
    
    Map<Integer, String> getStatusList();
    
    List<Category> getParentCategories();
}
