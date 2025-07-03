package com.example.dulich.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "categories")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Category {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "c_name")
    private String name;

    @Column(name = "c_slug", unique = true)
    private String slug;

    @Column(name = "c_icon")
    private String icon;

    @Column(name = "c_title_seo")
    private String titleSeo;

    @Column(name = "c_description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "c_keyword_seo")
    private String keywordSeo;

    @Column(name = "c_active")
    private Integer active = 1;

    @Column(name = "c_home")
    private Integer home = 0;

    @Column(name = "c_user_id")
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "c_user_id", insertable = false, updatable = false)
    private User user;

    @OneToMany(mappedBy = "category")
    private List<Article> articles = new ArrayList<>();

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
