package com.example.dulich.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class BookHotelDto {

    private Long id;
    @NotNull(message = "Hotel ID không được để trống")
    private Long hotelId;
    private String hotelName;    // Room selection instead of room type
    @NotNull(message = "Phòng không được để trống")
    private Long roomId;
    private String roomNumber;
    
    // Keep room type info for backward compatibility
    private Long roomTypeId;
    private String roomTypeName;

    private Long userId;
    private String userName;

    @NotBlank(message = "Tên khách hàng không được để trống")
    private String customerName;

    // Alternative field for the template
    private String fullName;

    @NotBlank(message = "Email không được để trống")
    @Email(message = "Email không đúng định dạng")
    private String email;

    @NotBlank(message = "Số điện thoại không được để trống")
    private String phone;
    private String address;    // Use String for form input (date format: yyyy-MM-dd)
    private String checkInDate;

    // Use String for form input (date format: yyyy-MM-dd)
    private String checkOutDate;

    @NotNull(message = "Số khách không được để trống")
    @Min(value = 1, message = "Số khách phải lớn hơn 0")
    private Integer numberOfGuests;

    // Alternative field for the template
    private Integer numberPeople;

    // Payment method field used in template
    private String paymentMethod;

    private Double pricePerNight;
    private Integer totalNights;
    private Double totalPrice;
    private String specialRequests;
    private Integer status;
    private String statusName;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long version;

    // Constructors
    public BookHotelDto() {
    }

    // Getters and Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getHotelId() {
        return hotelId;
    }

    public void setHotelId(Long hotelId) {
        this.hotelId = hotelId;
    }

    public String getHotelName() {
        return hotelName;
    }

    public void setHotelName(String hotelName) {
        this.hotelName = hotelName;
    }    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public String getRoomNumber() {
        return roomNumber;
    }

    public void setRoomNumber(String roomNumber) {
        this.roomNumber = roomNumber;
    }

    public Long getRoomTypeId() {
        return roomTypeId;
    }

    public void setRoomTypeId(Long roomTypeId) {
        this.roomTypeId = roomTypeId;
    }

    public String getRoomTypeName() {
        return roomTypeName;
    }

    public void setRoomTypeName(String roomTypeName) {
        this.roomTypeName = roomTypeName;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getFullName() {
        return fullName != null ? fullName : customerName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
        this.customerName = fullName; // Sync with customerName
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCheckInDate() {
        return checkInDate;
    }

    public void setCheckInDate(String checkInDate) {
        this.checkInDate = checkInDate;
    }    public LocalDateTime getCheckInDateTime() {
        if (checkInDate == null || checkInDate.isEmpty()) {
            return null;
        }
        try {
            // Convert string date to LocalDateTime at 14:00 (check-in time)
            return LocalDateTime.parse(checkInDate + "T14:00:00");
        } catch (Exception e) {
            return null;
        }
    }

    public void setCheckInDateTime(LocalDateTime checkInDateTime) {
        if (checkInDateTime != null) {
            this.checkInDate = checkInDateTime.toLocalDate().toString();
        } else {
            this.checkInDate = null;
        }
    }

    public String getCheckOutDate() {
        return checkOutDate;
    }

    public void setCheckOutDate(String checkOutDate) {
        this.checkOutDate = checkOutDate;
    }    public LocalDateTime getCheckOutDateTime() {
        if (checkOutDate == null || checkOutDate.isEmpty()) {
            return null;
        }
        try {
            // Convert string date to LocalDateTime at 12:00 (check-out time)
            return LocalDateTime.parse(checkOutDate + "T12:00:00");
        } catch (Exception e) {
            return null;
        }
    }

    public void setCheckOutDateTime(LocalDateTime checkOutDateTime) {
        if (checkOutDateTime != null) {
            this.checkOutDate = checkOutDateTime.toLocalDate().toString();
        } else {
            this.checkOutDate = null;
        }
    }

    public Integer getNumberOfGuests() {
        return numberOfGuests;
    }

    public void setNumberOfGuests(Integer numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }

    public Integer getNumberPeople() {
        return numberPeople != null ? numberPeople : numberOfGuests;
    }

    public void setNumberPeople(Integer numberPeople) {
        this.numberPeople = numberPeople;
        this.numberOfGuests = numberPeople; // Sync with numberOfGuests
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public void setPaymentMethod(String paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public Double getPricePerNight() {
        return pricePerNight;
    }

    public void setPricePerNight(Double pricePerNight) {
        this.pricePerNight = pricePerNight;
    }

    public Integer getTotalNights() {
        return totalNights;
    }

    public void setTotalNights(Integer totalNights) {
        this.totalNights = totalNights;
    }

    public Double getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }

    public String getSpecialRequests() {
        return specialRequests;
    }

    public void setSpecialRequests(String specialRequests) {
        this.specialRequests = specialRequests;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getStatusName() {
        return statusName;
    }

    public void setStatusName(String statusName) {
        this.statusName = statusName;
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

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }    // Calculate total price automatically
    public void calculateTotalPrice() {
        if (totalNights != null && pricePerNight != null) {
            this.totalPrice = totalNights * pricePerNight;
        }
    }
    
    @Override
    public String toString() {
        return "BookHotelDto{" +
                "id=" + id +
                ", hotelId=" + hotelId +
                ", hotelName='" + hotelName + '\'' +
                ", roomId=" + roomId +
                ", roomTypeName='" + roomTypeName + '\'' +
                ", userId=" + userId +
                ", customerName='" + customerName + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", checkInDate='" + checkInDate + '\'' +
                ", checkOutDate='" + checkOutDate + '\'' +
                ", numberOfGuests=" + numberOfGuests +
                ", totalPrice=" + totalPrice +
                ", status=" + status +
                '}';
    }
}
