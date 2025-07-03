package com.example.dulich.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.example.dulich.entity.BookHotel;
import com.example.dulich.entity.Hotel;
import com.example.dulich.repository.ArticleRepository;
import com.example.dulich.repository.BookHotelRepository;
import com.example.dulich.repository.HotelRepository;
import com.example.dulich.repository.UserRepository;
import com.example.dulich.security.UserPrincipal;
import com.example.dulich.service.EmailService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
        @Autowired
        private UserRepository userRepository;

        @Autowired
        private HotelRepository hotelRepository;

        @Autowired
        private BookHotelRepository bookHotelRepository;

        @Autowired
        private ArticleRepository articleRepository;        @Autowired
        private ObjectMapper objectMapper;

        @Autowired
        private EmailService emailService;

        // Mapping trạng thái BookTour với nhãn
        private String getStatusLabel(String status) {
                if (status == null)
                        return "Không xác định";

                switch (status) {
                        case "1":
                                return "Đã nhận";
                        case "2":
                                return "Đã xác nhận";
                        case "3":
                                return "Đã thanh toán";
                        case "4":
                                return "Đã hủy";
                        case "5":
                                return "Hoàn thành";
                        default:
                                return "Không xác định";
                }
        }

        @GetMapping({ "/", "/dashboard" })
        public String adminDashboard(
                        @RequestParam(value = "select_month", required = false) Integer month,
                        @RequestParam(value = "select_year", required = false) Integer year,
                        @AuthenticationPrincipal UserPrincipal userPrincipal,
                        Model model) throws JsonProcessingException {                model.addAttribute("title", "Quản lý khách sạn");
                model.addAttribute("currentUser", userPrincipal.getUser());

                // Lấy số lượng thống kê cơ bản - chuyển từ tour sang hotel
                long hotelCount = hotelRepository.count();
                long bookHotelCount = bookHotelRepository.count();
                long userCount = userRepository.count();
                long articleCount = articleRepository.count();

                model.addAttribute("hotel", hotelCount);
                model.addAttribute("bookHotel", bookHotelCount);
                model.addAttribute("user", userCount);
                model.addAttribute("article", articleCount);
                
                // Lấy danh sách khách sạn nổi bật (top 5 hotel)
                List<Hotel> topHotels = hotelRepository.findAll()
                                .stream()
                                .filter(h -> h.getStatus() == 1)
                                .limit(5)
                                .collect(java.util.stream.Collectors.toList());
                model.addAttribute("hotels", topHotels);

                // Thiết lập tháng và năm cho thống kê
                LocalDate today = LocalDate.now();
                int currentMonth = (month != null) ? month : today.getMonthValue();
                int currentYear = (year != null) ? year : today.getYear();
                model.addAttribute("currentMonth", currentMonth);
                model.addAttribute("currentYear", currentYear);
                // Xử lý dữ liệu cho biểu đồ
                YearMonth yearMonth = YearMonth.of(currentYear, currentMonth);
                int daysInMonth = yearMonth.lengthOfMonth();
                List<String> listDays = new ArrayList<>();
                for (int i = 1; i <= daysInMonth; i++) {
                        listDays.add(String.valueOf(i));
                }                // Tạo dữ liệu cho biểu đồ thống kê booking và doanh thu
                List<Integer> bookingStats = new ArrayList<>();
                List<Double> revenueStats = new ArrayList<>();
                // Lấy tất cả đặt phòng trong tháng để xử lý theo ngày
                LocalDateTime startOfMonth = LocalDate.of(currentYear, currentMonth, 1).atStartOfDay();
                LocalDateTime endOfMonth = startOfMonth.plusMonths(1);
                // Sử dụng findAll và filter
                List<BookHotel> allBookings = bookHotelRepository.findAll();
                List<BookHotel> monthlyBookings = allBookings.stream()
                                .filter(b -> b.getCreatedAt() != null &&
                                                b.getCreatedAt().isAfter(startOfMonth) &&
                                                b.getCreatedAt().isBefore(endOfMonth))
                                .collect(java.util.stream.Collectors.toList());                for (int i = 1; i <= daysInMonth; i++) {
                        LocalDateTime startOfDay = LocalDate.of(currentYear, currentMonth, i).atStartOfDay();
                        LocalDateTime endOfDay = startOfDay.plusDays(1);

                        // Lọc và tính toán số lượng đặt phòng và doanh thu theo ngày
                        List<BookHotel> dayBookings = monthlyBookings.stream()
                                        .filter(b -> {
                                                LocalDateTime bookTime = b.getCreatedAt();
                                                return bookTime != null &&
                                                                bookTime.isAfter(startOfDay) &&
                                                                bookTime.isBefore(endOfDay);
                                        })
                                        .collect(java.util.stream.Collectors.toList());

                        int bookingCount = dayBookings.size();

                        double revenue = dayBookings.stream()
                                        .mapToDouble(b -> b.getTotalPrice() != null ? b.getTotalPrice() : 0)
                                        .sum();

                        bookingStats.add(bookingCount);
                        revenueStats.add(revenue);
                }

                // Tạo dữ liệu để gửi đến view
                model.addAttribute("bookingStats", bookingStats);
                model.addAttribute("revenueStats", revenueStats);

                // Tính toán thống kê tổng quát
                Map<String, Object> summaryStats = new HashMap<>();
                summaryStats.put("totalBookings", monthlyBookings.size());
                summaryStats.put("totalRevenue", revenueStats.stream().mapToDouble(Double::doubleValue).sum());                summaryStats.put("averageBookingValue", 
                    monthlyBookings.size() > 0 ? 
                        monthlyBookings.stream().mapToDouble(b -> b.getTotalPrice() != null ? b.getTotalPrice() : 0).average().orElse(0) : 0);

                // Tính toán theo trạng thái
                Map<String, Integer> statusStats = new HashMap<>();
                for (BookHotel booking : allBookings) {
                        Integer status = booking.getStatus();
                        if (status != null) {
                                statusStats.put(getStatusLabel(status.toString()), 
                                        statusStats.getOrDefault(getStatusLabel(status.toString()), 0) + 1);
                        }
                }

                // Tạo dữ liệu cho biểu đồ tròn thể hiện trạng thái đặt phòng
                List<Map<String, Object>> statusChartData = new ArrayList<>();
                Map<Integer, Long> statusCounts = new HashMap<>();
                // Đếm số lượng đặt phòng theo trạng thái
                for (BookHotel booking : allBookings) {
                        Integer status = booking.getStatus();
                        if (status != null) {
                                statusCounts.put(status, statusCounts.getOrDefault(status, 0L) + 1);
                        }
                }

                // Tạo dữ liệu cho biểu đồ tròn
                for (Map.Entry<Integer, Long> entry : statusCounts.entrySet()) {
                        Map<String, Object> statusData = new HashMap<>();
                        statusData.put("name", getStatusLabel(entry.getKey().toString()));
                        statusData.put("y", entry.getValue());
                        statusData.put("selected", false);
                        statusData.put("sliced", false);
                        statusChartData.add(statusData);
                }

                model.addAttribute("listDay", objectMapper.writeValueAsString(listDays));
                model.addAttribute("arrRevenueTransactionMonth", objectMapper.writeValueAsString(bookingStats));
                model.addAttribute("arrRevenueTransactionMonthDefault", objectMapper.writeValueAsString(revenueStats));
                model.addAttribute("arrmoney", objectMapper.writeValueAsString(revenueStats));
                model.addAttribute("statusTransaction", objectMapper.writeValueAsString(statusChartData));

                return "admin/dashboard";
        }

        @GetMapping("/users-overview")
        public String manageUsers(Model model) {
                model.addAttribute("title", "Quản lý người dùng");
                model.addAttribute("users", userRepository.findAllWithRoles());
                return "admin/users";
        }        @GetMapping("/settings")
        public String adminSettings(Model model) {
                model.addAttribute("title", "Cài đặt hệ thống");
                return "admin/settings";
        }

        @PostMapping("/test-email")
        @ResponseBody
        public Map<String, String> testEmail(@RequestParam String testEmail) {
                Map<String, String> response = new HashMap<>();
                try {
                        String subject = "Test Email từ hệ thống Du Lịch";
                        String content = "Đây là email test để kiểm tra cấu hình email của hệ thống.\n\n" +
                                      "Nếu bạn nhận được email này, có nghĩa là cấu hình email đã hoạt động bình thường.\n\n" +
                                      "Thời gian gửi: " + LocalDateTime.now().toString();
                        
                        emailService.sendTestEmail(testEmail, subject, content);
                        response.put("status", "success");
                        response.put("message", "Email test đã được gửi thành công!");
                } catch (Exception e) {
                        response.put("status", "error");
                        response.put("message", "Lỗi gửi email: " + e.getMessage());
                }
                return response;
        }
}
