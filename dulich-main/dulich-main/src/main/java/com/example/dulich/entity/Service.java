package com.example.dulich.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "services")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Service {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price")
    private Double price;    @Column(name = "category")
    private String category; // Room Service, Spa, Restaurant, Transport, etc.

    @Column(name = "is_included")
    private Boolean isIncluded = false; // Dịch vụ miễn phí hay tính phí

    @Column(name = "is_available_24h")
    private Boolean isAvailable24h = false;

    @Column(name = "operating_hours")
    private String operatingHours; // Giờ hoạt động

    @Column(name = "status")
    private Integer status = 1; // 1: Active, 0: Inactive

    @Column(name = "sort_order")
    private Integer sortOrder = 0;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;    // Remove bidirectional relationship to avoid ConcurrentModificationException
    // @ManyToMany(mappedBy = "services")
    // private Set<Hotel> hotels = new HashSet<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    // Convenience methods
    public String getStatusText() {
        return status == 1 ? "Hoạt động" : "Không hoạt động";
    }

    public boolean isActive() {
        return status == 1;
    }

    public String getPriceText() {
        if (isIncluded) {
            return "Miễn phí";
        }
        return price != null ? String.format("%,.0f VNĐ", price) : "Liên hệ";
    }
}
