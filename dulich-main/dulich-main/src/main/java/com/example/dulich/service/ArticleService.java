package com.example.dulich.service;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import com.example.dulich.dto.ArticleDto;
import com.example.dulich.entity.Article;

public interface ArticleService {
    
    List<Article> findAll();
    
    Page<Article> findAllPaged(Pageable pageable);
    
    List<Article> findByActive(Integer active);
    
    Optional<Article> findById(Long id);
    
    Article findBySlug(String slug);
    
    Page<Article> findByCategoryId(Long categoryId, Pageable pageable);
    
    Page<Article> findWithFilters(String title, Long categoryId, Integer active, Pageable pageable);
    
    Article createOrUpdate(ArticleDto articleDto) throws Exception;
    
    Article createOrUpdate(ArticleDto articleDto, Long id, MultipartFile imageFile) throws Exception;
    
    void delete(Long id);
    
    ArticleDto convertToDto(Article article);
    
    Article convertToEntity(ArticleDto articleDto);
    
    List<Article> findFeaturedArticles(int limit);
    
    List<Article> findLatestArticles(int limit);
}
