package com.example.dulich.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.dulich.entity.Article;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    
    // Tìm article theo category id
    List<Article> findByCategoryId(Long categoryId);
    
    // Tìm article theo slug
    Article findBySlug(String slug);
    
    // Tìm article theo userId
    List<Article> findByUserId(Long userId);
    
    // Tìm article theo active
    List<Article> findByActive(Integer active);
    
    // Tìm kiếm article theo name
    List<Article> findByNameContaining(String name);
    
    // Tìm kiếm article theo name và active với phân trang
    Page<Article> findByNameContainingAndActive(String name, Integer active, Pageable pageable);
    
    // Phân trang article theo active
    Page<Article> findByActive(Integer active, Pageable pageable);
    
    // Lấy 6 bài viết mới nhất
    List<Article> findTop6ByActiveOrderByIdDesc(Integer active);
    
    // Lấy bài viết liên quan (khác id, cùng active)
    List<Article> findTop4ByIdNotAndActiveOrderByCreatedAtDesc(Long id, Integer active);
    
    // Phân trang article theo category và active
    Page<Article> findByCategoryIdAndActive(Long categoryId, Integer active, Pageable pageable);
    
    // Kiểm tra slug đã tồn tại chưa
    boolean existsBySlug(String slug);
    
    // Tìm article nổi bật (hot = 1)
    List<Article> findByHotAndActive(Integer hot, Integer active);
    
    // Tìm kiếm với filters
    @Query("SELECT a FROM Article a WHERE " +
           "(:name IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :name, '%'))) AND " +
           "(:categoryId IS NULL OR a.category.id = :categoryId) AND " +
           "(:active IS NULL OR a.active = :active)")
    Page<Article> findWithFilters(@Param("name") String name, 
                                 @Param("categoryId") Long categoryId, 
                                 @Param("active") Integer active, 
                                 Pageable pageable);
    
    // Lấy bài viết nổi bật với limit
    @Query("SELECT a FROM Article a WHERE a.hot = 1 AND a.active = 1 ORDER BY a.createdAt DESC")
    List<Article> findFeaturedArticles(Pageable pageable);
    
    // Lấy bài viết mới nhất với limit
    @Query("SELECT a FROM Article a WHERE a.active = 1 ORDER BY a.createdAt DESC")
    List<Article> findLatestArticles(Pageable pageable);
}
