package com.example.dulich.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class ArticleDto {
    
    private Long id;
    
    @NotBlank(message = "Tiêu đề không được để trống")
    @Size(max = 255, message = "Tiêu đề không được vượt quá 255 ký tự")
    private String title;
    
    private String slug;
    
    @NotBlank(message = "Nội dung không được để trống")
    private String content;
    
    private String summary;
    
    private String image;
    
    @NotNull(message = "Danh mục không được để trống")
    private Long categoryId;
    
    private String categoryName;
    
    private Integer active = 1;
    
    private Integer home = 0;
    
    private Integer featured = 0;
    
    private String titleSeo;
    
    private String keywordSeo;
    
    private String descriptionSeo;
    
    private Long authorId;
    
    private String authorName;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // Constructors
    public ArticleDto() {}
    
    public ArticleDto(String title, String content, Long categoryId) {
        this.title = title;
        this.content = content;
        this.categoryId = categoryId;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getTitle() {
        return title;
    }
    
    public void setTitle(String title) {
        this.title = title;
    }
    
    public String getSlug() {
        return slug;
    }
    
    public void setSlug(String slug) {
        this.slug = slug;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public String getSummary() {
        return summary;
    }
    
    public void setSummary(String summary) {
        this.summary = summary;
    }
    
    public String getImage() {
        return image;
    }
    
    public void setImage(String image) {
        this.image = image;
    }
    
    public Long getCategoryId() {
        return categoryId;
    }
    
    public void setCategoryId(Long categoryId) {
        this.categoryId = categoryId;
    }
    
    public String getCategoryName() {
        return categoryName;
    }
    
    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
    
    public Integer getActive() {
        return active;
    }
    
    public void setActive(Integer active) {
        this.active = active;
    }
    
    public Integer getHome() {
        return home;
    }
    
    public void setHome(Integer home) {
        this.home = home;
    }
    
    public Integer getFeatured() {
        return featured;
    }
    
    public void setFeatured(Integer featured) {
        this.featured = featured;
    }
    
    public String getTitleSeo() {
        return titleSeo;
    }
    
    public void setTitleSeo(String titleSeo) {
        this.titleSeo = titleSeo;
    }
    
    public String getKeywordSeo() {
        return keywordSeo;
    }
    
    public void setKeywordSeo(String keywordSeo) {
        this.keywordSeo = keywordSeo;
    }
    
    public String getDescriptionSeo() {
        return descriptionSeo;
    }
    
    public void setDescriptionSeo(String descriptionSeo) {
        this.descriptionSeo = descriptionSeo;
    }
    
    public Long getAuthorId() {
        return authorId;
    }
    
    public void setAuthorId(Long authorId) {
        this.authorId = authorId;
    }
    
    public String getAuthorName() {
        return authorName;
    }
    
    public void setAuthorName(String authorName) {
        this.authorName = authorName;
    }
    
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
    
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
