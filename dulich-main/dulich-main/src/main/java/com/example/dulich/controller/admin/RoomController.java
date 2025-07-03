package com.example.dulich.controller.admin;

import java.util.List;
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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.dulich.entity.Hotel;
import com.example.dulich.entity.Room;
import com.example.dulich.repository.HotelRepository;
import com.example.dulich.repository.RoomTypeRepository;
import com.example.dulich.service.RoomService;
import com.example.dulich.utils.FileUploadUtil;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/admin/rooms")
@PreAuthorize("hasAnyRole('ADMIN', 'STAFF')")
public class RoomController {

    @Autowired
    private RoomService roomService;    @Autowired
    private HotelRepository hotelRepository;    
    
    @Autowired
    private RoomTypeRepository roomTypeRepository;

    @GetMapping
    public String index(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            @RequestParam(required = false) Long hotelId,
            @RequestParam(required = false) Long roomTypeId,
            @RequestParam(required = false) Integer status,
            Model model) {

        model.addAttribute("title", "Quản lý phòng");

        Sort sort = sortDir.equalsIgnoreCase("desc") ? 
            Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        
        Pageable pageable = PageRequest.of(page, size, sort);
        Page<Room> rooms;

        if (hotelId != null) {
            rooms = roomService.findByHotelId(hotelId, pageable);
        } else {
            rooms = roomService.findAllPaged(pageable);
        }

        model.addAttribute("rooms", rooms);
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", rooms.getTotalPages());
        model.addAttribute("totalElements", rooms.getTotalElements());
        model.addAttribute("size", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        
        // Filter parameters
        model.addAttribute("hotelId", hotelId);
        model.addAttribute("roomTypeId", roomTypeId);
        model.addAttribute("status", status);

        // Data for filters
        model.addAttribute("hotels", hotelRepository.findByStatus(1));
        model.addAttribute("roomTypes", roomTypeRepository.findByStatus(1));

        return "admin/room/index";
    }    @GetMapping("/view/{id}")
    public String view(@PathVariable String id, Model model) {
        model.addAttribute("title", "Chi tiết phòng");
        
        // Check if ID is a valid number or if it contains file extension (like .jpg)
        if (id.contains(".")) {
            return "redirect:/admin/rooms?error=invalid_id";
        }
        
        try {
            Long roomId = Long.parseLong(id);
            Optional<Room> roomOpt = roomService.findById(roomId);
            if (roomOpt.isPresent()) {
                model.addAttribute("room", roomOpt.get());
                return "admin/room/view";
            } else {
                return "redirect:/admin/rooms?error=not_found";
            }
        } catch (NumberFormatException e) {
            return "redirect:/admin/rooms?error=invalid_id";
        }
    }

    @GetMapping("/create")
    public String create(@RequestParam(required = false) Long hotelId, Model model) {
        model.addAttribute("title", "Tạo phòng mới");
        
        Room room = new Room();
        if (hotelId != null) {
            Hotel hotel = hotelRepository.findById(hotelId).orElse(null);
            if (hotel != null) {
                room.setHotel(hotel);
            }
        }
        
        model.addAttribute("room", room);
        model.addAttribute("hotels", hotelRepository.findByStatus(1));
        model.addAttribute("roomTypes", roomTypeRepository.findByStatus(1));
        
        return "admin/room/create";
    }    @PostMapping("/create")
    public String store(@Valid @ModelAttribute Room room,
                       BindingResult result,
                       @RequestParam(value = "imageFiles", required = false) MultipartFile imageFile,
                       RedirectAttributes redirectAttributes,
                       Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("title", "Tạo phòng mới");
            model.addAttribute("hotels", hotelRepository.findByStatus(1));
            model.addAttribute("roomTypes", roomTypeRepository.findByStatus(1));
            return "admin/room/create";
        }        try {
            // Upload và xử lý ảnh
            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    String fileName = org.springframework.util.StringUtils.cleanPath(imageFile.getOriginalFilename());
                    String uploadDir = "uploads/room/";
                    String savedFileName = FileUploadUtil.saveFile(uploadDir, fileName, imageFile);
                    
                    room.setImages("/" + uploadDir + savedFileName);
                } catch (Exception e) {
                    // Log lỗi nhưng không dừng quá trình tạo phòng
                    System.err.println("Không thể upload ảnh: " + e.getMessage());
                }
            }

