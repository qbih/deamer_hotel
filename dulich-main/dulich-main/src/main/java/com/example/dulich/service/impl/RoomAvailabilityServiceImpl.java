package com.example.dulich.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.dulich.entity.Room;
import com.example.dulich.repository.BookHotelRoomRepository;
import com.example.dulich.repository.RoomRepository;
import com.example.dulich.service.RoomAvailabilityService;

@Service
public class RoomAvailabilityServiceImpl implements RoomAvailabilityService {

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private BookHotelRoomRepository bookHotelRoomRepository;

    @Override
    public List<Room> findAvailableRooms(Long hotelId, Long roomTypeId,
            LocalDateTime checkInDate, LocalDateTime checkOutDate) {

        // Debug: Log input parameters
        System.out.println("Debug RoomAvailability - hotelId: " + hotelId + ", roomTypeId: " + roomTypeId +
                ", checkIn: " + checkInDate + ", checkOut: " + checkOutDate);

        // Lấy tất cả phòng theo khách sạn và loại phòng
        List<Room> allRooms = roomRepository.findByHotelIdAndRoomTypeIdAndStatus(hotelId, roomTypeId, 1);

        // Debug: Log total rooms found
        System.out.println("Debug RoomAvailability - Total rooms found: " + allRooms.size());
        for (Room room : allRooms) {
            System.out.println("  Room ID: " + room.getId() + ", Number: " + room.getRoomNumber() +
                    ", Type: " + room.getRoomType().getName());
        }

        // Lấy danh sách phòng đã được đặt trong khoảng thời gian này
        List<Long> bookedRoomIds = bookHotelRoomRepository
                .findConflictingBookingsByHotel(hotelId, checkInDate, checkOutDate)
                .stream()
                .filter(bhr -> bhr.getRoom().getRoomType().getId().equals(roomTypeId))
                .map(bhr -> bhr.getRoom().getId())
                .collect(Collectors.toList());

        // Debug: Log booked room IDs
        System.out.println("Debug RoomAvailability - Booked room IDs: " + bookedRoomIds);

        // Lọc ra những phòng chưa được đặt
        List<Room> availableRooms = allRooms.stream()
                .filter(room -> !bookedRoomIds.contains(room.getId()))
                .collect(Collectors.toList());

        // Debug: Log available rooms
        System.out.println("Debug RoomAvailability - Available rooms: " + availableRooms.size());
        for (Room room : availableRooms) {
            System.out.println("  Available Room ID: " + room.getId() + ", Number: " + room.getRoomNumber());
        }

        return availableRooms;
    }

    @Override
    public int countAvailableRooms(Long hotelId, Long roomTypeId,
            LocalDateTime checkInDate, LocalDateTime checkOutDate) {
        return findAvailableRooms(hotelId, roomTypeId, checkInDate, checkOutDate).size();
    }

    @Override
    public boolean hasEnoughAvailableRooms(Long hotelId, Long roomTypeId,
            int requestedRooms, LocalDateTime checkInDate,
            LocalDateTime checkOutDate) {
        int availableCount = countAvailableRooms(hotelId, roomTypeId, checkInDate, checkOutDate);
        return availableCount >= requestedRooms;
    }

    @Override
    public List<Room> selectRoomsForBooking(Long hotelId, Long roomTypeId,
            int numberOfRooms, LocalDateTime checkInDate,
            LocalDateTime checkOutDate) {
        List<Room> availableRooms = findAvailableRooms(hotelId, roomTypeId, checkInDate, checkOutDate);

        if (availableRooms.size() < numberOfRooms) {
            throw new IllegalArgumentException(
                    String.format("Không đủ phòng trống. Yêu cầu: %d, có sẵn: %d",
                            numberOfRooms, availableRooms.size()));
        }

        // Lấy số lượng phòng cần thiết
        return availableRooms.stream()
                .limit(numberOfRooms)
                .collect(Collectors.toList());
    }
}
