package com.example.dulich.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.dulich.entity.BookHotel;
import com.example.dulich.entity.Hotel;
import com.example.dulich.entity.RoleName;
import com.example.dulich.entity.User;
import com.example.dulich.service.BookHotelService;
import com.example.dulich.service.HotelService;
import com.example.dulich.service.UserService;

@Controller
@RequestMapping("/staff")
@PreAuthorize("hasRole('STAFF')")
public class StaffController {
    @Autowired
    private UserService userService;

    @Autowired
    private BookHotelService bookHotelService;

    @Autowired
    private HotelService hotelService;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication authentication) {
        try {
            model.addAttribute("title", "Dashboard Nhân viên");            // Lấy thông tin staff hiện tại
            String currentUsername = authentication.getName();
            User currentStaff = userService.findByUsername(currentUsername);
            model.addAttribute("currentStaff", currentStaff);
            
            // Đếm tổng số khách sạn
            List<Hotel> activeHotels = hotelService.findByStatus(1);
            model.addAttribute("totalHotels", activeHotels.size());

            // Đếm số đơn đặt phòng đang chờ xác nhận
            model.addAttribute("pendingBookings", bookHotelService.countByStatus(BookHotel.STATUS_RECEIVED));

            // Đếm số đơn đặt phòng đã xác nhận
            model.addAttribute("confirmedBookings", bookHotelService.countByStatus(BookHotel.STATUS_CONFIRMED));

            // Tính tổng doanh thu
            model.addAttribute("totalRevenue", bookHotelService.getTotalRevenue());

            // Lấy các đơn đặt phòng gần đây
            Pageable pageable = PageRequest.of(0, 10);
            Page<BookHotel> bookingsPage = bookHotelService.findAllPaged(pageable);
            model.addAttribute("recentBookings", bookingsPage.getContent());            // Set active menu
            model.addAttribute("home_active", "active");

            return "staff/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi tải dashboard: " + e.getMessage());
            return "error/error";
        }
    }    // Booking management has been moved to StaffBookingController

    @GetMapping("/profile")
    public String profile(Model model, Authentication authentication) {
        try {
            String currentUsername = authentication.getName();
            User currentStaff = userService.findByUsername(currentUsername);

            model.addAttribute("user", currentStaff);
            model.addAttribute("profile_active", "active");
            model.addAttribute("title", "Thông tin cá nhân - De'Amor Hotel");

            return "staff/profile";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi tải thông tin cá nhân: " + e.getMessage());
            return "error/error";
        }
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @ModelAttribute User user,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin staff hiện tại
            String currentUsername = authentication.getName();
            User currentStaff = userService.findByUsername(currentUsername);

            // Cập nhật các trường được phép
            currentStaff.setFullName(user.getFullName());
            currentStaff.setEmail(user.getEmail());
            currentStaff.setPhone(user.getPhone());
            currentStaff.setAddress(user.getAddress());

            // Lưu thông tin cập nhật
            userService.updateUser(currentStaff);

            redirectAttributes.addFlashAttribute("success", "Cập nhật thông tin cá nhân thành công!");
            return "redirect:/staff/profile";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi cập nhật thông tin: " + e.getMessage());
            return "staff/profile";
        }
    }

    @GetMapping("/change-password")
    public String changePasswordForm(Model model) {
        model.addAttribute("title", "Đổi mật khẩu - De'Amor Hotel");
        return "staff/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {
        try {
            // Lấy thông tin staff hiện tại
            String currentUsername = authentication.getName();
            User currentStaff = userService.findByUsername(currentUsername);

            // Kiểm tra mật khẩu hiện tại
            if (!passwordEncoder.matches(currentPassword, currentStaff.getPassword())) {
                model.addAttribute("error", "Mật khẩu hiện tại không chính xác");
                return "staff/change-password";
            }

            // Kiểm tra mật khẩu mới và xác nhận mật khẩu
            if (!newPassword.equals(confirmPassword)) {
                model.addAttribute("error", "Mật khẩu mới và xác nhận mật khẩu không khớp");
                return "staff/change-password";
            }

            // Cập nhật mật khẩu mới
            currentStaff.setPassword(passwordEncoder.encode(newPassword));
            userService.updateUser(currentStaff);

            redirectAttributes.addFlashAttribute("success", "Đổi mật khẩu thành công!");
            return "redirect:/staff/dashboard";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi đổi mật khẩu: " + e.getMessage());
            return "staff/change-password";
        }
    }    @GetMapping("/customers")
    public String viewCustomers(
            @RequestParam(required = false) String search,
            Model model) {
        try {
            // Get all users
            List<User> allUsers = userService.findAll();
            
            // Filter to only show users with USER role
            List<User> customers = allUsers.stream()
                    .filter(user -> user.getRoles().stream()
                            .anyMatch(role -> role.getName() == RoleName.USER))
                    .toList();
            
            // Simple search implementation if search term is provided
            if (search != null && !search.isEmpty()) {
                String searchLower = search.toLowerCase();
                customers = customers.stream()
                        .filter(user -> 
                            (user.getUsername() != null && user.getUsername().toLowerCase().contains(searchLower)) ||
                            (user.getEmail() != null && user.getEmail().toLowerCase().contains(searchLower)) ||
                            (user.getFullName() != null && user.getFullName().toLowerCase().contains(searchLower)) ||
                            (user.getPhone() != null && user.getPhone().contains(searchLower))
                        )
                        .toList();
            }
            
            model.addAttribute("customers", customers);
            model.addAttribute("totalElements", customers.size());            
            if (search != null) {
                model.addAttribute("search", search);
            }
            
            // Set active menu
            model.addAttribute("customer_active", "active");
            model.addAttribute("title", "Quản lý khách hàng - De'Amor Hotel");
            
            return "staff/customer/index";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách khách hàng: " + e.getMessage());
            return "error/error";
        }
    }

    @GetMapping("/customers/view/{id}")
    public String viewCustomerDetail(@PathVariable Long id, Model model) {
        try {            // Lấy thông tin khách hàng
            Optional<User> customerOpt = userService.findById(id);
            if (customerOpt.isEmpty() || customerOpt.get().getRoles().stream()
                    .noneMatch(role -> role.getName() == RoleName.USER)) {
                model.addAttribute("error", "Không tìm thấy khách hàng");
                return "redirect:/staff/customers";
            }

            User customer = customerOpt.get();
            model.addAttribute("customer", customer);

            // Lấy danh sách đặt phòng của khách hàng
            List<BookHotel> customerBookings = bookHotelService.findByUserId(id);
            model.addAttribute("customerBookings", customerBookings);

            model.addAttribute("title", "Chi tiết khách hàng - De'Amor Hotel");
            model.addAttribute("customer_active", "active");

            return "staff/customer/view";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/staff/customers";
        }
    }
}
