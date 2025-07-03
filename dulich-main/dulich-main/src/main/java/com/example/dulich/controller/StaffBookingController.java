package com.example.dulich.controller;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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
@RequestMapping("/staff/bookings")
@PreAuthorize("hasRole('STAFF')")
public class StaffBookingController {

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
        
        // Tạo Sort object
        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
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
        
        // Set active menu for staff
        model.addAttribute("booking_active", "active");

        return "staff/booking/index";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("title", "Chi tiết đặt phòng");
        
        Optional<BookHotel> bookHotelOpt = bookHotelService.findById(id);
        if (bookHotelOpt.isPresent()) {
            model.addAttribute("bookHotel", bookHotelOpt.get());
            
            // Set active menu
            model.addAttribute("booking_active", "active");
            
            return "staff/booking/view";
        } else {
            return "redirect:/staff/bookings?error=not_found";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("title", "Cập nhật trạng thái đặt phòng");
        
        Optional<BookHotel> bookHotelOpt = bookHotelService.findById(id);
        if (bookHotelOpt.isPresent()) {
            BookHotelDto bookHotelDto = bookHotelService.convertToDto(bookHotelOpt.get());
            model.addAttribute("bookHotelDto", bookHotelDto);
            model.addAttribute("bookHotel", bookHotelOpt.get());
            model.addAttribute("hotels", hotelRepository.findByStatus(1));
            model.addAttribute("users", userRepository.findAll());
            model.addAttribute("statusList", bookHotelService.getStatusList());
            
            // Set active menu
            model.addAttribute("booking_active", "active");
            
            return "staff/booking/edit";
        } else {
            return "redirect:/staff/bookings?error=not_found";
        }
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute BookHotelDto bookHotelDto,
                        BindingResult result,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("title", "Cập nhật trạng thái đặt phòng");
            Optional<BookHotel> bookHotelOpt = bookHotelService.findById(id);
            if (bookHotelOpt.isPresent()) {
                model.addAttribute("bookHotel", bookHotelOpt.get());
            }
            model.addAttribute("statusList", bookHotelService.getStatusList());
            
            // Set active menu
            model.addAttribute("booking_active", "active");
            
            return "staff/booking/edit";
        }

        try {
            // Staff chỉ được phép thay đổi trạng thái
            bookHotelService.updateStatus(id, bookHotelDto.getStatus());
            
            redirectAttributes.addFlashAttribute("success", "Cập nhật trạng thái đặt phòng thành công!");
            return "redirect:/staff/bookings";
        } catch (Exception e) {
            model.addAttribute("title", "Cập nhật trạng thái đặt phòng");
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            Optional<BookHotel> bookHotelOpt = bookHotelService.findById(id);
            if (bookHotelOpt.isPresent()) {
                model.addAttribute("bookHotel", bookHotelOpt.get());
            }
            model.addAttribute("statusList", bookHotelService.getStatusList());
            
            // Set active menu
            model.addAttribute("booking_active", "active");
            
            return "staff/booking/edit";
        }
    }

    @PostMapping("/update-status/{id}")
    public String updateStatus(@PathVariable Long id,
                              @RequestParam Integer status,
                              RedirectAttributes redirectAttributes) {
        try {
            bookHotelService.updateStatus(id, status);
            redirectAttributes.addFlashAttribute("success", "Trạng thái đã được cập nhật thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
        }
        
        return "redirect:/staff/bookings";
    }
}
