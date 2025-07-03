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
@Table(name = "comments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "cm_content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "cm_name")
    private String name;

    @Column(name = "cm_email")
    private String email;

    @Column(name = "cm_status")
    private Integer status = 1;
    
    @Column(name = "cm_rating")
    private Integer rating; // Rating from 1 to 5 stars

    @ManyToOne
    @JoinColumn(name = "cm_user_id")
    private User user;

    @ManyToOne
    @JoinColumn(name = "cm_hotel_id")
    private Hotel hotel;
    
    @ManyToOne
    @JoinColumn(name = "cm_article_id")
    private Article article;
    
    @ManyToOne
    @JoinColumn(name = "cm_parent_id")
    private Comment parent;

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
