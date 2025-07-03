package com.example.dulich.entity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.CascadeType;
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
import lombok.ToString;

@Entity
@Table(name = "locations")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"hotels", "user"})
public class Location {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "l_name", nullable = false)
    private String name;

    @Column(name = "l_slug", unique = true)
    private String slug;

    @Column(name = "l_image")
    private String image;

    @Column(name = "l_description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "l_content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "l_status")
    private Integer status = 1;

    @Column(name = "l_user_id")
    private Long userId;

    @ManyToOne
    @JoinColumn(name = "l_user_id", insertable = false, updatable = false)
    private User user;    @OneToMany(mappedBy = "location", cascade = CascadeType.ALL)
    private List<Hotel> hotels = new ArrayList<>();

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
