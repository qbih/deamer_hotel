package com.example.dulich.controller.admin;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
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

import com.example.dulich.entity.Service;
import com.example.dulich.service.ServiceService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/services")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class ServiceController {

    @Autowired
    private ServiceService serviceService;

    @GetMapping
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) Boolean isIncluded,
            Model model) {

        model.addAttribute("title", "Quản lý dịch vụ");

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Service> services;

        if (status != null) {
            services = serviceService.findByStatus(status, pageable);
        } else {
            services = serviceService.findAllPaged(pageable);
        }

        model.addAttribute("services", services);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", services.getTotalPages());
        model.addAttribute("totalElements", services.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        
        // Filter parameters
        model.addAttribute("category", category);
        model.addAttribute("status", status);
        model.addAttribute("isIncluded", isIncluded);

        // Data for filters
        model.addAttribute("categories", serviceService.findDistinctCategories());

        return "admin/service/index";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("title", "Chi tiết dịch vụ");
        
        Optional<Service> serviceOpt = serviceService.findById(id);
        if (serviceOpt.isPresent()) {
            model.addAttribute("service", serviceOpt.get());
            return "admin/service/view";
        } else {
            return "redirect:/admin/services?error=not_found";
        }
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("title", "Tạo dịch vụ mới");
        
        Service service = new Service();
        service.setStatus(1);
        service.setSortOrder(0);
        service.setIsIncluded(false);
        service.setIsAvailable24h(false);
        
        model.addAttribute("service", service);
        model.addAttribute("categories", serviceService.findDistinctCategories());
        
        return "admin/service/create";
    }

    @PostMapping("/create")
    public String store(@Valid @ModelAttribute Service service,
                       BindingResult result,
                       RedirectAttributes redirectAttributes,
                       Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("title", "Tạo dịch vụ mới");
            model.addAttribute("categories", serviceService.findDistinctCategories());
            return "admin/service/create";
        }

        try {
            Service savedService = serviceService.save(service);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Dịch vụ đã được tạo thành công: " + savedService.getName());
            return "redirect:/admin/services";
        } catch (Exception e) {
            model.addAttribute("title", "Tạo dịch vụ mới");
            model.addAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            model.addAttribute("categories", serviceService.findDistinctCategories());
            return "admin/service/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("title", "Chỉnh sửa dịch vụ");
        
        Optional<Service> serviceOpt = serviceService.findById(id);
        if (serviceOpt.isPresent()) {
            model.addAttribute("service", serviceOpt.get());
            model.addAttribute("categories", serviceService.findDistinctCategories());
            return "admin/service/edit";
        } else {
            return "redirect:/admin/services?error=not_found";
        }
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute Service service,
                        BindingResult result,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("title", "Chỉnh sửa dịch vụ");
            model.addAttribute("categories", serviceService.findDistinctCategories());
            return "admin/service/edit";
        }

        try {
            service.setId(id);
            serviceService.update(service);
            redirectAttributes.addFlashAttribute("successMessage", "Dịch vụ đã được cập nhật thành công!");
            return "redirect:/admin/services";
        } catch (Exception e) {
            model.addAttribute("title", "Chỉnh sửa dịch vụ");
            model.addAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            model.addAttribute("categories", serviceService.findDistinctCategories());
            return "admin/service/edit";
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            serviceService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Dịch vụ đã được xóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi xóa: " + e.getMessage());
        }
        
        return "redirect:/admin/services";
    }
}
