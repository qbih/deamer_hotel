package com.example.dulich.entity;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "room_types")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"rooms"})
public class RoomType {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name; // Standard, Deluxe, Suite, VIP, etc.

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "base_price")
    private Double basePrice;

    @Column(name = "max_capacity")
    private Integer maxCapacity;

    @Column(name = "bed_type")
    private String bedType; // Single, Double, Queen, King

    @Column(name = "room_size")
    private Double roomSize; // Diện tích phòng (m²)

    @Column(name = "amenities", columnDefinition = "TEXT")
    private String amenities; // JSON string hoặc comma-separated

    @Column(name = "images", columnDefinition = "TEXT")
    private String images; // JSON string chứa danh sách đường dẫn ảnh

    @Column(name = "status")
    private Integer status = 1; // 1: Active, 0: Inactive

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "roomType", cascade = CascadeType.ALL)
    private Set<Room> rooms = new HashSet<>();

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
}
