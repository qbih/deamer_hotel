package com.example.dulich.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.dulich.entity.BookHotel;
import com.example.dulich.entity.User;
import com.example.dulich.security.UserPrincipal;
import com.example.dulich.service.BookHotelService;
import com.example.dulich.service.UserService;

@Controller
@RequestMapping("/user")
@PreAuthorize("hasAnyRole('USER', 'STAFF', 'ADMIN')")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private BookHotelService bookHotelService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/dashboard")
    public String userDashboard(@AuthenticationPrincipal UserPrincipal userPrincipal, Model model) {
        model.addAttribute("title", "Bảng điều khiển người dùng");
        model.addAttribute("currentUser", userPrincipal.getUser());
        return "user/dashboard";
    }

    @GetMapping("/profile")
    public String userProfile(@AuthenticationPrincipal UserPrincipal userPrincipal, Model model) {
        model.addAttribute("title", "Thông tin cá nhân");
        model.addAttribute("user", userPrincipal.getUser());
        return "user/profile";
    }

    @GetMapping("/bookings")
    public String userBookings(@AuthenticationPrincipal UserPrincipal userPrincipal, Model model) {
        model.addAttribute("title", "Danh sách phòng đã đặt");
        User currentUser = userPrincipal.getUser();
        model.addAttribute("currentUser", currentUser);

        System.out.println("DEBUG: Loading bookings for user: " + currentUser.getUsername() + " (ID: "
                + currentUser.getId() + ")");        // Load bookings for current user
        List<BookHotel> allBookings = bookHotelService.findByUserId(currentUser.getId());
        System.out.println("DEBUG: Found " + allBookings.size() + " total bookings for user");

        // Show all bookings including cancelled ones so user can see history
        List<BookHotel> bookings = allBookings;
        System.out.println("DEBUG: Displaying all " + bookings.size() + " bookings (including cancelled ones)");

        model.addAttribute("bookings", bookings);
        return "user/bookings";
    }

    @GetMapping("/account")
    public String accountPage(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", currentUser);
        return "user/account";
    }

    @GetMapping("/change-password")
    public String changePasswordForm(Model model) {
        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", currentUser);
        return "user/change-password";
    }

    @PostMapping("/change-password")
    public String changePasswordSubmit(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            RedirectAttributes redirectAttributes) {

        User currentUser = getCurrentUser();
        if (currentUser == null) {
            return "redirect:/login";
        }

        // Kiểm tra mật khẩu hiện tại
        if (!passwordEncoder.matches(currentPassword, currentUser.getPassword())) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu hiện tại không chính xác");
            return "redirect:/user/change-password";
        }

        // Kiểm tra mật khẩu mới và xác nhận mật khẩu
        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("error", "Mật khẩu mới và xác nhận mật khẩu không khớp");
            return "redirect:/user/change-password";
        }

        // Cập nhật mật khẩu
        currentUser.setPassword(passwordEncoder.encode(newPassword));
        userService.updateUser(currentUser);

        redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công");
        return "redirect:/user/account";
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.isAuthenticated() &&
                !authentication.getPrincipal().equals("anonymousUser")) {
            return (User) userService.findByUsername(authentication.getName());
        }
        return null;
    }    @GetMapping("/bookings/cancel/{id}")
    public String cancelBooking(@PathVariable("id") Long id,
            @AuthenticationPrincipal UserPrincipal userPrincipal,
            RedirectAttributes redirectAttributes) {
        try {
            User currentUser = userPrincipal.getUser();
            
            // Verify booking exists and belongs to current user
            BookHotel booking = bookHotelService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Đặt phòng không tồn tại"));
            
            // Check if user owns this booking
            if (booking.getUser() == null || !booking.getUser().getId().equals(currentUser.getId())) {
                redirectAttributes.addFlashAttribute("error", "Bạn không có quyền hủy đặt phòng này");
                return "redirect:/user/bookings";
            }
            
            // Check if booking can be cancelled
            if (booking.getStatus() != BookHotel.STATUS_RECEIVED && booking.getStatus() != BookHotel.STATUS_CONFIRMED) {
                redirectAttributes.addFlashAttribute("error", "Không thể hủy đặt phòng này do trạng thái không phù hợp");
                return "redirect:/user/bookings";
            }
            
            // Update status to cancelled instead of deleting
            bookHotelService.updateStatus(id, BookHotel.STATUS_CANCELLED);
            redirectAttributes.addFlashAttribute("success", "Hủy đặt phòng thành công");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra khi hủy đặt phòng: " + e.getMessage());
        }
        
        return "redirect:/user/bookings";
    }
}
