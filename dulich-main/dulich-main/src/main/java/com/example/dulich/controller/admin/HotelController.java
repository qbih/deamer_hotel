package com.example.dulich.controller.admin;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.MediaType;
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

import com.example.dulich.dto.HotelDto;
import com.example.dulich.entity.Hotel;
import com.example.dulich.service.HotelService;
import com.example.dulich.service.LocationService;
import com.example.dulich.utils.FileUploadUtil;

import jakarta.validation.Valid;

@Controller("adminHotelController")
@RequestMapping("/admin/hotels")
public class HotelController {

    private static final int NUMBER_PAGINATION = 10;

    private static final Map<Integer, String> STATUS = new HashMap<Integer, String>() {
        {
            put(1, "Hiển thị");
            put(0, "Ẩn");
        }
    };
    @Autowired
    private HotelService hotelService;

    @Autowired
    private LocationService locationService;

    @ModelAttribute
    public void addAttributes(Model model) {
        model.addAttribute("hotel_active", "active");
        model.addAttribute("status", STATUS);
        model.addAttribute("locations", locationService.findAllActiveLocations());
    }

    /**
     * Hiển thị danh sách khách sạn.
     */
    @GetMapping("")
    public String index(
            @RequestParam(value = "name", required = false) String name,
            @RequestParam(value = "location_id", required = false) Long locationId,
            @RequestParam(value = "min_price", required = false) Double minPrice,
            @RequestParam(value = "max_price", required = false) Double maxPrice,
            @RequestParam(value = "status", required = false) Integer status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            Model model) {

        Pageable pageable = PageRequest.of(page, NUMBER_PAGINATION, Sort.by("id").descending());

        Page<Hotel> hotels = hotelService.findWithFilters(name, locationId, minPrice, maxPrice, status, pageable);

        model.addAttribute("hotels", hotels);
        model.addAttribute("currentPage", page);

        return "admin/hotel/index";
    }

    /**
     * Hiển thị form tạo mới khách sạn.
     */
    @GetMapping("/create")
    public String create(Model model) {
        model.addAttribute("hotel", new HotelDto());
        return "admin/hotel/create";
    }

    /**
     * Lưu khách sạn mới vào database.
     */
    @PostMapping(value = "/store", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String store(@Valid @ModelAttribute("hotel") HotelDto hotelDto,
            BindingResult result,
            @RequestParam(value = "image_hotel", required = false) MultipartFile imageHotel,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "admin/hotel/create";
        }

        try {
            // Handle image upload
            if (imageHotel != null && !imageHotel.isEmpty()) {
                String fileName = StringUtils.cleanPath(imageHotel.getOriginalFilename());
                String uploadDir = "uploads/hotel/";
                String savedFileName = FileUploadUtil.saveFile(uploadDir, fileName, imageHotel);
                hotelDto.setImage("/" + uploadDir + savedFileName);
            }

            hotelService.createOrUpdate(hotelDto);

            redirectAttributes.addFlashAttribute("success", "Lưu dữ liệu thành công");
            return "redirect:/admin/hotels";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi lưu dữ liệu: " + e.getMessage());
            return "redirect:/admin/hotels/create";
        }
    }

    /**
     * Hiển thị form chỉnh sửa khách sạn.
     */
    @GetMapping("/edit/{id}")
    public String edit(@PathVariable Long id, Model model) {
        Hotel hotel = hotelService.findById(id)
                .orElse(null);

        if (hotel == null) {
            return "redirect:/admin/hotels";
        }

        HotelDto hotelDto = hotelService.convertToDto(hotel);
        model.addAttribute("hotel", hotelDto);

        return "admin/hotel/edit";
    }

    /**
     * Cập nhật thông tin khách sạn.
     */
    @PostMapping(value = "/update/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String update(@PathVariable Long id,
            @Valid @ModelAttribute("hotel") HotelDto hotelDto,
            BindingResult result,
            @RequestParam(value = "image_hotel", required = false) MultipartFile imageHotel,
            RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "admin/hotel/edit";
        }

        try {
            Hotel existingHotel = hotelService.findById(id).orElse(null);

            if (existingHotel == null) {
                redirectAttributes.addFlashAttribute("error", "Dữ liệu không tồn tại");
                return "redirect:/admin/hotels";
            }

            // Handle image upload
            if (imageHotel != null && !imageHotel.isEmpty()) {
                String fileName = StringUtils.cleanPath(imageHotel.getOriginalFilename());
                String uploadDir = "/uploads/hotel/";
                String savedFileName = FileUploadUtil.saveFile(uploadDir, fileName, imageHotel);
                hotelDto.setImage(uploadDir + savedFileName);
            } else {
                // Keep existing image if no new image uploaded
                hotelDto.setImage(existingHotel.getImage());
            }

            hotelDto.setId(id);
            hotelService.createOrUpdate(hotelDto);

            redirectAttributes.addFlashAttribute("success", "Lưu dữ liệu thành công");
            return "redirect:/admin/hotels";

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi khi lưu dữ liệu: " + e.getMessage());
            return "redirect:/admin/hotels/edit/" + id;
        }
    }

    /**
     * Xóa khách sạn khỏi database.
     */
    @GetMapping("/delete/{id}")
    public String delete(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Hotel hotel = hotelService.findById(id).orElse(null);

        if (hotel == null) {
            redirectAttributes.addFlashAttribute("error", "Dữ liệu không tồn tại");
            return "redirect:/admin/hotels";
        }

        try {
            hotelService.delete(id);
            redirectAttributes.addFlashAttribute("success", "Xóa thành công");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Đã xảy ra lỗi không thể xóa dữ liệu");
        }

        return "redirect:/admin/hotels";
    }
}
