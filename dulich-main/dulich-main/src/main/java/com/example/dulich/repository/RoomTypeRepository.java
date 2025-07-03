package com.example.dulich.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.dulich.entity.RoomType;

@Repository
public interface RoomTypeRepository extends JpaRepository<RoomType, Long> {
    
    // Tìm loại phòng theo trạng thái
    List<RoomType> findByStatus(Integer status);
    
    // Tìm loại phòng hoạt động
    List<RoomType> findByStatusOrderByName(Integer status);
    
    // Tìm theo tên
    List<RoomType> findByNameContainingIgnoreCase(String name);
    
    // Tìm theo khoảng giá
    @Query("SELECT rt FROM RoomType rt WHERE rt.basePrice BETWEEN :minPrice AND :maxPrice")
    List<RoomType> findByBasePriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);
    
    // Tìm theo capacity tối thiểu
    List<RoomType> findByMaxCapacityGreaterThanEqual(Integer minCapacity);
    
    // Tìm theo loại giường
    List<RoomType> findByBedType(String bedType);
    
    // Đếm số loại phòng hoạt động
    long countByStatus(Integer status);
    
    // Tìm loại phòng có phòng trong khách sạn cụ thể
    @Query("SELECT DISTINCT rt FROM RoomType rt " +
           "JOIN Room r ON r.roomType.id = rt.id " + 
           "WHERE r.hotel.id = :hotelId AND r.status = 1 AND rt.status = 1")
    List<RoomType> findByHotelId(@Param("hotelId") Long hotelId);
}
