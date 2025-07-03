package com.example.dulich.controller.admin;

import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.dulich.entity.GroupPermission;
import com.example.dulich.entity.Permission;
import com.example.dulich.entity.Role;
import com.example.dulich.entity.RoleName;
import com.example.dulich.service.GroupPermissionService;
import com.example.dulich.service.PermissionService;
import com.example.dulich.service.RoleService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/roles")
public class AdminRoleController {

    @Autowired
    private RoleService roleService;
    
    @Autowired
    private GroupPermissionService groupPermissionService;
    
    @Autowired
    private PermissionService permissionService;

    @GetMapping("")
    public String index(Model model) {
        List<Role> roles = roleService.findAll();
        model.addAttribute("roles", roles);
        model.addAttribute("role_active", "active");
        return "admin/role/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        List<GroupPermission> permissionGroups = groupPermissionService.findAll();
        
        model.addAttribute("role", new Role());
        model.addAttribute("permissionGroups", permissionGroups);
        model.addAttribute("role_active", "active");
        
        return "admin/role/create";
    }

    @PostMapping("/create")
    public String store(@Valid Role role,
                       BindingResult result,
                       @RequestParam(value = "permissions", required = false) List<Long> permissionIds,
                       RedirectAttributes redirectAttributes,
                       Model model) {
        
        if (result.hasErrors()) {
            List<GroupPermission> permissionGroups = groupPermissionService.findAll();
            model.addAttribute("permissionGroups", permissionGroups);
            model.addAttribute("role_active", "active");
            return "admin/role/create";
        }
        
        try {
            // Convert display name to role name enum (sanitize)
            String safeName = role.getDisplayName().toUpperCase().replaceAll("\\s+", "_");
            try {
                role.setName(RoleName.valueOf(safeName));
            } catch (IllegalArgumentException e) {
                // If not a valid enum value, set as a default
                role.setName(RoleName.USER);
            }
            
            // Add permissions
            if (permissionIds != null && !permissionIds.isEmpty()) {
                Set<Permission> permissions = new HashSet<>();
                for (Long id : permissionIds) {
                    permissionService.findById(id).ifPresent(permissions::add);
                }
                role.setPermissions(permissions);
            }
            
            roleService.save(role);
            redirectAttributes.addFlashAttribute("success", "Thêm mới thành công");
            return "redirect:/admin/role/create";
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi lưu dữ liệu: " + e.getMessage());
            return "redirect:/admin/roles/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Role> roleOpt = roleService.findById(id);
        
        if (roleOpt.isPresent()) {
            Role role = roleOpt.get();
            List<GroupPermission> permissionGroups = groupPermissionService.findAll();
            
            // Get current permissions IDs for the role
            List<Long> listPermission = role.getPermissions().stream()
                .map(Permission::getId)
                .collect(Collectors.toList());
            
            model.addAttribute("role", role);
            model.addAttribute("permissionGroups", permissionGroups);
            model.addAttribute("listPermission", listPermission);
            model.addAttribute("role_active", "active");
            
            return "admin/role/edit";
        } else {
            redirectAttributes.addFlashAttribute("error", "Dữ liệu không tồn tại");
            return "redirect:/admin/roles";
        }
    }

    @PostMapping("/update/{id}")
    public String update(@PathVariable Long id,
                       @Valid Role role,
                       BindingResult result,
                       @RequestParam(value = "permissions", required = false) List<Long> permissionIds,
                       RedirectAttributes redirectAttributes,
                       Model model) {
        
        if (result.hasErrors()) {
            List<GroupPermission> permissionGroups = groupPermissionService.findAll();
            model.addAttribute("permissionGroups", permissionGroups);
            model.addAttribute("role_active", "active");
            return "admin/role/edit";
        }
        
        try {
            Optional<Role> existingRoleOpt = roleService.findById(id);
            
            if (!existingRoleOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Dữ liệu không tồn tại");
                return "redirect:/admin/roles";
            }
            
            Role existingRole = existingRoleOpt.get();
            
            // Update fields
            existingRole.setDisplayName(role.getDisplayName());
            existingRole.setDescription(role.getDescription());
            
            // Check if name needs to be changed
            if (!existingRole.getDisplayName().equals(role.getDisplayName())) {
                // Convert display name to role name enum (sanitize)
                String safeName = role.getDisplayName().toUpperCase().replaceAll("\\s+", "_");
                try {
                    existingRole.setName(RoleName.valueOf(safeName));
                } catch (IllegalArgumentException e) {
                    // Keep existing name if conversion fails
                }
            }
            
            // Update permissions
            Set<Permission> permissions = new HashSet<>();
            if (permissionIds != null && !permissionIds.isEmpty()) {
                for (Long permissionId : permissionIds) {
                    permissionService.findById(permissionId).ifPresent(permissions::add);
                }
            }
            existingRole.setPermissions(permissions);
            
            roleService.save(existingRole);
            redirectAttributes.addFlashAttribute("success", "Chỉnh sửa thành công");
            return "redirect:/admin/roles/edit/" + id;
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi lưu dữ liệu: " + e.getMessage());
            return "redirect:/admin/roles/edit/" + id;
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Role> roleOpt = roleService.findById(id);
            
            if (!roleOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Dữ liệu không tồn tại");
                return "redirect:/admin/roles";
            }
            
            // Don't allow deletion of system roles
            Role role = roleOpt.get();
            if (role.getName() == RoleName.ADMIN || role.getName() == RoleName.USER) {
                redirectAttributes.addFlashAttribute("error", "Không thể xóa vai trò hệ thống");
                return "redirect:/admin/roles";
            }
            
            roleService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Xóa thành công");
            
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi không thể xóa dữ liệu: " + e.getMessage());
        }
        
        return "redirect:/admin/roles";
    }
}
