package com.example.dulich.service;

import java.time.LocalDateTime;
import java.util.List;

import com.example.dulich.entity.Room;

public interface RoomAvailabilityService {
    
    /**
     * Tìm phòng trống theo khách sạn, loại phòng và thời gian
     */
    List<Room> findAvailableRooms(Long hotelId, Long roomTypeId, 
                                 LocalDateTime checkInDate, LocalDateTime checkOutDate);
    
    /**
     * Kiểm tra số lượng phòng trống theo loại phòng
     */
    int countAvailableRooms(Long hotelId, Long roomTypeId, 
                           LocalDateTime checkInDate, LocalDateTime checkOutDate);
    
    /**
     * Kiểm tra xem có đủ phòng trống không
     */
    boolean hasEnoughAvailableRooms(Long hotelId, Long roomTypeId, 
                                   int requestedRooms, LocalDateTime checkInDate, 
                                   LocalDateTime checkOutDate);
    
    /**
     * Lấy danh sách phòng để đặt (không trùng lặp)
     */
    List<Room> selectRoomsForBooking(Long hotelId, Long roomTypeId, 
                                    int numberOfRooms, LocalDateTime checkInDate, 
                                    LocalDateTime checkOutDate);
}
