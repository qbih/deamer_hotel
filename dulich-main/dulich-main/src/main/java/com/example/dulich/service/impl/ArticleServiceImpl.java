package com.example.dulich.service.impl;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.dulich.dto.ArticleDto;
import com.example.dulich.entity.Article;
import com.example.dulich.entity.Category;
import com.example.dulich.entity.User;
import com.example.dulich.repository.ArticleRepository;
import com.example.dulich.repository.CategoryRepository;
import com.example.dulich.repository.UserRepository;
import com.example.dulich.service.ArticleService;
import com.example.dulich.utils.FileUploadUtil;
import com.example.dulich.utils.SlugUtil;

@Service
@Transactional
public class ArticleServiceImpl implements ArticleService {

    @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
      @Autowired
    private UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public List<Article> findAll() {
        return articleRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Article> findAllPaged(Pageable pageable) {
        return articleRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Article> findByActive(Integer active) {
        return articleRepository.findByActive(active);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<Article> findById(Long id) {
        return articleRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public Article findBySlug(String slug) {
        return articleRepository.findBySlug(slug);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Article> findByCategoryId(Long categoryId, Pageable pageable) {
        return articleRepository.findByCategoryIdAndActive(categoryId, 1, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<Article> findWithFilters(String title, Long categoryId, Integer active, Pageable pageable) {
        return articleRepository.findWithFilters(title, categoryId, active, pageable);
    }

    @Override
    public Article createOrUpdate(ArticleDto articleDto) throws Exception {
        return createOrUpdate(articleDto, null, null);
    }

    @Override
    public Article createOrUpdate(ArticleDto articleDto, Long id, MultipartFile imageFile) throws Exception {
        Article article;
        
        if (id != null) {
            // Update case
            article = articleRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Bài viết không tồn tại"));
        } else {
            // Create case
            article = new Article();
        }
        
        // Set properties from DTO
        article.setName(articleDto.getTitle());
        article.setDescription(articleDto.getSummary());
        article.setContent(articleDto.getContent());
        article.setActive(articleDto.getActive());
        article.setHot(articleDto.getFeatured());
        article.setTitleSeo(articleDto.getTitleSeo());
        article.setKeywordSeo(articleDto.getKeywordSeo());
        
        // Generate slug if it's a new article or title changed
        if (article.getSlug() == null || !article.getName().equals(articleDto.getTitle())) {
            String baseSlug = SlugUtil.makeSlug(articleDto.getTitle());
            String uniqueSlug = baseSlug;
            int counter = 1;
            
            while (articleRepository.existsBySlug(uniqueSlug) && 
                   (article.getId() == null || !uniqueSlug.equals(article.getSlug()))) {
                uniqueSlug = baseSlug + "-" + counter;
                counter++;
            }
            
            article.setSlug(uniqueSlug);
        }
        
        // Set category
        if (articleDto.getCategoryId() != null) {
            Category category = categoryRepository.findById(articleDto.getCategoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Danh mục không tồn tại"));
            article.setCategory(category);
        }
          // Set author (current user)
        if (article.getUser() == null && articleDto.getAuthorId() != null) {
            User author = userRepository.findById(articleDto.getAuthorId())
                    .orElse(null);
            if (author != null) {
                article.setUser(author);
            }
        }        // Handle image upload
        if (imageFile != null && !imageFile.isEmpty()) {
            try {
                // Delete old image if exists
                if (article.getAvatar() != null && !article.getAvatar().isEmpty()) {
                    FileUploadUtil.deleteFile(article.getAvatar());
                }
                
                // Upload new image
                String fileName = StringUtils.cleanPath(imageFile.getOriginalFilename());
                String uploadDir = "uploads/articles";  // Relative path from project root
                String savedFileName = FileUploadUtil.saveFile(uploadDir, fileName, imageFile);
                article.setAvatar("/uploads/articles/" + savedFileName);  // Web path for display
            } catch (Exception e) {
                throw new Exception("Lỗi khi upload hình ảnh: " + e.getMessage());
            }
        }
        
        // Save to database
        return articleRepository.save(article);
    }    @Override
    public void delete(Long id) {
        Optional<Article> articleOpt = articleRepository.findById(id);
        if (articleOpt.isPresent()) {
            Article article = articleOpt.get();
            
            // Delete associated image
            if (article.getAvatar() != null && !article.getAvatar().isEmpty()) {
                FileUploadUtil.deleteFile(article.getAvatar());
            }
            
            articleRepository.deleteById(id);
        }
    }

    @Override
    public ArticleDto convertToDto(Article article) {
        ArticleDto dto = new ArticleDto();
        
        dto.setId(article.getId());
        dto.setTitle(article.getName());
        dto.setSlug(article.getSlug());
        dto.setContent(article.getContent());
        dto.setSummary(article.getDescription());
        dto.setImage(article.getAvatar());
        dto.setActive(article.getActive());
        dto.setFeatured(article.getHot());
        dto.setTitleSeo(article.getTitleSeo());
        dto.setKeywordSeo(article.getKeywordSeo());
        dto.setCreatedAt(article.getCreatedAt());
        dto.setUpdatedAt(article.getUpdatedAt());
        
        if (article.getCategory() != null) {
            dto.setCategoryId(article.getCategory().getId());
            dto.setCategoryName(article.getCategory().getName());
        }
        
        if (article.getUser() != null) {
            dto.setAuthorId(article.getUser().getId());
            dto.setAuthorName(article.getUser().getUsername());
        }
        
        return dto;
    }

    @Override
    public Article convertToEntity(ArticleDto articleDto) {
        Article article = new Article();
        
        if (articleDto.getId() != null) {
            article.setId(articleDto.getId());
        }
        
        article.setName(articleDto.getTitle());
        article.setSlug(articleDto.getSlug());
        article.setContent(articleDto.getContent());
        article.setDescription(articleDto.getSummary());
        article.setAvatar(articleDto.getImage());
        article.setActive(articleDto.getActive());
        article.setHot(articleDto.getFeatured());
        article.setTitleSeo(articleDto.getTitleSeo());
        article.setKeywordSeo(articleDto.getKeywordSeo());
        
        return article;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Article> findFeaturedArticles(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return articleRepository.findFeaturedArticles(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Article> findLatestArticles(int limit) {
        Pageable pageable = PageRequest.of(0, limit);
        return articleRepository.findLatestArticles(pageable);
    }
}
