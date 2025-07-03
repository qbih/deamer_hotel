package com.example.dulich.controller.admin;

import java.util.HashMap;
import java.util.Map;

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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.dulich.dto.ArticleDto;
import com.example.dulich.entity.Article;
import com.example.dulich.service.ArticleService;
import com.example.dulich.service.CategoryService;

import jakarta.validation.Valid;

@Controller("adminArticleController")
@RequestMapping("/admin/articles")
public class ArticleController {

    private static final int NUMBER_PAGINATION = 10;
    
    private static final Map<Integer, String> STATUS = new HashMap<Integer, String>() {{
        put(1, "Hiển thị");
        put(0, "Ẩn");
    }};

    @Autowired
    private ArticleService articleService;    @Autowired
    private CategoryService categoryService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("article_active", "active");
        model.addAttribute("status", STATUS);
        model.addAttribute("categories", categoryService.findByActive(1)); // Chỉ lấy các danh mục đang kích hoạt
    }

    /**
     * Hiển thị danh sách bài viết.
     */
    @GetMapping("")
    public String index(
            @RequestParam(value = "title", required = false) String title,
            @RequestParam(value = "category_id", required = false) Long categoryId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, NUMBER_PAGINATION, Sort.by("id").descending());
        
        Page<Article> articles = articleService.findWithFilters(title, categoryId, status, pageable);
        
        model.addAttribute("articles", articles);
        model.addAttribute("currentPage", page);
        
        return "admin/article/index";
    }

    /**
     * Hiển thị form tạo mới bài viết.
     */
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("article", new ArticleDto());
        return "admin/article/create";
    }    /**
     * Lưu bài viết mới vào database.
     */
    @PostMapping("/store")
    public String store(@Valid @ModelAttribute("article") ArticleDto articleDto, 
                        BindingResult result,
                        @RequestParam(value = "image_article", required = false) MultipartFile imageFile,
                        RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "admin/article/create";
        }
        
        try {
            // Set author ID (you may want to get this from session/security context)
            // For now, setting a default author ID of 1 - update this based on your authentication
            if (articleDto.getAuthorId() == null) {
                articleDto.setAuthorId(1L); // Replace with actual logged-in user ID
            }
            
            articleService.createOrUpdate(articleDto, null, imageFile);
            
            redirectAttributes.addFlashAttribute("success", "Lưu dữ liệu thành công");
            return "redirect:/admin/articles";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi lưu dữ liệu: " + e.getMessage());
            return "redirect:/admin/articles/create";
        }
    }

    /**
     * Hiển thị form chỉnh sửa bài viết.
     */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        Article article = articleService.findById(id)
                .orElse(null);

        if (article == null) {
            return "redirect:/admin/articles";
        }
        
        ArticleDto articleDto = articleService.convertToDto(article);
        model.addAttribute("article", articleDto);
        
        return "admin/article/edit";
    }

    /**
     * Cập nhật thông tin bài viết.
     */
    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute("article") ArticleDto articleDto,
                        BindingResult result,
                        @RequestParam(value = "image_article", required = false) MultipartFile imageFile,
                        RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "admin/article/edit";
        }        try {
            // Set author ID if not already set
            if (articleDto.getAuthorId() == null) {
                articleDto.setAuthorId(1L); // Replace with actual logged-in user ID
            }
            
            articleDto.setId(id);
            articleService.createOrUpdate(articleDto, id, imageFile);
            
            redirectAttributes.addFlashAttribute("success", "Lưu dữ liệu thành công");
            return "redirect:/admin/articles";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi lưu dữ liệu: " + e.getMessage());
            return "redirect:/admin/articles/edit/" + id;
        }
    }

    /**
     * Xóa bài viết khỏi database.
     */
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Article article = articleService.findById(id).orElse(null);
        
        if (article == null) {
            redirectAttributes.addFlashAttribute("error", "Dữ liệu không tồn tại");
            return "redirect:/admin/articles";
        }

        try {
            articleService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Xóa thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi không thể xóa dữ liệu");
        }
        
        return "redirect:/admin/articles";
    }
}
