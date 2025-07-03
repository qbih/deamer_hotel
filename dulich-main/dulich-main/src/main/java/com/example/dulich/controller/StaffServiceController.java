package com.example.dulich.controller;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
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
@RequestMapping("/staff")
@PreAuthorize("hasRole('STAFF')")
public class StaffServiceController {

    @Autowired
    private ServiceService serviceService;
    
    @GetMapping("/services")
    public String listServices(
            @RequestParam(required = false) String search,
            Model model) {
        try {
            List<Service> services;
              if (search != null && !search.isEmpty()) {
                // Use available methods from the service for search
                List<Service> allServices = serviceService.findAll();
                String searchLower = search.toLowerCase();
                services = allServices.stream()
                    .filter(service -> 
                        (service.getName() != null && service.getName().toLowerCase().contains(searchLower)) ||
                        (service.getDescription() != null && service.getDescription().toLowerCase().contains(searchLower)))
                    .toList();
            } else {
                // Get all services
                services = serviceService.findAll();
            }
            
            model.addAttribute("services", services);
            model.addAttribute("service_active", "active");
            model.addAttribute("title", "Quản lý dịch vụ - De'Amor Hotel");
            
            if (search != null) {
                model.addAttribute("search", search);
            }
            
            return "staff/service/index";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách dịch vụ: " + e.getMessage());
            return "error/error";
        }
    }
    
    @GetMapping("/services/view/{id}")
    public String viewService(@PathVariable Long id, Model model) {
        try {
            Optional<Service> serviceOpt = serviceService.findById(id);
            
            if (serviceOpt.isEmpty()) {
                model.addAttribute("error", "Không tìm thấy dịch vụ");
                return "redirect:/staff/services";
            }
            
            Service service = serviceOpt.get();
            model.addAttribute("service", service);
            model.addAttribute("service_active", "active");
            model.addAttribute("title", "Chi tiết dịch vụ - De'Amor Hotel");
            
            return "staff/service/view";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/staff/services";
        }
    }
    
    @GetMapping("/services/create")
    public String createService(Model model) {
        model.addAttribute("title", "Tạo dịch vụ mới - De'Amor Hotel");
        
        Service service = new Service();
        service.setStatus(1);
        service.setSortOrder(0);
        service.setIsIncluded(false);
        service.setIsAvailable24h(false);
        
        model.addAttribute("service", service);
        model.addAttribute("service_active", "active");
        
        return "staff/service/create";
    }
    
    @PostMapping("/services/create")
    public String storeService(@Valid @ModelAttribute Service service,
                         BindingResult result,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("title", "Tạo dịch vụ mới - De'Amor Hotel");
            model.addAttribute("service_active", "active");
            return "staff/service/create";
        }

        try {
            Service savedService = serviceService.save(service);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Dịch vụ đã được tạo thành công: " + savedService.getName());
            return "redirect:/staff/services";
        } catch (Exception e) {
            model.addAttribute("title", "Tạo dịch vụ mới - De'Amor Hotel");
            model.addAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            model.addAttribute("service_active", "active");
            return "staff/service/create";
        }
    }
    
    @GetMapping("/services/edit/{id}")
    public String editService(@PathVariable Long id, Model model) {
        model.addAttribute("title", "Chỉnh sửa dịch vụ - De'Amor Hotel");
        
        Optional<Service> serviceOpt = serviceService.findById(id);
        if (serviceOpt.isPresent()) {
            model.addAttribute("service", serviceOpt.get());
            model.addAttribute("service_active", "active");
            return "staff/service/edit";
        } else {
            return "redirect:/staff/services?error=not_found";
        }
    }
    
    @PostMapping("/services/edit/{id}")
    public String updateService(@PathVariable Long id,
                          @Valid @ModelAttribute Service service,
                          BindingResult result,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("title", "Chỉnh sửa dịch vụ - De'Amor Hotel");
            model.addAttribute("service_active", "active");
            return "staff/service/edit";
        }

        try {
            service.setId(id);
            serviceService.update(service);
            redirectAttributes.addFlashAttribute("successMessage", "Dịch vụ đã được cập nhật thành công!");
            return "redirect:/staff/services";
        } catch (Exception e) {
            model.addAttribute("title", "Chỉnh sửa dịch vụ - De'Amor Hotel");
            model.addAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            model.addAttribute("service_active", "active");
            return "staff/service/edit";
        }
    }
}
