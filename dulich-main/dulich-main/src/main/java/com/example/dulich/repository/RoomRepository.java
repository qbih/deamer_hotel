package com.example.dulich.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.dulich.entity.Room;

@Repository
public interface RoomRepository extends JpaRepository<Room, Long> {
    
    // Tìm phòng theo khách sạn
    List<Room> findByHotelId(Long hotelId);
    
    // Tìm phòng theo khách sạn với phân trang
    Page<Room> findByHotelId(Long hotelId, Pageable pageable);
    
    // Tìm phòng theo loại phòng
    List<Room> findByRoomTypeId(Long roomTypeId);
    
    // Tìm phòng theo trạng thái
    List<Room> findByStatus(Integer status);
    
    // Tìm phòng có sẵn theo khách sạn
    List<Room> findByHotelIdAndStatus(Long hotelId, Integer status);
    
    // Tìm phòng theo số phòng
    Optional<Room> findByRoomNumberAndHotelId(String roomNumber, Long hotelId);
    
    // Tìm phòng theo loại phòng và khách sạn
    List<Room> findByHotelIdAndRoomTypeId(Long hotelId, Long roomTypeId);
    
    // Tìm phòng có sẵn theo loại phòng và khách sạn
    List<Room> findByHotelIdAndRoomTypeIdAndStatus(Long hotelId, Long roomTypeId, Integer status);
    
    // Đếm số phòng theo khách sạn
    long countByHotelId(Long hotelId);
    
    // Đếm số phòng có sẵn theo khách sạn
    long countByHotelIdAndStatus(Long hotelId, Integer status);
    
    // Tìm phòng theo khoảng giá
    @Query("SELECT r FROM Room r WHERE r.pricePerNight BETWEEN :minPrice AND :maxPrice")
    List<Room> findByPriceRange(@Param("minPrice") Double minPrice, @Param("maxPrice") Double maxPrice);
    
    // Tìm phòng có sẵn với capacity tối thiểu
    @Query("SELECT r FROM Room r WHERE r.capacity >= :minCapacity AND r.status = 1")
    List<Room> findAvailableRoomsWithMinCapacity(@Param("minCapacity") Integer minCapacity);
    
    // Tìm phòng theo amenities
    @Query("SELECT r FROM Room r WHERE r.amenities LIKE %:amenity%")
    List<Room> findByAmenity(@Param("amenity") String amenity);
    
    // Tìm phòng có view biển
    List<Room> findByHasSeaViewTrue();
    
    // Tìm phòng có ban công
    List<Room> findByHasBalconyTrue();
    
    // Tìm phòng không hút thuốc
    List<Room> findByIsSmokingAllowedFalse();
    
    // Query to check room statuses for debugging
    @Query("SELECT COUNT(r) FROM Room r WHERE r.hotel.id = :hotelId AND r.status = :status")
    Long countRoomsByHotelIdAndStatus(@Param("hotelId") Long hotelId, @Param("status") Integer status);
    
    // List all rooms with their statuses for a hotel
    @Query("SELECT r FROM Room r WHERE r.hotel.id = :hotelId")
    List<Room> findAllByHotelIdWithStatus(@Param("hotelId") Long hotelId);
}
