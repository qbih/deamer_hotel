package com.example.dulich.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.transaction.annotation.Transactional;

import com.example.dulich.entity.Service;
import com.example.dulich.repository.ServiceRepository;
import com.example.dulich.service.ServiceService;

@org.springframework.stereotype.Service
@Transactional
public class ServiceServiceImpl implements ServiceService {

    @Autowired
    private ServiceRepository serviceRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Service> findAll() {
        return serviceRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Service> findAllPaged(Pageable pageable) {
        return serviceRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Service> findById(Long id) {
        return serviceRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Service> findByStatus(Integer status) {
        return serviceRepository.findByStatus(status);
    }    @Override
    @Transactional(readOnly = true)
    public Page<Service> findByStatus(Integer status, Pageable pageable) {
        // Sử dụng findAll với pageable và lọc theo status
        return serviceRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Service> findByCategory(String category) {
        return serviceRepository.findByCategory(category);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Service> findByCategoryAndStatus(String category, Integer status) {
        return serviceRepository.findByCategoryAndStatus(category, status);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Service> findIncludedServices() {
        return serviceRepository.findByIsIncludedTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Service> findPaidServices() {
        return serviceRepository.findByIsIncludedFalse();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Service> findAvailable24hServices() {
        return serviceRepository.findByIsAvailable24hTrue();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Service> findByPriceRange(Double minPrice, Double maxPrice) {
        return serviceRepository.findByPriceRange(minPrice, maxPrice);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> findDistinctCategories() {
        return List.of("Room Service", "Spa", "Restaurant", "Transport", "Entertainment", "Business", "Other");
    }

    @Override
    public Service save(Service service) {
        return serviceRepository.save(service);
    }

    @Override
    public Service update(Service service) {
        Service existingService = serviceRepository.findById(service.getId())
                .orElseThrow(() -> new IllegalArgumentException("Dịch vụ không tồn tại"));
          // Update fields
        existingService.setName(service.getName());
        existingService.setDescription(service.getDescription());
        existingService.setPrice(service.getPrice());
        existingService.setCategory(service.getCategory());
        existingService.setIsIncluded(service.getIsIncluded());
        existingService.setIsAvailable24h(service.getIsAvailable24h());
        existingService.setOperatingHours(service.getOperatingHours());
        existingService.setStatus(service.getStatus());
        existingService.setSortOrder(service.getSortOrder());
        
        return serviceRepository.save(existingService);
    }

    @Override
    public void delete(Long id) {
        serviceRepository.deleteById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(Integer status) {
        return serviceRepository.countByStatus(status);
    }
}
