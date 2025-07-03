package com.example.dulich.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.dulich.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Tìm category theo slug
    Category findBySlug(String slug);
    
    // Tìm category theo active status
    List<Category> findByActive(Integer active);
    
    // Tìm category theo home status (hiển thị trang chủ)
    List<Category> findByHome(Integer home);
    
    // Tìm kiếm category theo tên
    List<Category> findByNameContaining(String name);
    
    // Phân trang category theo active
    Page<Category> findByActive(Integer active, Pageable pageable);
    
    // Kiểm tra slug đã tồn tại chưa
    boolean existsBySlug(String slug);
    
    // Tìm category theo userId
    List<Category> findByUserId(Long userId);
    
    // Tìm category hiển thị trang chủ và đang active
    List<Category> findByHomeAndActive(Integer home, Integer active);
}
