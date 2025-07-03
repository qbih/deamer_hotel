package com.example.dulich.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.dulich.entity.Comment;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    
    // Tìm comment theo hotel
    List<Comment> findByHotelId(Long hotelId);
    
    // Tìm comment theo hotel và status
    List<Comment> findByHotelIdAndStatus(Long hotelId, Integer status);
    
    // Tìm comment theo article
    List<Comment> findByArticleId(Long articleId);
    
    // Tìm comment theo article và status
    List<Comment> findByArticleIdAndStatus(Long articleId, Integer status);
    
    // Tìm comment theo user
    List<Comment> findByUserId(Long userId);
    
    // Tìm comment theo status
    List<Comment> findByStatus(Integer status);
    
    // Phân trang comment theo status
    Page<Comment> findByStatus(Integer status, Pageable pageable);
      // Tìm top 10 comment theo status
    List<Comment> findTop10ByStatusOrderByCreatedAtDesc(Integer status);
    
    // Phân trang comment theo hotel
    Page<Comment> findByHotelIdAndStatus(Long hotelId, Integer status, Pageable pageable);
      // Phân trang comment theo article
    Page<Comment> findByArticleIdAndStatus(Long articleId, Integer status, Pageable pageable);
    
    // Đếm số lượng comment theo hotel
    long countByHotelId(Long hotelId);
    
    // Đếm số lượng comment theo article
    long countByArticleId(Long articleId);
    
    // Đếm số lượng comment theo user
    long countByUserId(Long userId);
    
    // Đếm số lượng comment theo status
    long countByStatus(Integer status);
    
    // Tìm comment là reply của comment khác
    List<Comment> findByParentId(Long parentId);
    
    // Tìm comment không có parent (comment gốc)
    List<Comment> findByParentIdIsNull();
      // Tìm kiếm với các bộ lọc
    @Query("SELECT c FROM Comment c WHERE " +
           "(:hotelId IS NULL OR c.hotel.id = :hotelId) AND " +
           "(:articleId IS NULL OR c.article.id = :articleId) AND " +
           "(:userId IS NULL OR c.user.id = :userId) AND " +
           "(:status IS NULL OR c.status = :status)")    Page<Comment> findWithFilters(@Param("hotelId") Long hotelId,
                                @Param("articleId") Long articleId,
                                @Param("userId") Long userId,
                                @Param("status") Integer status,
                                Pageable pageable);
}
