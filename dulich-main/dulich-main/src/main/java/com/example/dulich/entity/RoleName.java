package com.example.dulich.entity;

public enum RoleName {
    ADMIN("Quản trị viên"),
    STAFF("Nhân viên"),
    USER("Người dùng");
    
    private final String description;
    
    RoleName(String description) {
        this.description = description;
    }
    
    public String getDescription() {
        return description;
    }
}
