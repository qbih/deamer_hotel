package com.example.dulich.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.dulich.entity.Service;

@Repository
public interface ServiceRepository extends JpaRepository<Service, Long> {
    
    // Tìm dịch vụ theo trạng thái
    List<Service> findByStatus(Integer status);
    
    // Tìm dịch vụ hoạt động sắp xếp theo thứ tự
    List<Service> findByStatusOrderBySortOrderAsc(Integer status);
    
    // Tìm dịch vụ theo category
    List<Service> findByCategory(String category);
    
    // Tìm dịch vụ theo category và trạng thái
    List<Service> findByCategoryAndStatus(String category, Integer status);
    
    // Tìm dịch vụ miễn phí
    List<Service> findByIsIncludedTrue();
    
    // Tìm dịch vụ tính phí
    List<Service> findByIsIncludedFalse();
    
    // Tìm dịch vụ 24/7
    List<Service> findByIsAvailable24hTrue();
    
    // Tìm theo tên
    List<Service> findByNameContainingIgnoreCase(String name);    // Tìm dịch vụ theo khách sạn - using native query approach since we removed bidirectional relationship
    @Query(value = "SELECT s.* FROM services s JOIN hotel_services hs ON s.id = hs.service_id WHERE hs.hotel_id = :hotelId AND s.status = 1", nativeQuery = true)
    List<Service> findByHotelId(@Param("hotelId") Long hotelId);
    
    // Tìm dịch vụ theo khoảng giá
    @Query("SELECT s FROM Service s WHERE s.price BETWEEN :minPrice AND :maxPrice")
    List<Service> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);
    
    // Đếm số dịch vụ hoạt động
    long countByStatus(Integer status);
    
    // Đếm số dịch vụ miễn phí
    long countByIsIncludedTrue();
}
