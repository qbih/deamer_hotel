package com.example.dulich.controller.admin;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.dulich.entity.RoomType;
import com.example.dulich.service.RoomTypeService;
import com.example.dulich.utils.FileUploadUtil;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/room-types")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class RoomTypeController {

    @Autowired
    private RoomTypeService roomTypeService;

    @GetMapping
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Integer status,
            @RequestParam(required = false) String bedType,
            Model model) {

        model.addAttribute("title", "Quản lý loại phòng");

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<RoomType> roomTypes = roomTypeService.findAllPaged(pageable);

        model.addAttribute("roomTypes", roomTypes);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", roomTypes.getTotalPages());
        model.addAttribute("totalElements", roomTypes.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        
        // Filter parameters
        model.addAttribute("status", status);
        model.addAttribute("bedType", bedType);

        // Data for filters  
        model.addAttribute("bedTypes", List.of("Single", "Double", "Queen", "King", "Twin"));

        return "admin/room_type/index";
    }

    @GetMapping("/view/{id}")
    public String view(@PathVariable Long id, Model model) {
        model.addAttribute("title", "Chi tiết loại phòng");
        
        Optional<RoomType> roomTypeOpt = roomTypeService.findById(id);
        if (roomTypeOpt.isPresent()) {
            model.addAttribute("roomType", roomTypeOpt.get());
            return "admin/room_type/view";
        } else {
            return "redirect:/admin/room-types?error=not_found";
        }
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("title", "Tạo loại phòng mới");
        
        RoomType roomType = new RoomType();
        roomType.setStatus(1);
        roomType.setMaxCapacity(2);
        
        model.addAttribute("roomType", roomType);
        model.addAttribute("bedTypes", List.of("Single", "Double", "Queen", "King", "Twin"));
        
        return "admin/room_type/create";
    }    @PostMapping(value = "/create", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String store(@Valid @ModelAttribute RoomType roomType,
                       BindingResult result,
                       @RequestParam(value = "image_roomtype", required = false) MultipartFile[] imageFiles,
                       RedirectAttributes redirectAttributes,
                       Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("title", "Tạo loại phòng mới");
            model.addAttribute("bedTypes", List.of("Single", "Double", "Queen", "King", "Twin"));
            return "admin/room_type/create";
        }

        try {
            // Handle image uploads
            if (imageFiles != null && imageFiles.length > 0) {
                StringBuilder imagePathsBuilder = new StringBuilder();
                String uploadDir = "uploads/room/";
                
                for (int i = 0; i < imageFiles.length; i++) {
                    MultipartFile file = imageFiles[i];
                    if (!file.isEmpty()) {
                        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
                        String savedFileName = FileUploadUtil.saveFile(uploadDir, fileName, file);
                        String fullPath = "/" + uploadDir + savedFileName;
                        
                        // Add comma separator for multiple images
                        if (i > 0) {
                            imagePathsBuilder.append(",");
                        }
                        imagePathsBuilder.append(fullPath);
                    }
                }
                
                if (imagePathsBuilder.length() > 0) {
                    roomType.setImages(imagePathsBuilder.toString());
                }
            }

            RoomType savedRoomType = roomTypeService.save(roomType);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Loại phòng đã được tạo thành công: " + savedRoomType.getName());
            return "redirect:/admin/room-types";
        } catch (Exception e) {
            model.addAttribute("title", "Tạo loại phòng mới");
            model.addAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            model.addAttribute("bedTypes", List.of("Single", "Double", "Queen", "King", "Twin"));
            return "admin/room_type/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("title", "Chỉnh sửa loại phòng");
        
        Optional<RoomType> roomTypeOpt = roomTypeService.findById(id);
        if (roomTypeOpt.isPresent()) {
            model.addAttribute("roomType", roomTypeOpt.get());
            model.addAttribute("bedTypes", List.of("Single", "Double", "Queen", "King", "Twin"));
            return "admin/room_type/edit";
        } else {
            return "redirect:/admin/room-types?error=not_found";
        }
    }    @PostMapping(value = "/edit/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String update(@PathVariable Long id,
                        @Valid @ModelAttribute RoomType roomType,
                        BindingResult result,
                        @RequestParam(value = "image_roomtype", required = false) MultipartFile[] imageFiles,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("title", "Chỉnh sửa loại phòng");
            model.addAttribute("bedTypes", List.of("Single", "Double", "Queen", "King", "Twin"));
            return "admin/room_type/edit";
        }

        try {
            RoomType existingRoomType = roomTypeService.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Loại phòng không tồn tại"));

            // Handle image uploads
            if (imageFiles != null && imageFiles.length > 0 && !imageFiles[0].isEmpty()) {
                StringBuilder imagePathsBuilder = new StringBuilder();
                String uploadDir = "uploads/room/";
                
                for (int i = 0; i < imageFiles.length; i++) {
                    MultipartFile file = imageFiles[i];
                    if (!file.isEmpty()) {
                        String fileName = StringUtils.cleanPath(file.getOriginalFilename());
                        String savedFileName = FileUploadUtil.saveFile(uploadDir, fileName, file);
                        String fullPath = "/" + uploadDir + savedFileName;
                        
                        // Add comma separator for multiple images
                        if (i > 0) {
                            imagePathsBuilder.append(",");
                        }
                        imagePathsBuilder.append(fullPath);
                    }
                }
                
                if (imagePathsBuilder.length() > 0) {
                    roomType.setImages(imagePathsBuilder.toString());
                }
            } else {
                // Keep existing images if no new ones are uploaded
                roomType.setImages(existingRoomType.getImages());
            }

            roomType.setId(id);
            roomTypeService.update(roomType);
            redirectAttributes.addFlashAttribute("successMessage", "Loại phòng đã được cập nhật thành công!");
            return "redirect:/admin/room-types";
        } catch (Exception e) {
            model.addAttribute("title", "Chỉnh sửa loại phòng");
            model.addAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            model.addAttribute("bedTypes", List.of("Single", "Double", "Queen", "King", "Twin"));
            return "admin/room_type/edit";
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            roomTypeService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Loại phòng đã được xóa thành công!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi xóa: " + e.getMessage());
        }
        
        return "redirect:/admin/room-types";
    }
}
