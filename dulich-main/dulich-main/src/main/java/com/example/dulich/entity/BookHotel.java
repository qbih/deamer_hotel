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
import jakarta.persistence.Transient;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "book_hotels")
@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString(exclude = { "hotel", "roomType", "room", "user", "bookHotelRooms" })
public class BookHotel {
    public static final int STATUS_RECEIVED = 1;
    public static final int STATUS_CONFIRMED = 2;
    public static final int STATUS_PAID = 3;
    public static final int STATUS_COMPLETED = 4;
    public static final int STATUS_CANCELLED = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "b_hotel_id")
    private Hotel hotel;
    @ManyToOne
    @JoinColumn(name = "b_room_type_id")
    private RoomType roomType;

    @ManyToOne
    @JoinColumn(name = "b_room_id")
    private Room room;

    @ManyToOne
    @JoinColumn(name = "b_user_id")
    private User user;

    @Column(name = "b_customer_name")
    private String customerName;

    @Column(name = "b_email")
    private String email;

    @Column(name = "b_phone")
    private String phone;

    @Column(name = "b_address")
    private String address;

    @Column(name = "b_check_in_date")
    private LocalDateTime checkInDate;
    @Column(name = "b_check_out_date")
    private LocalDateTime checkOutDate;

    @Column(name = "b_number_of_guests")
    private Integer numberOfGuests;

    @Column(name = "b_price_per_night")
    private Double pricePerNight;

    @Column(name = "b_total_nights")
    private Integer totalNights;

    @Column(name = "b_total_price")
    private Double totalPrice;

    @Column(name = "b_special_requests", columnDefinition = "TEXT")
    private String specialRequests;

    @Column(name = "b_status")
    private Integer status = STATUS_RECEIVED;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "bookHotel", cascade = CascadeType.ALL)
    private Set<BookHotelRoom> bookHotelRooms = new HashSet<>();

    @jakarta.persistence.Version
    @Column(name = "version")
    private Long version;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    @Transient
    public String getStatusName() {
        if (status == null)
            return "";
        switch (status) {
            case STATUS_RECEIVED:
                return "Đã nhận";
            case STATUS_CONFIRMED:
                return "Đã xác nhận";
            case STATUS_PAID:
                return "Đã thanh toán";
            case STATUS_COMPLETED:
                return "Hoàn thành";
            case STATUS_CANCELLED:
                return "Đã hủy";
            default:
                return "";
        }
    }
}
