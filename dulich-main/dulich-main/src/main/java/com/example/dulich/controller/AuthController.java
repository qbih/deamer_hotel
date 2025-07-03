package com.example.dulich.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.dulich.dto.LoginDto;
import com.example.dulich.dto.UserRegistrationDto;
import com.example.dulich.service.UserService;

import jakarta.validation.Valid;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String loginForm(@RequestParam(value = "error", required = false) String error,
            @RequestParam(value = "logout", required = false) String logout,
            Model model) {
        System.out.println("Login form accessed with error: " + error + ", logout: " + logout);
        if (error != null) {
            model.addAttribute("errorMessage", "Tên đăng nhập hoặc mật khẩu không đúng!");
        }

        if (logout != null) {
            model.addAttribute("successMessage", "Bạn đã đăng xuất thành công!");
        }

        // Add a LoginDto for form binding
        model.addAttribute("loginDto", new LoginDto());
        model.addAttribute("title", "Đăng nhập");

        // Return the login template directly from the root templates folder
        return "auth/login";
    }

    @GetMapping("/register")
    public String registrationForm(Model model) {
        model.addAttribute("user", new UserRegistrationDto());
        model.addAttribute("title", "Đăng ký");
        return "auth/register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute("user") UserRegistrationDto userDto,
            BindingResult result, Model model) {

        // Kiểm tra xem mật khẩu và xác nhận mật khẩu có khớp không
        if (!userDto.getPassword().equals(userDto.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.user", "Mật khẩu xác nhận không khớp");
        }

        // Kiểm tra xem username đã tồn tại chưa
        if (userService.existsByUsername(userDto.getUsername())) {
            result.rejectValue("username", "error.user", "Tên đăng nhập đã tồn tại");
        }

        // Kiểm tra xem email đã tồn tại chưa
        if (userService.existsByEmail(userDto.getEmail())) {
            result.rejectValue("email", "error.user", "Email đã được sử dụng");
        }

        if (result.hasErrors()) {
            model.addAttribute("title", "Đăng ký");
            return "auth/register";
        }

        try {
            userService.save(userDto);
            // Clear the form by creating a new empty DTO
            model.addAttribute("user", new UserRegistrationDto());
            model.addAttribute("successMessage", "Đăng ký thành công! Bạn có thể đăng nhập ngay bây giờ.");
        } catch (Exception e) {
            model.addAttribute("errorMessage", "Đã xảy ra lỗi khi đăng ký. Vui lòng thử lại sau.");
            return "auth/register";
        }

        return "auth/register";
    }
}
