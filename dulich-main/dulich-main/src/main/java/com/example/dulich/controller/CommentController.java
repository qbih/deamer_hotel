package com.example.dulich.controller;

import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.dulich.entity.Article;
import com.example.dulich.entity.Comment;
import com.example.dulich.entity.Hotel;
import com.example.dulich.entity.User;
import com.example.dulich.repository.ArticleRepository;
import com.example.dulich.repository.CommentRepository;
import com.example.dulich.repository.HotelRepository;
import com.example.dulich.service.UserService;

@Controller
@RequestMapping("/comment")
public class CommentController {

    @Autowired
    private CommentRepository commentRepository;
      @Autowired
    private ArticleRepository articleRepository;
    
    @Autowired
    private HotelRepository hotelRepository;
    
    @Autowired
    private UserService userService;
      @PostMapping("/article/{id}")
    public String postArticleComment(@PathVariable Long id, 
                                    @RequestParam String content,
                                    @RequestParam(required = false) Long parentId,
                                    RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn phải đăng nhập để bình luận");
            return "redirect:/article/" + id;
        }
        
        try {
            Article article = articleRepository.findById(id).orElseThrow(() -> 
                    new IllegalArgumentException("Bài viết không tồn tại với id: " + id));
            
            Comment comment = new Comment();
            comment.setContent(content);
            comment.setUser(currentUser);
            comment.setArticle(article);
            comment.setStatus(1); // Mặc định hiển thị comment
            comment.setCreatedAt(LocalDateTime.now());
            comment.setUpdatedAt(LocalDateTime.now());
            
            if (parentId != null) {
                Comment parentComment = commentRepository.findById(parentId).orElse(null);
                comment.setParent(parentComment);
            }
            
            commentRepository.save(comment);
            
            redirectAttributes.addFlashAttribute("successMessage", "Bình luận đã được gửi thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi: " + e.getMessage());
        }
        
        return "redirect:/article/" + id;
    }    @PostMapping("/hotel/{id}")
    public String postHotelComment(@PathVariable Long id, 
                                 @RequestParam String content,
                                 @RequestParam(required = false) Integer rating,
                                 @RequestParam(required = false) Long parentId,
                                 RedirectAttributes redirectAttributes) {
        
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "Bạn phải đăng nhập để đánh giá");
            return "redirect:/hotel/" + id;
        }
        
        try {
            Hotel hotel = hotelRepository.findById(id).orElseThrow(() -> 
                    new IllegalArgumentException("Khách sạn không tồn tại với id: " + id));
            
            Comment comment = new Comment();
            comment.setContent(content);
            comment.setUser(currentUser);
            comment.setHotel(hotel);
            comment.setRating(rating); // Thêm đánh giá từ form
            comment.setStatus(1);
            comment.setCreatedAt(LocalDateTime.now());
            comment.setUpdatedAt(LocalDateTime.now());
            
            if (parentId != null) {
                Comment parentComment = commentRepository.findById(parentId).orElse(null);
                comment.setParent(parentComment);
            }
            
            commentRepository.save(comment);
            
            redirectAttributes.addFlashAttribute("successMessage", "Cảm ơn bạn đã đánh giá khách sạn!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Đã xảy ra lỗi: " + e.getMessage());
        }
        
        return "redirect:/hotel/" + id;
    }
    
    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() && 
            !authentication.getPrincipal().equals("anonymousUser")) {
            return (User) userService.findByUsername(authentication.getName());
        }
        return null;
    }
}
