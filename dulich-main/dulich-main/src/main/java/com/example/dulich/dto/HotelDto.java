package com.example.dulich.dto;

import java.time.LocalDateTime;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class HotelDto {
    
    private Long id;
    
    @NotBlank(message = "Tên khách sạn không được để trống")
    @Size(max = 255, message = "Tên khách sạn không được vượt quá 255 ký tự")
    private String name;
    
    private String image;
    
    private String albumImage;
    
    @NotBlank(message = "Địa chỉ không được để trống")
    private String address;
    
    private String phone;
    
    private Integer numberPeople;
    
    @NotNull(message = "Giá không được để trống")
    @DecimalMin(value = "0.0", inclusive = false, message = "Giá phải lớn hơn 0")
    private Double price;
    
    private Integer sale = 0;
    
    private String description;
    
    @NotBlank(message = "Nội dung không được để trống")
    private String content;
    
    private Integer status = 1;
    
    private LocalDateTime startDate;
    
    private LocalDateTime endDate;
    
    @NotNull(message = "Địa điểm không được để trống")
    private Long locationId;
    
    private String locationName;
    
    private Long userId;
    
    private String userName;
    
    private LocalDateTime createdAt;
    
    private LocalDateTime updatedAt;
    
    // Constructors
    public HotelDto() {}
    
    public HotelDto(String name, String address, Double price, Long locationId) {
        this.name = name;
        this.address = address;
        this.price = price;
        this.locationId = locationId;
    }
    
    // Getters and Setters
    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getImage() {
        return image;
    }
    
    public void setImage(String image) {
        this.image = image;
    }
    
    public String getAlbumImage() {
        return albumImage;
    }
    
    public void setAlbumImage(String albumImage) {
        this.albumImage = albumImage;
    }
    
    public String getAddress() {
        return address;
    }
    
    public void setAddress(String address) {
        this.address = address;
    }
    
    public String getPhone() {
        return phone;
    }
    
    public void setPhone(String phone) {
        this.phone = phone;
    }
    
    public Integer getNumberPeople() {
        return numberPeople;
    }
    
    public void setNumberPeople(Integer numberPeople) {
        this.numberPeople = numberPeople;
    }
    
    public Double getPrice() {
        return price;
    }
    
    public void setPrice(Double price) {
        this.price = price;
    }
    
    public Integer getSale() {
        return sale;
    }
    
    public void setSale(Integer sale) {
        this.sale = sale;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getContent() {
        return content;
    }
    
    public void setContent(String content) {
        this.content = content;
    }
    
    public Integer getStatus() {
        return status;
    }
    
    public void setStatus(Integer status) {
        this.status = status;
    }
    
    public LocalDateTime getStartDate() {
        return startDate;
    }
    
    public void setStartDate(LocalDateTime startDate) {
        this.startDate = startDate;
    }
    
    public LocalDateTime getEndDate() {
        return endDate;
    }
    
    public void setEndDate(LocalDateTime endDate) {
        this.endDate = endDate;
    }
    
    public Long getLocationId() {
        return locationId;
    }
    
    public void setLocationId(Long locationId) {
        this.locationId = locationId;
    }
    
    public String getLocationName() {
        return locationName;
    }
    
    public void setLocationName(String locationName) {
        this.locationName = locationName;
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
}
