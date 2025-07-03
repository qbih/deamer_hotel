package com.example.dulich.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.example.dulich.dto.CommentDto;
import com.example.dulich.entity.Comment;

public interface CommentService {
    
    List<Comment> findAll();
    
    Page<Comment> findAllPaged(Pageable pageable);
      Optional<Comment> findById(Long id);
    
    List<Comment> findByHotelId(Long hotelId);
    
    List<Comment> findByArticleId(Long articleId);
    
    List<Comment> findByUserId(Long userId);
    
    List<Comment> findByStatus(Integer status);
    
    List<Comment> findReplies(Long parentId);
    
    Page<Comment> findWithFilters(Long hotelId, Long articleId, Long userId, Integer status, Pageable pageable);
    
    Comment create(CommentDto commentDto);
    
    Comment update(CommentDto commentDto, Long id);
    
    Comment updateStatus(Long id, Integer status);
    
    void delete(Long id);
    
    CommentDto convertToDto(Comment comment);
      Comment convertToEntity(CommentDto commentDto);
    
    long countByHotelId(Long hotelId);
    
    long countByArticleId(Long articleId);
    
    long countByUserId(Long userId);
    
    long countByStatus(Integer status);
}
