package com.example.dulich.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;

public class CategoryDto {

    private Long id;

    @NotEmpty(message = "Tên danh mục không được để trống")
    @Size(max = 100, message = "Tên danh mục không được quá 100 ký tự")
    private String name;
    
    private String description;
    
    private Integer active = 1;
    
    private Integer home = 0;

    private String icon;
    
    private String titleSeo;
    
    private String keywordSeo;
    
    // Constructors
    public CategoryDto() {
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

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
    }

    public Integer getHome() {
        return home;
    }

    public void setHome(Integer home) {
        this.home = home;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public String getTitleSeo() {
        return titleSeo;
    }

    public void setTitleSeo(String titleSeo) {
        this.titleSeo = titleSeo;
    }

    public String getKeywordSeo() {
        return keywordSeo;
    }

    public void setKeywordSeo(String keywordSeo) {
        this.keywordSeo = keywordSeo;
    }
}
