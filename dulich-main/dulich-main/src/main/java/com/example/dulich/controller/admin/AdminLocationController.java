package com.example.dulich.controller.admin;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.dulich.entity.Location;
import com.example.dulich.service.LocationService;
import com.example.dulich.utils.FileUploadUtil;
import com.example.dulich.utils.SlugGenerator;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/locations")
public class AdminLocationController {

    private static final Map<Integer, String> STATUS_MAP = new HashMap<>();
    static {
        STATUS_MAP.put(1, "Hoạt động");
        STATUS_MAP.put(0, "Không hoạt động");
    }

    @Autowired
    private LocationService locationService;

    @GetMapping("")
    public String index(
            @RequestParam(value = "l_name", required = false) String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {

        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Location> locations;

        if (name != null && !name.isEmpty()) {
            locations = locationService.searchByName(name, pageable);
            model.addAttribute("l_name", name);
        } else {
            locations = locationService.findPaginated(pageable);
        }

        model.addAttribute("locations", locations);
        model.addAttribute("status", STATUS_MAP);
        model.addAttribute("location_active", "active");

        return "admin/location/index";
    }

    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("location", new Location());
        model.addAttribute("status", STATUS_MAP);
        model.addAttribute("location_active", "active");
        return "admin/location/create";
    }

    @PostMapping(value = "/create")
    public String store(@Valid Location location,
            BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("status", STATUS_MAP);
            model.addAttribute("location_active", "active");
            return "admin/location/create";
        }

        try {
            // Generate slug
            String slug = SlugGenerator.toSlug(location.getName());
            location.setSlug(slug);

            // Handle image upload
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = StringUtils.cleanPath(imageFile.getOriginalFilename());
                String uploadDir = "uploads/locations/";
                String savedFileName = FileUploadUtil.saveFile(uploadDir, fileName, imageFile);
                location.setImage(uploadDir + savedFileName);
            }
            locationService.save(location);
            redirectAttributes.addFlashAttribute("success", "Lưu dữ liệu thành công");
            return "redirect:/admin/locations/create";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi lưu dữ liệu");
            return "redirect:/admin/locations/create";
        }
    }

    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Location> locationOpt = locationService.findById(id);

        if (locationOpt.isPresent()) {
            model.addAttribute("location", locationOpt.get());
            model.addAttribute("status", STATUS_MAP);
            model.addAttribute("location_active", "active");
            return "admin/location/edit";
        } else {
            redirectAttributes.addFlashAttribute("error", "Dữ liệu không tồn tại");
            return "redirect:/admin/locations";
        }
    }

    @PostMapping(value = "/update/{id}")
    public String update(@PathVariable Long id,
            @Valid Location location,
            BindingResult result,
            @RequestParam(value = "imageFile", required = false) MultipartFile imageFile,
            RedirectAttributes redirectAttributes,
            Model model) {

        if (result.hasErrors()) {
            model.addAttribute("status", STATUS_MAP);
            model.addAttribute("location_active", "active");
            return "admin/location/edit";
        }

        try {
            Optional<Location> existingLocationOpt = locationService.findById(id);
            if (!existingLocationOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Dữ liệu không tồn tại");
                return "redirect:/admin/locations";
            }

            Location existingLocation = existingLocationOpt.get();

            // Update fields
            existingLocation.setName(location.getName());
            existingLocation.setDescription(location.getDescription());
            existingLocation.setContent(location.getContent());
            existingLocation.setStatus(location.getStatus());

            // Generate slug if name changed
            if (!existingLocation.getName().equals(location.getName())) {
                String slug = SlugGenerator.toSlug(location.getName());
                existingLocation.setSlug(slug);
            }

            // Handle image upload
            if (imageFile != null && !imageFile.isEmpty()) {
                String fileName = StringUtils.cleanPath(imageFile.getOriginalFilename());
                String uploadDir = "uploads/locations/";
                String savedFileName = FileUploadUtil.saveFile(uploadDir, fileName, imageFile);
                existingLocation.setImage(uploadDir + savedFileName);
            }

            locationService.save(existingLocation);
            redirectAttributes.addFlashAttribute("success", "Lưu dữ liệu thành công");
            return "redirect:/admin/locations/edit/" + id;

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi lưu dữ liệu: " + e.getMessage());
            return "redirect:/admin/locations/edit/" + id;
        }
    }

    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Location> locationOpt = locationService.findById(id);
            if (!locationOpt.isPresent()) {
                redirectAttributes.addFlashAttribute("error", "Dữ liệu không tồn tại");
                return "redirect:/admin/locations";
            }

            locationService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Xóa thành công");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi không thể xóa dữ liệu");
        }

        return "redirect:/admin/locations";
    }
}
