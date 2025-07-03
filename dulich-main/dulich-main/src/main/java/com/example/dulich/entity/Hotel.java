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
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
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
@Table(name = "hotels")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = {"location", "user", "comments", "bookHotels", "rooms", "services"})
public class Hotel {

    // Constructor for safer fetching without collections (to avoid ConcurrentModificationException)
    public Hotel(Long id, String name, String image, String albumImage, String address, String phone, 
                Integer numberPeople, Double price, Integer sale, String description, String content, 
                Integer status, LocalDateTime startDate, LocalDateTime endDate, 
                LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.image = image;
        this.albumImage = albumImage;
        this.address = address;
        this.phone = phone;
        this.numberPeople = numberPeople;
        this.price = price;
        this.sale = sale;
        this.description = description;
        this.content = content;
        this.status = status;
        this.startDate = startDate;
        this.endDate = endDate;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "h_name", nullable = false)
    private String name;

    @Column(name = "h_image")
    private String image;

    @Column(name = "h_anbum_image", columnDefinition = "TEXT")
    private String albumImage;

    @Column(name = "h_address")
    private String address;

    @Column(name = "h_phone")
    private String phone;

    @Column(name = "h_number_people")
    private Integer numberPeople;

    @Column(name = "h_price")
    private Double price;

    @Column(name = "h_sale")
    private Integer sale;

    @Column(name = "h_description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "h_content", columnDefinition = "TEXT")
    private String content;

    @Column(name = "h_status")
    private Integer status = 1;

    @Column(name = "h_start_date")
    private LocalDateTime startDate;

    @Column(name = "h_end_date")
    private LocalDateTime endDate;

    @ManyToOne
    @JoinColumn(name = "h_location_id")
    private Location location;

    @ManyToOne
    @JoinColumn(name = "h_user_id")
    private User user;

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL)
    private Set<Comment> comments = new HashSet<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL)
    private Set<BookHotel> bookHotels = new HashSet<>();

    @OneToMany(mappedBy = "hotel", cascade = CascadeType.ALL)
    private Set<Room> rooms = new HashSet<>();

    @ManyToMany
    @JoinTable(
        name = "hotel_services",
        joinColumns = @JoinColumn(name = "hotel_id"),
        inverseJoinColumns = @JoinColumn(name = "service_id")
    )
    private Set<Service> services = new HashSet<>();

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
