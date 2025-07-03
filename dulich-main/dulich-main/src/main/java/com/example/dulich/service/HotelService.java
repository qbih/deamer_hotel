package com.example.dulich.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.dulich.dto.HotelDto;
import com.example.dulich.entity.Hotel;
import com.example.dulich.entity.Service;

public interface HotelService {

    List<Hotel> findAll();

    Page<Hotel> findAllPaged(Pageable pageable);

    List<Hotel> findByStatus(Integer status);

    Optional<Hotel> findById(Long id);

    Page<Hotel> findByLocationId(Long locationId, Pageable pageable);

    Page<Hotel> findWithFilters(String name, Long locationId, Double minPrice, Double maxPrice,
            Integer status, Pageable pageable);

    Page<Hotel> findAvailableHotels(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable);

    Hotel createOrUpdate(HotelDto hotelDto) throws Exception;

    void delete(Long id);

    HotelDto convertToDto(Hotel hotel);

    Hotel convertToEntity(HotelDto hotelDto);

    List<Hotel> findFeaturedHotels(int limit);

    List<Hotel> findLatestHotels(int limit);

    List<Hotel> findRecommendedHotels(Long hotelId, int limit);
    
    Set<Service> getHotelServices(Long hotelId);
}
