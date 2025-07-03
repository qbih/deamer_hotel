package com.example.dulich.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.example.dulich.entity.Location;

@Repository
public interface LocationRepository extends JpaRepository<Location, Long> {
    
    // Tìm location theo slug
    Location findBySlug(String slug);
    
    // Tìm location theo status
    List<Location> findByStatus(Integer status);
    
    // Tìm location theo userId
    List<Location> findByUserId(Long userId);
    
    // Tìm kiếm location theo tên
    List<Location> findByNameContaining(String name);
    
    // Tìm kiếm location theo tên với phân trang
    Page<Location> findByNameContaining(String name, Pageable pageable);
    
    // Phân trang location theo status
    Page<Location> findByStatus(Integer status, Pageable pageable);
    
    // Kiểm tra slug đã tồn tại chưa
    boolean existsBySlug(String slug);
      // Tìm location phổ biến (có nhiều hotel nhất)
    @Query("SELECT l FROM Location l LEFT JOIN l.hotels h WHERE l.status = 1 GROUP BY l.id ORDER BY COUNT(h) DESC")
    List<Location> findPopularLocations(Pageable pageable);
}