            Room savedRoom = roomService.save(room);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Phòng đã được tạo thành công: " + savedRoom.getRoomNumber());
            return "redirect:/admin/rooms";
        } catch (Exception e) {
            model.addAttribute("title", "Tạo phòng mới");
            model.addAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            model.addAttribute("hotels", hotelRepository.findByStatus(1));
            model.addAttribute("roomTypes", roomTypeRepository.findByStatus(1));
            return "admin/room/create";
        }
    }    @GetMapping("/edit/{id}")
    public String edit(@PathVariable String id, Model model) {
        model.addAttribute("title", "Chỉnh sửa phòng");
        
        // Check if ID is a valid number or if it contains file extension (like .jpg)
        if (id.contains(".")) {
            return "redirect:/admin/rooms?error=invalid_id";
        }
        
        try {
            Long roomId = Long.parseLong(id);
            Optional<Room> roomOpt = roomService.findById(roomId);
            if (roomOpt.isPresent()) {
                model.addAttribute("room", roomOpt.get());
                model.addAttribute("hotels", hotelRepository.findByStatus(1));
                model.addAttribute("roomTypes", roomTypeRepository.findByStatus(1));
                return "admin/room/edit";
            } else {
                return "redirect:/admin/rooms?error=not_found";
            }
        } catch (NumberFormatException e) {
            return "redirect:/admin/rooms?error=invalid_id";
        }
    }    @PostMapping("/edit/{id}")
    public String update(@PathVariable String id,
                        @Valid @ModelAttribute Room room,
                        BindingResult result,
                        @RequestParam(value = "imageFiles", required = false) MultipartFile imageFile,
                        @RequestParam(value = "keepExistingImages", required = false, defaultValue = "false") boolean keepExistingImages,
                        RedirectAttributes redirectAttributes,
                        Model model) {
                        
        Long roomId;
        try {
            roomId = Long.parseLong(id);
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID phòng không hợp lệ");
            return "redirect:/admin/rooms";
        }
        
        if (result.hasErrors()) {
            model.addAttribute("title", "Chỉnh sửa phòng");
            model.addAttribute("hotels", hotelRepository.findByStatus(1));
            model.addAttribute("roomTypes", roomTypeRepository.findByStatus(1));
            return "admin/room/edit";
        }        try {
            room.setId(roomId);
            
            // Lấy thông tin phòng hiện tại để giữ lại ảnh cũ nếu cần
            Room existingRoom = roomService.findById(roomId).orElse(null);
            String existingImages = "";
            if (existingRoom != null && keepExistingImages) {
                existingImages = existingRoom.getImages() != null ? existingRoom.getImages() : "";
            }
              // Upload và xử lý ảnh mới
            if (imageFile != null && !imageFile.isEmpty()) {
                try {
                    String fileName = org.springframework.util.StringUtils.cleanPath(imageFile.getOriginalFilename());
                    String uploadDir = "uploads/room/";
                    String savedFileName = FileUploadUtil.saveFile(uploadDir, fileName, imageFile);
                    
                    room.setImages("/" + uploadDir + savedFileName);
                } catch (Exception e) {
                    // Log lỗi nhưng không dừng quá trình cập nhật phòng
                    System.err.println("Không thể upload ảnh: " + e.getMessage());
                }
            } else if (keepExistingImages) {
                // Chỉ giữ lại ảnh cũ nếu không có ảnh mới
                room.setImages(existingImages);
            }
            
            roomService.update(room);
            redirectAttributes.addFlashAttribute("successMessage", "Phòng đã được cập nhật thành công!");
            return "redirect:/admin/rooms";
        } catch (Exception e) {
            model.addAttribute("title", "Chỉnh sửa phòng");
            model.addAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            model.addAttribute("hotels", hotelRepository.findByStatus(1));
            model.addAttribute("roomTypes", roomTypeRepository.findByStatus(1));
            return "admin/room/edit";
        }
    }    @GetMapping("/delete/{id}")
    public String delete(@PathVariable String id, RedirectAttributes redirectAttributes) {
        // Check if ID is a valid number or if it contains file extension (like .jpg)
        if (id.contains(".")) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID phòng không hợp lệ");
            return "redirect:/admin/rooms";
        }
        
        try {
            Long roomId = Long.parseLong(id);
            roomService.delete(roomId);
            redirectAttributes.addFlashAttribute("successMessage", "Phòng đã được xóa thành công!");
        } catch (NumberFormatException e) {
            redirectAttributes.addFlashAttribute("errorMessage", "ID phòng không hợp lệ");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Có lỗi xảy ra khi xóa: " + e.getMessage());
        }
        
        return "redirect:/admin/rooms";
    }

    @GetMapping("/hotel/{hotelId}")
    public String viewByHotel(@PathVariable Long hotelId, Model model) {
        model.addAttribute("title", "Danh sách phòng");
        
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new IllegalArgumentException("Khách sạn không tồn tại"));
        
        List<Room> rooms = roomService.findByHotelId(hotelId);
        
        model.addAttribute("hotel", hotel);
        model.addAttribute("rooms", rooms);
        model.addAttribute("totalRooms", rooms.size());
        model.addAttribute("availableRooms", rooms.stream().filter(Room::isAvailable).count());
        
        return "admin/room/by_hotel";
    }
}
