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
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.dulich.entity.Comment;
import com.example.dulich.service.ArticleService;
import com.example.dulich.service.CommentService;

@Controller("adminCommentController")
@RequestMapping("/admin/comments")
public class CommentController {

    private static final int NUMBER_PAGINATION = 15;
    
    private static final Map<Integer, String> STATUS = new HashMap<Integer, String>() {{
        put(1, "Hiển thị");
        put(0, "Ẩn");
    }};

    @Autowired
    private CommentService commentService;
    
    @Autowired
    private ArticleService articleService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("comment_active", "active");
        model.addAttribute("status", STATUS);
        model.addAttribute("articles", articleService.findAll());
    }    /**
     * Hiển thị danh sách bình luận.
     */
    @GetMapping("")
    public String index(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "article_id", required = false) Long articleId,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, NUMBER_PAGINATION, Sort.by("id").descending());
        
        Page<Comment> comments = commentService.findWithFilters(null, articleId, null, status, pageable);
        
        model.addAttribute("comments", comments);
        model.addAttribute("currentPage", page);
        
        return "admin/comment/index";
    }

    /**
     * Cập nhật trạng thái hiển thị/ẩn bình luận.
     */
    @GetMapping("/update-status/{id}/{status}")
    public String updateStatus(@PathVariable Long id, @PathVariable Integer status, RedirectAttributes redirectAttributes) {
        try {
            commentService.updateStatus(id, status);
            
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái thành công");
            return "redirect:/admin/comments";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi cập nhật trạng thái");
            return "redirect:/admin/comments";
        }
    }

    /**
     * Xóa bình luận.
     */
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Comment comment = commentService.findById(id).orElse(null);
        
        if (comment == null) {
            redirectAttributes.addFlashAttribute("error", "Bình luận không tồn tại");
            return "redirect:/admin/comments";
        }

        try {
            commentService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Xóa thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi không thể xóa dữ liệu");
        }
        
        return "redirect:/admin/comments";
    }
}
