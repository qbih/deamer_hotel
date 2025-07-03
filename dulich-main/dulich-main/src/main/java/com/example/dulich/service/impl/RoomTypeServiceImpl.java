package com.example.dulich.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dulich.entity.RoomType;
import com.example.dulich.repository.RoomTypeRepository;
import com.example.dulich.service.RoomTypeService;

@Service
@Transactional
public class RoomTypeServiceImpl implements RoomTypeService {

    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @Override
    @Transactional(readOnly = true)
    public List<RoomType> findAll() {
        return roomTypeRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<RoomType> findAllPaged(Pageable pageable) {
        return roomTypeRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<RoomType> findById(Long id) {
        return roomTypeRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomType> findByStatus(Integer status) {
        return roomTypeRepository.findByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomType> findByNameContaining(String name) {
        return roomTypeRepository.findByNameContainingIgnoreCase(name);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomType> findByBasePriceRange(Double minPrice, Double maxPrice) {
        return roomTypeRepository.findByBasePriceRange(minPrice, maxPrice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomType> findByMaxCapacityGreaterThanEqual(Integer minCapacity) {
        return roomTypeRepository.findByMaxCapacityGreaterThanEqual(minCapacity);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RoomType> findByBedType(String bedType) {
        return roomTypeRepository.findByBedType(bedType);
    }

    @Override
    public RoomType save(RoomType roomType) {
        return roomTypeRepository.save(roomType);
    }

    @Override
    public RoomType update(RoomType roomType) {
        RoomType existingRoomType = roomTypeRepository.findById(roomType.getId())
                .orElseThrow(() -> new IllegalArgumentException("Loại phòng không tồn tại"));
        
        // Update fields
        existingRoomType.setName(roomType.getName());
        existingRoomType.setDescription(roomType.getDescription());
        existingRoomType.setBasePrice(roomType.getBasePrice());
        existingRoomType.setMaxCapacity(roomType.getMaxCapacity());
        existingRoomType.setBedType(roomType.getBedType());
        existingRoomType.setRoomSize(roomType.getRoomSize());
        existingRoomType.setAmenities(roomType.getAmenities());
        existingRoomType.setImages(roomType.getImages());
        existingRoomType.setStatus(roomType.getStatus());
        
        return roomTypeRepository.save(existingRoomType);
    }

    @Override
    public void delete(Long id) {
        roomTypeRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(Integer status) {
        return roomTypeRepository.countByStatus(status);
    }
}
