package com.example.dulich.service.impl;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dulich.dto.HotelDto;
import com.example.dulich.entity.Hotel;
import com.example.dulich.entity.Location;
import com.example.dulich.entity.User;
import com.example.dulich.repository.HotelRepository;
import com.example.dulich.repository.LocationRepository;
import com.example.dulich.repository.UserRepository;
import com.example.dulich.service.HotelService;

@Service
@Transactional
public class HotelServiceImpl implements HotelService {
    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private UserRepository userRepository;
    
    @Autowired
    private com.example.dulich.repository.ServiceRepository serviceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Hotel> findAll() {
        return hotelRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Hotel> findAllPaged(Pageable pageable) {
        return hotelRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Hotel> findByStatus(Integer status) {
        return hotelRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Hotel> findById(Long id) {
        return hotelRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Hotel> findByLocationId(Long locationId, Pageable pageable) {
        return hotelRepository.findByLocationIdAndStatus(locationId, 1, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Hotel> findWithFilters(String name, Long locationId, Double minPrice, Double maxPrice,
            Integer status, Pageable pageable) {
        return hotelRepository.findWithFilters(name, locationId, minPrice, maxPrice, status, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Hotel> findAvailableHotels(LocalDateTime startDate, LocalDateTime endDate, Pageable pageable) {
        return hotelRepository.findByStatus(1, pageable);
    }

    @Override
    public Hotel createOrUpdate(HotelDto hotelDto) throws Exception {
        Hotel hotel;

        if (hotelDto.getId() != null) {
            // Update case
            hotel = hotelRepository.findById(hotelDto.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Khách sạn không tồn tại"));
        } else {
            // Create case
            hotel = new Hotel();
        }

        // Set properties from DTO
        hotel.setName(hotelDto.getName());
        hotel.setAddress(hotelDto.getAddress());
        hotel.setPhone(hotelDto.getPhone());
        hotel.setNumberPeople(hotelDto.getNumberPeople());
        hotel.setPrice(hotelDto.getPrice());
        hotel.setSale(hotelDto.getSale());
        hotel.setDescription(hotelDto.getDescription());
        hotel.setContent(hotelDto.getContent());
        hotel.setStatus(hotelDto.getStatus());
        hotel.setStartDate(hotelDto.getStartDate());
        hotel.setEndDate(hotelDto.getEndDate());

        // Set image from DTO
        if (hotelDto.getImage() != null && !hotelDto.getImage().isEmpty()) {
            hotel.setImage(hotelDto.getImage());
        }


        // Set location
        if (hotelDto.getLocationId() != null) {
            Location location = locationRepository.findById(hotelDto.getLocationId())
                    .orElseThrow(() -> new IllegalArgumentException("Địa điểm không tồn tại"));
            hotel.setLocation(location);
        }

        // Set user
        if (hotel.getUser() == null && hotelDto.getUserId() != null) {
            User user = userRepository.findById(hotelDto.getUserId())
                    .orElse(null);
            if (user != null) {
                hotel.setUser(user);
            }
        }

        // Save to database
        return hotelRepository.save(hotel);
    }

    @Override
    public void delete(Long id) {
        Optional<Hotel> hotelOpt = hotelRepository.findById(id);
        if (hotelOpt.isPresent()) {
            hotelRepository.deleteById(id);
        }
    }

    @Override
    public HotelDto convertToDto(Hotel hotel) {
        HotelDto dto = new HotelDto();

        dto.setId(hotel.getId());
        dto.setName(hotel.getName());
        dto.setImage(hotel.getImage());
        dto.setAlbumImage(hotel.getAlbumImage());
        dto.setAddress(hotel.getAddress());
        dto.setPhone(hotel.getPhone());
        dto.setNumberPeople(hotel.getNumberPeople());
        dto.setPrice(hotel.getPrice());
        dto.setSale(hotel.getSale());
        dto.setDescription(hotel.getDescription());
        dto.setContent(hotel.getContent());
        dto.setStatus(hotel.getStatus());
        dto.setStartDate(hotel.getStartDate());
        dto.setEndDate(hotel.getEndDate());
        dto.setCreatedAt(hotel.getCreatedAt());
        dto.setUpdatedAt(hotel.getUpdatedAt());

        if (hotel.getLocation() != null) {
            dto.setLocationId(hotel.getLocation().getId());
            dto.setLocationName(hotel.getLocation().getName());
        }

        if (hotel.getUser() != null) {
            dto.setUserId(hotel.getUser().getId());
            dto.setUserName(hotel.getUser().getUsername());
        }

        return dto;
    }

    @Override
    public Hotel convertToEntity(HotelDto hotelDto) {
        Hotel hotel = new Hotel();

        if (hotelDto.getId() != null) {
            hotel.setId(hotelDto.getId());
        }

        hotel.setName(hotelDto.getName());
        hotel.setImage(hotelDto.getImage());
        hotel.setAlbumImage(hotelDto.getAlbumImage());
        hotel.setAddress(hotelDto.getAddress());
        hotel.setPhone(hotelDto.getPhone());
        hotel.setNumberPeople(hotelDto.getNumberPeople());
        hotel.setPrice(hotelDto.getPrice());
        hotel.setSale(hotelDto.getSale());
        hotel.setDescription(hotelDto.getDescription());
        hotel.setContent(hotelDto.getContent());
        hotel.setStatus(hotelDto.getStatus());
        hotel.setStartDate(hotelDto.getStartDate());
        hotel.setEndDate(hotelDto.getEndDate());

        return hotel;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Hotel> findFeaturedHotels(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return hotelRepository.findFeaturedHotels(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Hotel> findLatestHotels(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return hotelRepository.findLatestHotels(pageable);
    }    @Override
    @Transactional(readOnly = true)
    public List<Hotel> findRecommendedHotels(Long hotelId, int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return hotelRepository.findRecommendedHotels(hotelId, pageable);
    }    @Override
    @Transactional(readOnly = true)
    public Set<com.example.dulich.entity.Service> getHotelServices(Long hotelId) {
        try {
            // Use the ServiceRepository to fetch services directly via the join table
            // This avoids the need for the bidirectional relationship
            List<com.example.dulich.entity.Service> services = serviceRepository.findByHotelId(hotelId);
            
            // Create defensive copies of each service to avoid ConcurrentModificationException
            Set<com.example.dulich.entity.Service> safeServices = new HashSet<>();
            for (com.example.dulich.entity.Service service : services) {
                com.example.dulich.entity.Service safeCopy = new com.example.dulich.entity.Service();
                safeCopy.setId(service.getId());
                safeCopy.setName(service.getName());
                safeCopy.setDescription(service.getDescription());
                safeCopy.setPrice(service.getPrice());
                safeCopy.setCategory(service.getCategory());
                safeCopy.setIsIncluded(service.getIsIncluded());
                safeCopy.setIsAvailable24h(service.getIsAvailable24h());
                safeCopy.setOperatingHours(service.getOperatingHours());
                safeCopy.setStatus(service.getStatus());
                safeCopy.setSortOrder(service.getSortOrder());
                safeServices.add(safeCopy);
            }
            
            return safeServices;
        } catch (Exception e) {
            // Log error
            System.err.println("Error loading hotel services for hotel " + hotelId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return new HashSet<>();
    }
}
