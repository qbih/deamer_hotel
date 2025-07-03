package com.example.dulich.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dulich.entity.GroupPermission;
import com.example.dulich.repository.GroupPermissionRepository;
import com.example.dulich.service.GroupPermissionService;

@Service
@Transactional
public class GroupPermissionServiceImpl implements GroupPermissionService {

    @Autowired
    private GroupPermissionRepository groupPermissionRepository;

    @Override
    @Transactional(readOnly = true)
    public List<GroupPermission> findAll() {
        return groupPermissionRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<GroupPermission> findById(Long id) {
        return groupPermissionRepository.findById(id);
    }

    @Override
    public GroupPermission save(GroupPermission groupPermission) {
        return groupPermissionRepository.save(groupPermission);
    }

    @Override
    public void delete(Long id) {
        groupPermissionRepository.deleteById(id);
    }
}
