package com.example.dulich.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.dulich.dto.BookHotelDto;
import com.example.dulich.entity.BookHotel;

public interface BookHotelService {
    
    List<BookHotel> findAll();
    
    Page<BookHotel> findAllPaged(Pageable pageable);
    
    Optional<BookHotel> findById(Long id);
    
    List<BookHotel> findByUserId(Long userId);
    
    Page<BookHotel> findByHotelId(Long hotelId, Pageable pageable);
    
    Page<BookHotel> findWithFilters(Long hotelId, Long userId, Integer status, 
                                   LocalDateTime checkInFrom, LocalDateTime checkInTo,
                                   Pageable pageable);
    
    BookHotel create(BookHotelDto bookHotelDto);
    
    BookHotel update(BookHotelDto bookHotelDto, Long id);
    
    BookHotel updateStatus(Long id, Integer status);
    
    boolean updatePaymentStatus(Long id, String paymentMethod);
    
    void delete(Long id);
    
    BookHotelDto convertToDto(BookHotel bookHotel);
    
    BookHotel convertToEntity(BookHotelDto bookHotelDto);
    
    Map<Integer, String> getStatusList();
    
    long countByStatus(Integer status);
    
    double getTotalRevenue();
    
    double getTotalRevenueByStatus(Integer status);
    
    double getTotalRevenueInRange(LocalDateTime startDate, LocalDateTime endDate);
}
