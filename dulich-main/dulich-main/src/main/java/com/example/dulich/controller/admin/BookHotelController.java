package com.example.dulich.controller.admin;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
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

import com.example.dulich.dto.BookHotelDto;
import com.example.dulich.entity.BookHotel;
import com.example.dulich.repository.HotelRepository;
import com.example.dulich.repository.UserRepository;
import com.example.dulich.service.BookHotelService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/book-hotels")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class BookHotelController {

    @Autowired
    private BookHotelService bookHotelService;

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Long hotelId,
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String checkInFrom,
            @RequestParam(required = false) String checkInTo,
            Authentication authentication,
            Model model) {

        model.addAttribute("title", "Quản lý đặt phòng khách sạn");

        // Xác định vai trò người dùng
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        // Tạo Sort object
        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();

        Pageable pageable = PageRequest.of(page, size, sort);

        // Xử lý filter ngày
        LocalDateTime checkInFromDate = null;
        LocalDateTime checkInToDate = null;

        if (checkInFrom != null && !checkInFrom.isEmpty()) {
            checkInFromDate = LocalDateTime.parse(checkInFrom + "T00:00:00");
        }

        if (checkInTo != null && !checkInTo.isEmpty()) {
            checkInToDate = LocalDateTime.parse(checkInTo + "T23:59:59");
        }

        Page<BookHotel> bookHotels = bookHotelService.findWithFilters(
                hotelId, userId, status, checkInFromDate, checkInToDate, pageable);

        model.addAttribute("bookHotels", bookHotels);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", bookHotels.getTotalPages());
        model.addAttribute("totalElements", bookHotels.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        // Filter parameters
        model.addAttribute("hotelId", hotelId);
        model.addAttribute("userId", userId);
        model.addAttribute("status", status);
        model.addAttribute("checkInFrom", checkInFrom);
        model.addAttribute("checkInTo", checkInTo);

        // Data for filters
        model.addAttribute("hotels", hotelRepository.findByStatus(1));
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("statusList", bookHotelService.getStatusList());

        // Set active menu for admin and staff
        model.addAttribute("book_hotel_active", "active");

        return "admin/book_hotel/index";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("title", "Chi tiết đặt phòng");

        // Xác định vai trò người dùng
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        Optional<BookHotel> bookHotelOpt = bookHotelService.findById(id);
        System.out.println("BookHotel ID: " + bookHotelOpt.get());
        if (bookHotelOpt.isPresent()) {
            model.addAttribute("bookHotel", bookHotelOpt.get());
            model.addAttribute("book_hotel_active", "active");

            return "admin/book_hotel/view";
        } else {
            return "redirect:/admin/book-hotels?error=not_found";
        }
    }

    @GetMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String create(Model model) {
        model.addAttribute("title", "Tạo đặt phòng mới");
        model.addAttribute("bookHotelDto", new BookHotelDto());
        model.addAttribute("hotels", hotelRepository.findByStatus(1));
        model.addAttribute("users", userRepository.findAll());
        model.addAttribute("statusList", bookHotelService.getStatusList());

        // Set active menu
        model.addAttribute("book_hotel_active", "active");

        return "admin/book_hotel/create";
    }

    @PostMapping("/create")
    @PreAuthorize("hasRole('ADMIN')")
    public String store(@Valid @ModelAttribute BookHotelDto bookHotelDto,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("title", "Tạo đặt phòng mới");
            model.addAttribute("hotels", hotelRepository.findByStatus(1));
            model.addAttribute("users", userRepository.findAll());
            model.addAttribute("statusList", bookHotelService.getStatusList());

            // Set active menu
            model.addAttribute("book_hotel_active", "active");

            return "admin/book_hotel/create";
        }

        try {
            BookHotel savedBookHotel = bookHotelService.create(bookHotelDto);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Đặt phòng đã được tạo thành công với ID: " + savedBookHotel.getId());
            return "redirect:/admin/book-hotels";
        } catch (Exception e) {
            model.addAttribute("title", "Tạo đặt phòng mới");
            model.addAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            model.addAttribute("hotels", hotelRepository.findByStatus(1));
            model.addAttribute("users", userRepository.findAll());
            model.addAttribute("statusList", bookHotelService.getStatusList());
            return "admin/book_hotel/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("title", "Chỉnh sửa đặt phòng");

        // Xác định vai trò người dùng
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        Optional<BookHotel> bookHotelOpt = bookHotelService.findById(id);
        if (bookHotelOpt.isPresent()) {
            BookHotelDto bookHotelDto = bookHotelService.convertToDto(bookHotelOpt.get());
            model.addAttribute("bookHotelDto", bookHotelDto);
            model.addAttribute("bookHotel", bookHotelOpt.get());
            model.addAttribute("hotels", hotelRepository.findByStatus(1));
            model.addAttribute("users", userRepository.findAll());
            model.addAttribute("statusList", bookHotelService.getStatusList());

            // Set active menu
            model.addAttribute("book_hotel_active", "active");

            return "admin/book_hotel/edit";
        } else {
            return "redirect:/admin/book-hotels?error=not_found";
        }
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
            @Valid @ModelAttribute BookHotelDto bookHotelDto,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Authentication authentication,
            Model model) {

        // Xác định vai trò người dùng
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        if (result.hasErrors()) {
            model.addAttribute("title", "Chỉnh sửa đặt phòng");
            Optional<BookHotel> bookHotelOpt = bookHotelService.findById(id);
            if (bookHotelOpt.isPresent()) {
                model.addAttribute("bookHotel", bookHotelOpt.get());
            }
            model.addAttribute("hotels", hotelRepository.findByStatus(1));
            model.addAttribute("users", userRepository.findAll());
            model.addAttribute("statusList", bookHotelService.getStatusList());

            // Set active menu
            model.addAttribute("book_hotel_active", "active");

            return "admin/book_hotel/edit";
        }

        try {
            // Nếu người dùng là staff, chỉ cho phép cập nhật trạng thái
            if (!isAdmin) {
                // Kiểm tra booking có tồn tại không
                bookHotelService.findById(id)
                        .orElseThrow(() -> new RuntimeException("Đặt phòng không tồn tại"));

                // Cập nhật trạng thái booking
                bookHotelService.updateStatus(id, bookHotelDto.getStatus());
            } else {
                // Admin có thể cập nhật tất cả các trường
                bookHotelService.update(bookHotelDto, id);
            }

            redirectAttributes.addFlashAttribute("successMessage", "Đặt phòng đã được cập nhật thành công!");
            return "redirect:/admin/book-hotels";
        } catch (Exception e) {
            model.addAttribute("title", "Chỉnh sửa đặt phòng");
            model.addAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            Optional<BookHotel> bookHotelOpt = bookHotelService.findById(id);
            if (bookHotelOpt.isPresent()) {
                model.addAttribute("bookHotel", bookHotelOpt.get());
            }
            model.addAttribute("hotels", hotelRepository.findByStatus(1));
            model.addAttribute("users", userRepository.findAll());
            model.addAttribute("statusList", bookHotelService.getStatusList());

            // Set active menu
            model.addAttribute("book_hotel_active", "active");

            return "admin/book_hotel/edit";
        }
    }

    @PostMapping("/update-status/{id}")
    public String updateStatus(@PathVariable Long id,
            @RequestParam Integer status,
            RedirectAttributes redirectAttributes) {
        try {
            bookHotelService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("successMessage", "Trạng thái đã được cập nhật thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
        }

        return "redirect:/admin/book-hotels";
    }

    @GetMapping("/delete/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Chỉ admin mới được phép xóa đặt phòng
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            bookHotelService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Đặt phòng đã được xóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi xóa: " + e.getMessage());
        }

        return "redirect:/admin/book-hotels";
    }
}
