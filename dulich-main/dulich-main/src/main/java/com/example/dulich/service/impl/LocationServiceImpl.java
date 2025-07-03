package com.example.dulich.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dulich.entity.Location;
import com.example.dulich.repository.LocationRepository;
import com.example.dulich.service.LocationService;

@Service
@Transactional
public class LocationServiceImpl implements LocationService {

    @Autowired
    private LocationRepository locationRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Location> findAll() {
        return locationRepository.findAll();
    }
    
    @Override
    @Transactional(readOnly = true)
    public List<Location> findAllActiveLocations() {
        // Tìm các địa điểm có trạng thái = 1 (active)
        return locationRepository.findByStatus(1);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Location> findById(Long id) {
        return locationRepository.findById(id);
    }

    @Override
    public Location save(Location location) {
        return locationRepository.save(location);
    }

    @Override
    public void delete(Long id) {
        locationRepository.deleteById(id);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Location> findPaginated(Pageable pageable) {
        return locationRepository.findAll(pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Page<Location> searchByName(String name, Pageable pageable) {
        return locationRepository.findByNameContaining(name, pageable);
    }
    
    @Override
    @Transactional(readOnly = true)
    public Optional<Location> findBySlug(String slug) {
        return Optional.ofNullable(locationRepository.findBySlug(slug));
    }
}
