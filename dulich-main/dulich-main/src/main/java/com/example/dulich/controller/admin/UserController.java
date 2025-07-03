package com.example.dulich.controller.admin;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.dulich.dto.UserDto;
import com.example.dulich.entity.Role;
import com.example.dulich.entity.User;
import com.example.dulich.service.RoleService;
import com.example.dulich.service.UserService;

import jakarta.validation.Valid;

@Controller("adminUserController")
@RequestMapping("/admin/users")
public class UserController {

    private static final int NUMBER_PAGINATION = 10;

    @Autowired
    private UserService userService;

    @Autowired
    private RoleService roleService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("user_active", "active");
        model.addAttribute("roles", roleService.findAll());
    }

    /**
     * Display a listing of the users.
     */
    @GetMapping("")
    public String index(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "email", required = false) String email,
            @RequestParam(value = "phone", required = false) String phone,
            @RequestParam(value = "user_id", required = false) Long userId,
            @RequestParam(value = "role_id", required = false) Long roleId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {
        
        Pageable pageable = PageRequest.of(page, NUMBER_PAGINATION, Sort.by("id").descending());
        
        Page<User> users = userService.findWithFilters(name, email, phone, userId, roleId, pageable);
        
        model.addAttribute("users", users);
        model.addAttribute("currentPage", page);
        
        return "admin/user/index";
    }

    /**
     * Show the form for creating a new user.
     */
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("user", new UserDto());
        return "admin/user/create";
    }

    /**
     * Store a newly created user in database.
     */    @PostMapping("/store")
    public String store(@Valid @ModelAttribute("user") UserDto userDto, 
                        BindingResult result,
                        @RequestParam(value = "images", required = false) MultipartFile imageFile,
                        RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "admin/user/create";
        }
        
        try {            // Create new user entity
            User user = new User();
            user.setUsername(userDto.getName()); // Sử dụng name từ DTO làm username
            user.setFullName(userDto.getName()); // Sử dụng name từ DTO làm fullName
            user.setEmail(userDto.getEmail());
            user.setPhone(userDto.getPhone());
            user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            user.setEnabled(userDto.getStatus() == 1); // Chuyển đổi status thành enabled boolean
            
            // Handle image upload if provided
            // Lưu ý: Entity User không có trường avatar, cần bổ sung nếu cần
            
            // Save user and assign role
            User savedUser = userService.save(user);
            if (savedUser != null && userDto.getRoleId() != null) {
                userService.assignRoleToUser(savedUser.getId(), userDto.getRoleId());
            }
            
            redirectAttributes.addFlashAttribute("success", "Thêm mới thành công");
            return "redirect:/admin/users";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi lưu dữ liệu");
            return "redirect:/admin/users/create";
        }
    }
    
    /**
     * Show the form for editing the specified user.
     */    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        User user = userService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
          UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getFullName()); // Sử dụng fullName từ entity
        userDto.setEmail(user.getEmail());
        userDto.setPhone(user.getPhone());
        userDto.setStatus(user.isEnabled() ? 1 : 0); // Chuyển đổi từ boolean enabled sang Integer status
        userDto.setAvatar(""); // Khởi tạo giá trị trống cho avatar
        
        // Get assigned role
        Role userRole = userService.getUserRole(id);
        if (userRole != null) {
            userDto.setRoleId(userRole.getId());
        }
        
        model.addAttribute("user", userDto);
        
        return "admin/user/edit";
    }
    
    /**
     * Update the specified user in database.
     */    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id, 
                        @Valid @ModelAttribute("user") UserDto userDto,
                        BindingResult result,
                        @RequestParam(value = "images", required = false) MultipartFile imageFile,
                        RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "admin/user/edit";
        }
        
        try {            User user = userService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid user Id:" + id));
            
            user.setFullName(userDto.getName()); // Sử dụng name từ DTO cho fullName
            user.setEmail(userDto.getEmail());
            user.setPhone(userDto.getPhone());
            user.setEnabled(userDto.getStatus() == 1); // Chuyển đổi status thành enabled boolean
            
            // Update password only if provided
            if (userDto.getPassword() != null && !userDto.getPassword().isEmpty()) {
                user.setPassword(passwordEncoder.encode(userDto.getPassword()));
            }
            
            // Handle image upload if provided
            // Lưu ý: Entity User không có trường avatar, cần bổ sung nếu cần
            
            // Save user
            User updatedUser = userService.save(user);
            
            // Update role if needed
            if (updatedUser != null && userDto.getRoleId() != null) {
                userService.updateUserRole(updatedUser.getId(), userDto.getRoleId());
            }
            
            redirectAttributes.addFlashAttribute("success", "Cập nhật thành công");
            return "redirect:/admin/users";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi cập nhật dữ liệu");
            return "redirect:/admin/users/edit/" + id;
        }
    }
    
    /**
     * Remove the specified user from database.
     */
    @GetMapping("/delete/{id}")
    public String destroy(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            userService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Xóa thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi xóa dữ liệu");
        }
        return "redirect:/admin/users";
    }
}
