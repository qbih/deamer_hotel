package com.example.dulich.service.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dulich.dto.CategoryDto;
import com.example.dulich.entity.Category;
import com.example.dulich.repository.CategoryRepository;
import com.example.dulich.service.CategoryService;
import com.example.dulich.utils.SlugUtil;

@Service
@Transactional
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    // Status constant like Laravel's CategoryController
    private static final Map<Integer, String> STATUS = new HashMap<Integer, String>() {{
        put(1, "Kích hoạt");
        put(0, "Ẩn");
    }};

    @Override
    @Transactional(readOnly = true)
    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Category> findAllPaged(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Category> findByActive(Integer active) {
        return categoryRepository.findByActive(active);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Category> findById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Category findBySlug(String slug) {
        return categoryRepository.findBySlug(slug);
    }

    @Override
    public Category createOrUpdate(CategoryDto categoryDto) {
        return createOrUpdate(categoryDto, null);
    }

    @Override
    public Category createOrUpdate(CategoryDto categoryDto, Long id) {
        Category category;
        
        if (id != null) {
            // Update case
            category = categoryRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại"));
        } else {
            // Create case
            category = new Category();
        }
        
        // Set properties from DTO
        category.setName(categoryDto.getName());
        category.setDescription(categoryDto.getDescription());
        category.setActive(categoryDto.getActive());
        category.setHome(categoryDto.getHome());
        category.setIcon(categoryDto.getIcon());
        category.setTitleSeo(categoryDto.getTitleSeo());
        category.setKeywordSeo(categoryDto.getKeywordSeo());
        
        // Generate slug if it's a new category
        if (category.getSlug() == null) {
            category.setSlug(SlugUtil.makeSlug(categoryDto.getName()));
        }
        
        // Save to database
        return categoryRepository.save(category);
    }

    @Override
    public void delete(Long id) {
        categoryRepository.deleteById(id);
    }

    @Override
    public CategoryDto convertToDto(Category category) {
        CategoryDto dto = new CategoryDto();
        
        dto.setId(category.getId());
        dto.setName(category.getName());
        dto.setDescription(category.getDescription());
        dto.setActive(category.getActive());
        dto.setHome(category.getHome());
        dto.setIcon(category.getIcon());
        dto.setTitleSeo(category.getTitleSeo());
        dto.setKeywordSeo(category.getKeywordSeo());
        
        return dto;
    }

    @Override
    public Category convertToEntity(CategoryDto categoryDto) {
        Category category = new Category();
        
        if (categoryDto.getId() != null) {
            category.setId(categoryDto.getId());
        }
        
        category.setName(categoryDto.getName());
        category.setDescription(categoryDto.getDescription());
        category.setActive(categoryDto.getActive());
        category.setHome(categoryDto.getHome());
        category.setIcon(categoryDto.getIcon());
        category.setTitleSeo(categoryDto.getTitleSeo());
        category.setKeywordSeo(categoryDto.getKeywordSeo());
        
        return category;
    }

    @Override
    public Map<Integer, String> getStatusList() {
        return Collections.unmodifiableMap(STATUS);
    }

    @Override
    public List<Category> getParentCategories() {
        // Return all active categories as potential parents
        return categoryRepository.findByActive(1);
    }
}
