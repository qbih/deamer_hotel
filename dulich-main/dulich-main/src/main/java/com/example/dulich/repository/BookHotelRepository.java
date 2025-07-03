package com.example.dulich.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.dulich.entity.BookHotel;

@Repository
public interface BookHotelRepository extends JpaRepository<BookHotel, Long> {
    
    // Tìm đặt phòng theo userId
    List<BookHotel> findByUserId(Long userId);
    
    // Phân trang đặt phòng theo userId
    Page<BookHotel> findByUserId(Long userId, Pageable pageable);
    
    // Tìm đặt phòng theo hotelId
    List<BookHotel> findByHotelId(Long hotelId);
    
    // Phân trang đặt phòng theo hotelId
    Page<BookHotel> findByHotelId(Long hotelId, Pageable pageable);
    
    // Tìm đặt phòng theo status
    List<BookHotel> findByStatus(Integer status);
    
    // Phân trang đặt phòng theo status
    Page<BookHotel> findByStatus(Integer status, Pageable pageable);
    
    // Tìm đặt phòng theo email
    List<BookHotel> findByEmail(String email);
    
    // Tìm đặt phòng theo số điện thoại
    List<BookHotel> findByPhone(String phone);
    
    // Tìm đặt phòng theo ngày nhận phòng
    @Query("SELECT b FROM BookHotel b WHERE DATE(b.checkInDate) = DATE(:checkInDate)")
    List<BookHotel> findByCheckInDate(@Param("checkInDate") LocalDateTime checkInDate);
    
    // Tổng số phòng đã đặt trong tháng
    @Query("SELECT COUNT(b) FROM BookHotel b WHERE YEAR(b.createdAt) = :year AND MONTH(b.createdAt) = :month")
    long countBookingsInMonth(@Param("year") int year, @Param("month") int month);
    
    // Tổng doanh thu từ khách sạn trong tháng
    @Query("SELECT SUM(b.totalPrice) FROM BookHotel b " +
           "WHERE YEAR(b.createdAt) = :year AND MONTH(b.createdAt) = :month AND b.status = 3")
    Double sumRevenueInMonth(@Param("year") int year, @Param("month") int month);
    
    // Tìm đặt phòng theo khách sạn và trạng thái
    List<BookHotel> findByHotelIdAndStatus(Long hotelId, Integer status);
    
    // Tìm kiếm với các bộ lọc
    @Query("SELECT b FROM BookHotel b WHERE " +
           "(:hotelId IS NULL OR b.hotel.id = :hotelId) AND " +
           "(:userId IS NULL OR b.user.id = :userId) AND " +
           "(:status IS NULL OR b.status = :status) AND " +
           "(:checkInFrom IS NULL OR b.checkInDate >= :checkInFrom) AND " +
           "(:checkInTo IS NULL OR b.checkInDate <= :checkInTo)")
    Page<BookHotel> findWithFilters(@Param("hotelId") Long hotelId,
                                   @Param("userId") Long userId,
                                   @Param("status") Integer status,
                                   @Param("checkInFrom") LocalDateTime checkInFrom,
                                   @Param("checkInTo") LocalDateTime checkInTo,
                                   Pageable pageable);
    
    // Đếm số lượng theo trạng thái
    long countByStatus(Integer status);
    
    // Tính tổng doanh thu
    @Query("SELECT SUM(b.totalPrice) FROM BookHotel b WHERE b.status = 3")
    Double getTotalRevenue();
    
    // Tính tổng doanh thu theo trạng thái
    @Query("SELECT SUM(b.totalPrice) FROM BookHotel b WHERE b.status = :status")
    Double getTotalRevenueByStatus(@Param("status") Integer status);
    
    // Tính tổng doanh thu trong khoảng thời gian
    @Query("SELECT SUM(b.totalPrice) FROM BookHotel b WHERE " +
           "b.createdAt BETWEEN :startDate AND :endDate AND b.status = 3")
    Double getTotalRevenueInRange(@Param("startDate") LocalDateTime startDate,
                                 @Param("endDate") LocalDateTime endDate);
    
    // Find booking by ID with eager loading of room and room type
    @Query("SELECT b FROM BookHotel b LEFT JOIN FETCH b.room r LEFT JOIN FETCH r.roomType WHERE b.id = :id")
    BookHotel findByIdWithRoom(@Param("id") Long id);
      // Find bookings for a specific room that overlap with the given date range
    @Query("SELECT b FROM BookHotel b WHERE b.room.id = :roomId AND b.status != 4 AND " +
           "((b.checkInDate <= :checkOutDate AND b.checkOutDate >= :checkInDate))")
    List<BookHotel> findByRoomIdAndDateRange(@Param("roomId") Long roomId, 
                                           @Param("checkInDate") LocalDateTime checkInDate,
                                           @Param("checkOutDate") LocalDateTime checkOutDate);
    
    // Tìm booking theo ngày check-in trong khoảng thời gian và trạng thái
    List<BookHotel> findByCheckInDateBetweenAndStatus(LocalDateTime startDate, LocalDateTime endDate, Integer status);
    
    // Tìm booking có ngày check-out trước thời điểm cho trước và trạng thái cụ thể
    List<BookHotel> findByCheckOutDateBeforeAndStatus(LocalDateTime endDate, Integer status);
}
