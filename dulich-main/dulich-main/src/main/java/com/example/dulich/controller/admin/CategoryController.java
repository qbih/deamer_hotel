package com.example.dulich.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.dulich.dto.CategoryDto;
import com.example.dulich.entity.Category;
import com.example.dulich.service.CategoryService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/categories")
public class CategoryController {

    private static final int NUMBER_PAGINATION = 10;
    
    @Autowired
    private CategoryService categoryService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("category_active", "active");
        model.addAttribute("data_product_active", "active");
        model.addAttribute("parents", categoryService.getParentCategories());
        model.addAttribute("status", categoryService.getStatusList());
    }

    /**
     * Hiển thị danh sách các danh mục
     */
    @GetMapping("")
    public String index(
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, NUMBER_PAGINATION, Sort.by("id").descending());
        Page<Category> categories = categoryService.findAllPaged(pageable);
        
        model.addAttribute("categories", categories);
        
        return "admin/category/index";
    }

    /**
     * Hiển thị form tạo mới danh mục
     */
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("category", new CategoryDto());
        return "admin/category/create";
    }

    /**
     * Lưu danh mục mới vào database
     */
    @PostMapping("/store")
    public String store(
            @Valid @ModelAttribute("category") CategoryDto categoryDto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "admin/category/create";
        }
        
        try {
            categoryService.createOrUpdate(categoryDto);
            redirectAttributes.addFlashAttribute("success", "Lưu dữ liệu thành công");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi lưu dữ liệu");
            return "redirect:/admin/categories/create";
        }
    }

    /**
     * Hiển thị form chỉnh sửa danh mục
     */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Category category = categoryService.findById(id).orElse(null);
        
        if (category == null) {
            redirectAttributes.addFlashAttribute("error", "Dữ liệu không tồn tại");
            return "redirect:/admin/categories";
        }
        
        CategoryDto categoryDto = categoryService.convertToDto(category);
        model.addAttribute("category", categoryDto);
        
        return "admin/category/edit";
    }

    /**
     * Cập nhật danh mục trong database
     */
    @PostMapping("/update/{id}")
    public String update(
            @PathVariable Long id,
            @Valid @ModelAttribute("category") CategoryDto categoryDto,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "admin/category/edit";
        }
        
        try {
            categoryService.createOrUpdate(categoryDto, id);
            redirectAttributes.addFlashAttribute("success", "Lưu dữ liệu thành công");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi lưu dữ liệu");
            return "redirect:/admin/categories/edit/" + id;
        }
    }

    /**
     * Xóa danh mục khỏi database
     */
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Category category = categoryService.findById(id).orElse(null);
            
            if (category == null) {
                redirectAttributes.addFlashAttribute("error", "Dữ liệu không tồn tại");
                return "redirect:/admin/categories";
            }
            
            categoryService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Xóa thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi không thể xóa dữ liệu");
        }
        
        return "redirect:/admin/categories";
    }
}
