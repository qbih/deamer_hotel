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
@Table(name = "rooms")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"hotel", "roomType", "bookHotelRooms"})
public class Room {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "room_number", nullable = false)
    private String roomNumber;

    @ManyToOne
    @JoinColumn(name = "hotel_id", nullable = false)
    private Hotel hotel;

    @ManyToOne
    @JoinColumn(name = "room_type_id", nullable = false)
    private RoomType roomType;

    @Column(name = "floor_number")
    private Integer floorNumber;

    @Column(name = "capacity")
    private Integer capacity;

    @Column(name = "price_per_night")
    private Double pricePerNight;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "amenities", columnDefinition = "TEXT")
    private String amenities; // JSON string hoặc comma-separated

    @Column(name = "images", columnDefinition = "TEXT")
    private String images; // JSON string chứa danh sách đường dẫn ảnh

    @Column(name = "status")
    private Integer status = 1; // 1: Available, 2: Occupied, 3: Maintenance, 4: Out of Order

    @Column(name = "is_smoking_allowed")
    private Boolean isSmokingAllowed = false;

    @Column(name = "has_balcony")
    private Boolean hasBalcony = false;

    @Column(name = "has_sea_view")
    private Boolean hasSeaView = false;

    @Column(name = "has_city_view")
    private Boolean hasCityView = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt;    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "room", cascade = CascadeType.ALL)
    private Set<BookHotelRoom> bookHotelRooms = new HashSet<>();

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
        switch (status) {
            case 1: return "Có sẵn";
            case 2: return "Đã được đặt";
            case 3: return "Bảo trì";
            case 4: return "Hỏng hóc";
            default: return "Không xác định";
        }
    }

    public boolean isAvailable() {
        return status == 1;
    }
    
    // Backward compatibility method for templates
    public Double getPrice() {
        return this.pricePerNight;
    }
    
    public void setPrice(Double price) {
        this.pricePerNight = price;
    }
}
