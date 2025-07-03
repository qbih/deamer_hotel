package com.example.dulich.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.dulich.entity.Article;
import com.example.dulich.entity.Comment;
import com.example.dulich.repository.ArticleRepository;
import com.example.dulich.repository.CommentRepository;

@Controller
@RequestMapping("/article")
public class ArticleController {

    @Autowired
    private ArticleRepository articleRepository;

    @Autowired
    private CommentRepository commentRepository;

    private static final int PAGE_SIZE = 10;

    @GetMapping
    public String index(Model model,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() &&
                !auth.getPrincipal().equals("anonymousUser");
        System.out.println("Is Authenticated: " + isAuthenticated);
        model.addAttribute("isAuthenticated", isAuthenticated);
        try {
            Pageable pageable = PageRequest.of(page, PAGE_SIZE, Sort.by("createdAt").descending());
            Page<Article> articles;

            if (keyword != null && !keyword.isEmpty()) {
                articles = articleRepository.findByNameContainingAndActive(keyword, 1, pageable);
                System.out.println("Tìm bài viết theo từ khóa: " + keyword + ", kết quả: "
                        + (articles != null ? articles.getTotalElements() : "null"));
            } else {
                articles = articleRepository.findByActive(1, pageable);
                System.out.println("Tìm tất cả bài viết active, kết quả: "
                        + (articles != null ? articles.getTotalElements() : "null"));
            }

            // Kiểm tra chi tiết articles
            if (articles != null && articles.hasContent()) {
                System.out.println("Số lượng bài viết: " + articles.getContent().size());
                articles.getContent().forEach(article -> {
                    System.out.println("ID: " + article.getId() + ", Tên: " + article.getName() + ", Active: "
                            + article.getActive());
                });
            } else {
                System.out.println("Không có bài viết nào");
            }

            model.addAttribute("articles", articles);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", articles != null ? articles.getTotalPages() : 0);
            model.addAttribute("keyword", keyword);
        } catch (Exception e) {
            // Log the error
            System.out.println("Lỗi khi tải danh sách bài viết: " + e.getMessage());
            e.printStackTrace();
            // Add empty model attributes
            model.addAttribute("articles", new PageImpl<>(List.of()));
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 0);
            model.addAttribute("keyword", keyword);
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách bài viết. Vui lòng thử lại sau.");
        }

        return "articles/index";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        try {
            Article article = articleRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Bài viết không tồn tại với id: " + id));

            // Tăng lượt xem
            article.setView(article.getView() + 1);
            articleRepository.save(article);
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            boolean isAuthenticated = auth != null && auth.isAuthenticated() &&
                    !auth.getPrincipal().equals("anonymousUser");
            System.out.println("Is Authenticated: " + isAuthenticated);
            model.addAttribute("isAuthenticated", isAuthenticated);
            // Lấy các bài viết liên quan
            List<Article> relatedArticles = articleRepository.findTop4ByIdNotAndActiveOrderByCreatedAtDesc(id, 1);

            // Lấy comments cho bài viết
            List<Comment> comments = commentRepository.findByArticleIdAndStatus(id, 1);

            model.addAttribute("article", article);
            model.addAttribute("relatedArticles", relatedArticles != null ? relatedArticles : List.of());
            model.addAttribute("comments", comments != null ? comments : List.of());

            return "articles/detail";
        } catch (Exception e) {
            // Log the error
            e.printStackTrace();

            // Redirect to the article list with an error message
            return "redirect:/article?error=Không thể tìm thấy bài viết";
        }
    }

    @PostMapping("/{id}/comment")
    public String addComment(@PathVariable Long id,
            @RequestParam("content") String content,
            Principal principal) {
        // Kiểm tra người dùng đã đăng nhập
        if (principal == null) {
            return "redirect:/login";
        }
        // Lấy thông tin bài viết
        articleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Bài viết không tồn tại với id: " + id));

        // TODO: Lấy thông tin người dùng và tạo comment
        // Ví dụ:
        // User user = userRepository.findByUsername(principal.getName());
        // Comment comment = new Comment();
        // comment.setContent(content);
        // comment.setArticle(article);
        // comment.setUser(user);
        // comment.setStatus(1);
        // commentRepository.save(comment);

        return "redirect:/article/" + id;
    }
}
