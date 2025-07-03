package com.example.dulich.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.dulich.entity.Room;

public interface RoomService {
    
    List<Room> findAll();
    
    Page<Room> findAllPaged(Pageable pageable);
    
    Optional<Room> findById(Long id);
    
    List<Room> findByHotelId(Long hotelId);
    
    Page<Room> findByHotelId(Long hotelId, Pageable pageable);
    
    List<Room> findAvailableRoomsByHotelId(Long hotelId);
    
    List<Room> findByRoomTypeId(Long roomTypeId);
    
    List<Room> findAvailableRoomsByHotelAndType(Long hotelId, Long roomTypeId);
    
    Room save(Room room);
    
    Room update(Room room);
    
    void delete(Long id);
    
    boolean isRoomAvailable(Long roomId, LocalDateTime checkIn, LocalDateTime checkOut);
    
    List<Room> findAvailableRoomsForPeriod(Long hotelId, LocalDateTime checkIn, LocalDateTime checkOut);
    
    List<Room> searchRooms(Long hotelId, Integer minCapacity, Double maxPrice, String amenity);
    
    long countRoomsByHotel(Long hotelId);
    
    long countAvailableRoomsByHotel(Long hotelId);
    
    // Debug methods
    Long countRoomsByHotelIdAndStatus(Long hotelId, Integer status);
    
    List<Room> findAllByHotelIdWithStatus(Long hotelId);
}
