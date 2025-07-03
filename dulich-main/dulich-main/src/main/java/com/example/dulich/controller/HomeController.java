package com.example.dulich.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.example.dulich.entity.Article;
import com.example.dulich.entity.Comment;
import com.example.dulich.entity.Hotel;
import com.example.dulich.entity.Location;
import com.example.dulich.repository.ArticleRepository;
import com.example.dulich.repository.CommentRepository;
import com.example.dulich.repository.HotelRepository;
import com.example.dulich.repository.LocationRepository;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class HomeController {
    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private CommentRepository commentRepository;

    @GetMapping({ "/", "/home" })
    public String home(Model model, HttpServletRequest request) {
        model.addAttribute("title", "Trang chủ - Đặt phòng khách sạn");
        model.addAttribute("httpServletRequest", request);        // Lấy dữ liệu từ database - tập trung vào khách sạn
        List<Location> locations = locationRepository.findByStatus(1);
        List<Article> articles = articleRepository.findTop6ByActiveOrderByIdDesc(1);
          // Fetch hotels safely to avoid ConcurrentModificationException
        List<Hotel> allHotels = hotelRepository.findByStatusSafe(1);
        List<Hotel> hotels = allHotels.stream().limit(8).toList();
        
        List<Comment> comments = commentRepository.findTop10ByStatusOrderByCreatedAtDesc(1);
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() &&
                !auth.getPrincipal().equals("anonymousUser");
        System.out.println("Is Authenticated: " + isAuthenticated);
        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("locations", locations);
        model.addAttribute("articles", articles);
        model.addAttribute("hotels", hotels);
        model.addAttribute("comments", comments);

        return "index";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() &&
                !auth.getPrincipal().equals("anonymousUser");
        System.out.println("Is Authenticated: " + isAuthenticated);
        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("title", "Liên hệ - Du lịch");
        return "contact/index";
    }

    @GetMapping("/about")
    public String about(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() &&
                !auth.getPrincipal().equals("anonymousUser");
        System.out.println("Is Authenticated: " + isAuthenticated);
        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("title", "Giới thiệu - Du lịch");

        List<Comment> comments = commentRepository.findTop10ByStatusOrderByCreatedAtDesc(1);
        model.addAttribute("comments", comments);

        return "about/index";
    }

    @GetMapping("/access-denied")
    public String accessDenied(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() &&
                !auth.getPrincipal().equals("anonymousUser");
        System.out.println("Is Authenticated: " + isAuthenticated);
        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("title", "Truy cập bị từ chối");
        return "access-denied";
    }
}
