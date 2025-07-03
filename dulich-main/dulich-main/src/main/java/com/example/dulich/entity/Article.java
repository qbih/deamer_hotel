package com.example.dulich.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "articles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "a_name", nullable = false)
    private String name;

    @Column(name = "a_slug", unique = true)
    private String slug;

    @Column(name = "a_description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "a_content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "a_active")
    private Integer active = 1;

    @Column(name = "a_hot")
    private Integer hot = 0;

    @Column(name = "a_view")
    private Integer view = 0;

    @Column(name = "a_avatar")
    private String avatar;

    @Column(name = "a_title_seo")
    private String titleSeo;

    @Column(name = "a_keyword_seo")
    private String keywordSeo;

    @ManyToOne
    @JoinColumn(name = "a_category_id")
    private Category category;

    @ManyToOne
    @JoinColumn(name = "a_user_id")
    private User user;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
