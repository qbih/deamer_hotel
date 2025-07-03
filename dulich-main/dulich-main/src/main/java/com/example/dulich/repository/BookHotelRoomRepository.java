package com.example.dulich.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.dulich.entity.BookHotelRoom;

@Repository
public interface BookHotelRoomRepository extends JpaRepository<BookHotelRoom, Long> {

    List<BookHotelRoom> findByBookHotelId(Long bookHotelId);

    List<BookHotelRoom> findByRoomId(Long roomId);

    @Query("SELECT bhr FROM BookHotelRoom bhr WHERE bhr.room.id = :roomId " +
           "AND bhr.bookHotel.status != 5 " +
           "AND bhr.bookHotel.checkInDate < :checkOutDate " +
           "AND bhr.bookHotel.checkOutDate > :checkInDate")
    List<BookHotelRoom> findConflictingBookings(@Param("roomId") Long roomId,
                                                @Param("checkInDate") LocalDateTime checkInDate,
                                                @Param("checkOutDate") LocalDateTime checkOutDate);

    @Query("SELECT bhr FROM BookHotelRoom bhr WHERE bhr.room.hotel.id = :hotelId " +
           "AND bhr.bookHotel.status != 5 " +
           "AND bhr.bookHotel.checkInDate < :checkOutDate " +
           "AND bhr.bookHotel.checkOutDate > :checkInDate")
    List<BookHotelRoom> findConflictingBookingsByHotel(@Param("hotelId") Long hotelId,
                                                       @Param("checkInDate") LocalDateTime checkInDate,
                                                       @Param("checkOutDate") LocalDateTime checkOutDate);
}
