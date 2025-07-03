package com.example.dulich.service.impl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dulich.entity.BookHotel;
import com.example.dulich.entity.Room;
import com.example.dulich.repository.BookHotelRoomRepository;
import com.example.dulich.repository.HotelRepository;
import com.example.dulich.repository.RoomRepository;
import com.example.dulich.repository.RoomTypeRepository;
import com.example.dulich.service.RoomService;

@Service
@Transactional
public class RoomServiceImpl implements RoomService {    @Autowired
    private RoomRepository roomRepository;
    
    @Autowired
    private BookHotelRoomRepository bookHotelRoomRepository;
    
    @Autowired
    private HotelRepository hotelRepository;
    
    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Room> findAll() {
        return roomRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Room> findAllPaged(Pageable pageable) {
        return roomRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Room> findById(Long id) {
        return roomRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> findByHotelId(Long hotelId) {
        return roomRepository.findByHotelId(hotelId);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Room> findByHotelId(Long hotelId, Pageable pageable) {
        return roomRepository.findByHotelId(hotelId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> findAvailableRoomsByHotelId(Long hotelId) {
        return roomRepository.findByHotelIdAndStatus(hotelId, 1); // Status 1 = Available
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> findByRoomTypeId(Long roomTypeId) {
        return roomRepository.findByRoomTypeId(roomTypeId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> findAvailableRoomsByHotelAndType(Long hotelId, Long roomTypeId) {
        return roomRepository.findByHotelIdAndRoomTypeIdAndStatus(hotelId, roomTypeId, 1);
    }

    @Override
    public Room save(Room room) {
        return roomRepository.save(room);
    }

    @Override
    public Room update(Room room) {
        Room existingRoom = roomRepository.findById(room.getId())
                .orElseThrow(() -> new IllegalArgumentException("Phòng không tồn tại"));
        
        // Update fields
        existingRoom.setRoomNumber(room.getRoomNumber());
        existingRoom.setRoomType(room.getRoomType());
        existingRoom.setFloorNumber(room.getFloorNumber());
        existingRoom.setCapacity(room.getCapacity());
        existingRoom.setPricePerNight(room.getPricePerNight());
        existingRoom.setDescription(room.getDescription());
        existingRoom.setAmenities(room.getAmenities());
        existingRoom.setImages(room.getImages());
        existingRoom.setStatus(room.getStatus());
        existingRoom.setIsSmokingAllowed(room.getIsSmokingAllowed());
        existingRoom.setHasBalcony(room.getHasBalcony());
        existingRoom.setHasSeaView(room.getHasSeaView());
        existingRoom.setHasCityView(room.getHasCityView());
        
        return roomRepository.save(existingRoom);
    }

    @Override
    public void delete(Long id) {
        roomRepository.deleteById(id);
    }    @Override
    @Transactional(readOnly = true)
    public boolean isRoomAvailable(Long roomId, LocalDateTime checkIn, LocalDateTime checkOut) {
        // Kiểm tra xem phòng có đang được đặt trong khoảng thời gian này không
        // Tìm tất cả BookHotelRoom có room_id = roomId
        List<com.example.dulich.entity.BookHotelRoom> roomBookings = bookHotelRoomRepository.findByRoomId(roomId);
        
        // Kiểm tra conflict với các booking hiện tại
        boolean hasConflict = roomBookings.stream()
                .filter(bookingRoom -> bookingRoom.getBookHotel().getStatus() != BookHotel.STATUS_CANCELLED) // Không tính đơn đã hủy
                .anyMatch(bookingRoom -> {
                    com.example.dulich.entity.BookHotel booking = bookingRoom.getBookHotel();
                    // Kiểm tra overlap thời gian
                    return checkIn.isBefore(booking.getCheckOutDate()) && 
                           checkOut.isAfter(booking.getCheckInDate());
                });
        
        return !hasConflict;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> findAvailableRoomsForPeriod(Long hotelId, LocalDateTime checkIn, LocalDateTime checkOut) {
        List<Room> allRooms = roomRepository.findByHotelIdAndStatus(hotelId, 1);
        
        return allRooms.stream()
                .filter(room -> isRoomAvailable(room.getId(), checkIn, checkOut))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Room> searchRooms(Long hotelId, Integer minCapacity, Double maxPrice, String amenity) {
        List<Room> rooms = roomRepository.findByHotelIdAndStatus(hotelId, 1);
        
        return rooms.stream()
                .filter(room -> minCapacity == null || room.getCapacity() >= minCapacity)
                .filter(room -> maxPrice == null || room.getPricePerNight() <= maxPrice)
                .filter(room -> amenity == null || room.getAmenities().contains(amenity))
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public long countRoomsByHotel(Long hotelId) {
        return roomRepository.countByHotelId(hotelId);
    }

    @Override
    @Transactional(readOnly = true)
    public long countAvailableRoomsByHotel(Long hotelId) {
        return roomRepository.countByHotelIdAndStatus(hotelId, 1);
    }

    @Override
    @Transactional(readOnly = true)
    public Long countRoomsByHotelIdAndStatus(Long hotelId, Integer status) {
        return roomRepository.countRoomsByHotelIdAndStatus(hotelId, status);
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Room> findAllByHotelIdWithStatus(Long hotelId) {
        return roomRepository.findAllByHotelIdWithStatus(hotelId);
    }

    /**
     * Create sample rooms for a hotel if none exist
     * @param hotelId Hotel ID
     * @return List of created rooms
     */
    public List<Room> createSampleRoomsForHotel(Long hotelId) {
        // First check if any rooms exist
        List<Room> existingRooms = findByHotelId(hotelId);
        if (!existingRooms.isEmpty()) {
            return existingRooms;
        }
        
        // Get hotel
        com.example.dulich.entity.Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new IllegalArgumentException("Hotel not found with id: " + hotelId));
          // Check for existing room types first
        List<com.example.dulich.entity.RoomType> existingTypes = roomTypeRepository.findAll();
        com.example.dulich.entity.RoomType standardType;
        com.example.dulich.entity.RoomType deluxeType;
        
        if (existingTypes.isEmpty()) {
            // Create sample room types if needed
            standardType = new com.example.dulich.entity.RoomType();
            standardType.setName("Standard");
            standardType.setDescription("Phòng tiêu chuẩn");
            standardType.setBasePrice(500000.0); // Set base price
            standardType.setMaxCapacity(2);
            standardType.setBedType("Double");
            standardType.setRoomSize(25.0);
            standardType.setAmenities("WiFi, TV, Điều hòa, Minibar");
            standardType.setStatus(1);
            standardType = roomTypeRepository.save(standardType);
            
            deluxeType = new com.example.dulich.entity.RoomType();
            deluxeType.setName("Deluxe");
            deluxeType.setDescription("Phòng cao cấp");
            deluxeType.setBasePrice(800000.0); // Set base price
            deluxeType.setMaxCapacity(3);
            deluxeType.setBedType("King");
            deluxeType.setRoomSize(35.0);
            deluxeType.setAmenities("WiFi, TV, Điều hòa, Minibar, Bồn tắm, Đồ dùng miễn phí");
            deluxeType.setStatus(1);
            deluxeType = roomTypeRepository.save(deluxeType);
        } else {
            // Use existing room types
            standardType = existingTypes.stream()
                .filter(rt -> "Standard".equalsIgnoreCase(rt.getName()))
                .findFirst()
                .orElseGet(() -> {
                    com.example.dulich.entity.RoomType newType = new com.example.dulich.entity.RoomType();
                    newType.setName("Standard");
                    newType.setDescription("Phòng tiêu chuẩn");
                    newType.setBasePrice(500000.0);
                    newType.setStatus(1);
                    return roomTypeRepository.save(newType);
                });
                
            deluxeType = existingTypes.stream()
                .filter(rt -> "Deluxe".equalsIgnoreCase(rt.getName()))
                .findFirst()
                .orElseGet(() -> {
                    com.example.dulich.entity.RoomType newType = new com.example.dulich.entity.RoomType();
                    newType.setName("Deluxe");
                    newType.setDescription("Phòng cao cấp");
                    newType.setBasePrice(800000.0);
                    newType.setStatus(1);
                    return roomTypeRepository.save(newType);
                });
        }
        
        // Create sample rooms
        List<Room> createdRooms = new ArrayList<>();
        
        for (int i = 1; i <= 5; i++) {
            Room standardRoom = new Room();
            standardRoom.setHotel(hotel);
            standardRoom.setRoomNumber("10" + i);
            standardRoom.setRoomType(standardType);
            standardRoom.setFloorNumber(1);
            standardRoom.setCapacity(2);
            standardRoom.setPricePerNight(hotel.getPrice());
            standardRoom.setDescription("Phòng tiêu chuẩn thoải mái");
            standardRoom.setAmenities("WiFi, TV, Điều hòa, Minibar");
            standardRoom.setStatus(1); // Available
            createdRooms.add(roomRepository.save(standardRoom));
        }
        
        for (int i = 1; i <= 3; i++) {
            Room deluxeRoom = new Room();
            deluxeRoom.setHotel(hotel);
            deluxeRoom.setRoomNumber("20" + i);
            deluxeRoom.setRoomType(deluxeType);
            deluxeRoom.setFloorNumber(2);
            deluxeRoom.setCapacity(2);
            deluxeRoom.setPricePerNight(hotel.getPrice() * 1.5);
            deluxeRoom.setDescription("Phòng cao cấp với nhiều tiện nghi");
            deluxeRoom.setAmenities("WiFi, TV, Điều hòa, Minibar, Bồn tắm, Đồ dùng miễn phí");
            deluxeRoom.setStatus(1); // Available
            createdRooms.add(roomRepository.save(deluxeRoom));
        }
        
        return createdRooms;
    }
}
