package com.example.dulich.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.dulich.entity.Hotel;

@Repository
public interface HotelRepository extends JpaRepository<Hotel, Long> {
    
    // Tìm hotel theo location id
    List<Hotel> findByLocationId(Long locationId);
    
    // Tìm hotel theo location id và status
    Page<Hotel> findByLocationIdAndStatus(Long locationId, Integer status, Pageable pageable);
    
    // Tìm hotel theo userId
    List<Hotel> findByUserId(Long userId);
      // Tìm hotel theo status
    List<Hotel> findByStatus(Integer status);
    
    // Phân trang hotel theo status
    Page<Hotel> findByStatusOrderByIdDesc(Integer status, Pageable pageable);
    
    // Tìm kiếm hotel theo tên
    List<Hotel> findByNameContaining(String name);
    
    // Tìm kiếm hotel theo tên và status có phân trang
    Page<Hotel> findByNameContainingAndStatus(String name, Integer status, Pageable pageable);
    
    // Tìm danh sách khách sạn tương tự (cùng vị trí, khác id)
    List<Hotel> findTop4ByLocationIdAndIdNotAndStatus(Long locationId, Long id, Integer status);
    
    // Phân trang hotel theo status
    Page<Hotel> findByStatus(Integer status, Pageable pageable);
    
    // Khách sạn có khuyến mãi
    List<Hotel> findBySaleGreaterThanAndStatus(Integer sale, Integer status);
    
    // Khách sạn theo khoảng giá
    @Query("SELECT h FROM Hotel h WHERE h.price BETWEEN :minPrice AND :maxPrice AND h.status = 1")
    List<Hotel> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);
    
    // Khách sạn có sẵn trong khoảng thời gian
    @Query("SELECT h FROM Hotel h WHERE h.status = 1 AND :checkIn >= h.startDate AND :checkOut <= h.endDate")
    List<Hotel> findAvailableHotels(@Param("checkIn") LocalDateTime checkIn, @Param("checkOut") LocalDateTime checkOut);
    
    // Khách sạn theo số lượng khách
    @Query("SELECT h FROM Hotel h WHERE h.status = 1 AND h.numberPeople >= :numberOfGuests")
    List<Hotel> findByNumberOfGuests(@Param("numberOfGuests") Integer numberOfGuests);
    
    // Tìm kiếm với filters
    @Query("SELECT h FROM Hotel h WHERE " +
           "(:name IS NULL OR LOWER(h.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:locationId IS NULL OR h.location.id = :locationId) AND " +
           "(:minPrice IS NULL OR h.price >= :minPrice) AND " +
           "(:maxPrice IS NULL OR h.price <= :maxPrice) AND " +
           "(:status IS NULL OR h.status = :status)")
    Page<Hotel> findWithFilters(@Param("name") String name,
                               @Param("locationId") Long locationId,
                               @Param("minPrice") Double minPrice,
                               @Param("maxPrice") Double maxPrice,
                               @Param("status") Integer status,
                               Pageable pageable);
    
    // Khách sạn mới nhất
    @Query("SELECT h FROM Hotel h WHERE h.status = 1 ORDER BY h.createdAt DESC")
    List<Hotel> findLatestHotels(Pageable pageable);
    
    // Khách sạn nổi bật (có khuyến mãi cao)
    @Query("SELECT h FROM Hotel h WHERE h.status = 1 AND h.sale > 0 ORDER BY h.sale DESC")
    List<Hotel> findFeaturedHotels(Pageable pageable);
    
    // Khách sạn gợi ý (khác hotel hiện tại)
    @Query("SELECT h FROM Hotel h WHERE h.id != :hotelId AND h.status = 1 ORDER BY h.createdAt DESC")
    List<Hotel> findRecommendedHotels(@Param("hotelId") Long hotelId, Pageable pageable);
    
    // Tìm hotel theo id với services được eager loaded
    @Query("SELECT h FROM Hotel h LEFT JOIN FETCH h.services WHERE h.id = :id")
    Optional<Hotel> findByIdWithServices(@Param("id") Long id);
      // Tìm tất cả hotels với trạng thái cụ thể và services được eager loaded
    @Query("SELECT DISTINCT h FROM Hotel h LEFT JOIN FETCH h.services WHERE h.status = :status")
    List<Hotel> findByStatusWithServices(@Param("status") Integer status);
    
    // Fetch hotels with safer approach to avoid ConcurrentModificationException
    @Query("SELECT NEW com.example.dulich.entity.Hotel(h.id, h.name, h.image, h.albumImage, h.address, h.phone, h.numberPeople, h.price, h.sale, h.description, h.content, h.status, h.startDate, h.endDate, h.createdAt, h.updatedAt) FROM Hotel h WHERE h.status = :status ORDER BY h.id DESC")
    List<Hotel> findByStatusSafe(@Param("status") Integer status);
    
    // Safer version to find related hotels
    @Query("SELECT NEW com.example.dulich.entity.Hotel(h.id, h.name, h.image, h.albumImage, h.address, h.phone, h.numberPeople, h.price, h.sale, h.description, h.content, h.status, h.startDate, h.endDate, h.createdAt, h.updatedAt) FROM Hotel h WHERE h.location.id = :locationId AND h.id != :id AND h.status = :status ORDER BY h.id DESC")
    List<Hotel> findRelatedHotelsSafe(@Param("locationId") Long locationId, @Param("id") Long id, @Param("status") Integer status, Pageable pageable);
}
