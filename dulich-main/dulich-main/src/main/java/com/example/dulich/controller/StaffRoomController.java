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
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.dulich.entity.Hotel;
import com.example.dulich.entity.Room;
import com.example.dulich.entity.RoomType;
import com.example.dulich.repository.HotelRepository;
import com.example.dulich.service.FileUploadService;
import com.example.dulich.service.RoomService;
import com.example.dulich.service.RoomTypeService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/staff")
@PreAuthorize("hasRole('STAFF')")
public class StaffRoomController {
    @Autowired
    private RoomService roomService;

    @Autowired
    private RoomTypeService roomTypeService;
    
    @Autowired
    private HotelRepository hotelRepository;
    
    @Autowired
    private FileUploadService fileUploadService;

    @GetMapping("/rooms")
    public String listRooms(
            @RequestParam(required = false) String search,
            Model model) {
        try {
            List<Room> rooms;
            if (search != null && !search.isEmpty()) {
                // Use available methods to simulate search
                // Assuming rooms might have names in their toString representation
                List<Room> allRooms = roomService.findAll();
                String searchLower = search.toLowerCase();
                rooms = allRooms.stream()
                        .filter(room -> room.toString().toLowerCase().contains(searchLower))
                        .toList();
            } else {
                // Get all rooms
                rooms = roomService.findAll();
            }

            model.addAttribute("rooms", rooms);
            model.addAttribute("room_active", "active");
            model.addAttribute("title", "Quản lý phòng - De'Amor Hotel");

            if (search != null) {
                model.addAttribute("search", search);
            }

            return "staff/room/index";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách phòng: " + e.getMessage());
            return "error/error";
        }
    }

    @GetMapping("/rooms/view/{id}")
    public String viewRoom(@PathVariable Long id, Model model) {
        try {
            Optional<Room> roomOpt = roomService.findById(id);

            if (roomOpt.isEmpty()) {
                model.addAttribute("error", "Không tìm thấy phòng");
                return "redirect:/staff/rooms";
            }

            Room room = roomOpt.get();
            model.addAttribute("room", room);
            model.addAttribute("room_active", "active");
            model.addAttribute("title", "Chi tiết phòng - De'Amor Hotel");

            return "staff/room/view";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/staff/rooms";
        }
    }
    
    @GetMapping("/rooms/create")
    public String createRoom(@RequestParam(required = false) Long hotelId, Model model) {
        model.addAttribute("title", "Tạo phòng mới - De'Amor Hotel");
        
        Room room = new Room();
        if (hotelId != null) {
            Hotel hotel = hotelRepository.findById(hotelId).orElse(null);
            if (hotel != null) {
                room.setHotel(hotel);
            }
        }
        
        model.addAttribute("room", room);
        model.addAttribute("hotels", hotelRepository.findByStatus(1));
        model.addAttribute("roomTypes", roomTypeService.findByStatus(1));
        model.addAttribute("room_active", "active");
        
        return "staff/room/create";
    }
    
    @PostMapping("/rooms/create")
    public String storeRoom(@Valid @ModelAttribute Room room,
                       BindingResult result,
                       @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles,
                       RedirectAttributes redirectAttributes,
                       Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("title", "Tạo phòng mới - De'Amor Hotel");
            model.addAttribute("hotels", hotelRepository.findByStatus(1));
            model.addAttribute("roomTypes", roomTypeService.findByStatus(1));
            model.addAttribute("room_active", "active");
            return "staff/room/create";
        }

        try {
            // Upload và xử lý ảnh
            if (imageFiles != null && imageFiles.length > 0) {
                StringBuilder imagePathsBuilder = new StringBuilder();
                for (MultipartFile file : imageFiles) {
                    if (!file.isEmpty()) {
                        try {
                            String imagePath = fileUploadService.uploadImage(file, "images/room");
                            if (imagePath != null) {
                                if (imagePathsBuilder.length() > 0) {
                                    imagePathsBuilder.append(",");
                                }
                                imagePathsBuilder.append("/").append(imagePath);
                            }
                        } catch (Exception e) {
                            System.err.println("Không thể upload ảnh: " + e.getMessage());
                        }
                    }
                }
                room.setImages(imagePathsBuilder.toString());
            }

            Room savedRoom = roomService.save(room);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Phòng đã được tạo thành công: " + savedRoom.getRoomNumber());
            return "redirect:/staff/rooms";
        } catch (Exception e) {
            model.addAttribute("title", "Tạo phòng mới - De'Amor Hotel");
            model.addAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            model.addAttribute("hotels", hotelRepository.findByStatus(1));
            model.addAttribute("roomTypes", roomTypeService.findByStatus(1));
            model.addAttribute("room_active", "active");
            return "staff/room/create";
        }
    }
    
    @GetMapping("/rooms/edit/{id}")
    public String editRoom(@PathVariable Long id, Model model) {
        model.addAttribute("title", "Chỉnh sửa phòng - De'Amor Hotel");
        
        Optional<Room> roomOpt = roomService.findById(id);
        if (roomOpt.isPresent()) {
            model.addAttribute("room", roomOpt.get());
            model.addAttribute("hotels", hotelRepository.findByStatus(1));
            model.addAttribute("roomTypes", roomTypeService.findByStatus(1));
            model.addAttribute("room_active", "active");
            return "staff/room/edit";
        } else {
            return "redirect:/staff/rooms?error=not_found";
        }
    }
    
    @PostMapping("/rooms/edit/{id}")
    public String updateRoom(@PathVariable Long id,
                        @Valid @ModelAttribute Room room,
                        BindingResult result,
                        @RequestParam(value = "imageFiles", required = false) MultipartFile[] imageFiles,
                        @RequestParam(value = "keepExistingImages", required = false, defaultValue = "false") boolean keepExistingImages,
                        RedirectAttributes redirectAttributes,
                        Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("title", "Chỉnh sửa phòng - De'Amor Hotel");
            model.addAttribute("hotels", hotelRepository.findByStatus(1));
            model.addAttribute("roomTypes", roomTypeService.findByStatus(1));
            model.addAttribute("room_active", "active");
            return "staff/room/edit";
        }

        try {
            room.setId(id);
            
            // Lấy thông tin phòng hiện tại để giữ lại ảnh cũ nếu cần
            Room existingRoom = roomService.findById(id).orElse(null);
            String existingImages = "";
            if (existingRoom != null && keepExistingImages) {
                existingImages = existingRoom.getImages() != null ? existingRoom.getImages() : "";
            }
            
            // Upload và xử lý ảnh mới
            if (imageFiles != null && imageFiles.length > 0) {
                StringBuilder imagePathsBuilder = new StringBuilder();
                
                // Giữ lại ảnh cũ nếu được yêu cầu
                if (keepExistingImages && !existingImages.isEmpty()) {
                    imagePathsBuilder.append(existingImages);
                }
                
                for (MultipartFile file : imageFiles) {
                    if (!file.isEmpty()) {
                        try {
                            String imagePath = fileUploadService.uploadImage(file, "images/room");
                            if (imagePath != null) {
                                if (imagePathsBuilder.length() > 0) {
                                    imagePathsBuilder.append(",");
                                }
                                imagePathsBuilder.append("/").append(imagePath);
                            }
                        } catch (Exception e) {
                            System.err.println("Không thể upload ảnh: " + e.getMessage());
                        }
                    }
                }
                room.setImages(imagePathsBuilder.toString());
            } else if (keepExistingImages) {
                // Chỉ giữ lại ảnh cũ nếu không có ảnh mới
                room.setImages(existingImages);
            }
            
            roomService.update(room);
            redirectAttributes.addFlashAttribute("successMessage", "Phòng đã được cập nhật thành công!");
            return "redirect:/staff/rooms";
        } catch (Exception e) {
            model.addAttribute("title", "Chỉnh sửa phòng - De'Amor Hotel");
            model.addAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            model.addAttribute("hotels", hotelRepository.findByStatus(1));
            model.addAttribute("roomTypes", roomTypeService.findByStatus(1));
            model.addAttribute("room_active", "active");
            return "staff/room/edit";
        }
    }

    @GetMapping("/room-types")
    public String listRoomTypes(
            @RequestParam(required = false) String search,
            Model model) {
        try {
            List<RoomType> roomTypes;
            if (search != null && !search.isEmpty()) {
                // Use findByNameContaining which is available in RoomTypeService
                roomTypes = roomTypeService.findByNameContaining(search);
            } else {
                // Get all room types
                roomTypes = roomTypeService.findAll();
            }

            model.addAttribute("roomTypes", roomTypes);
            model.addAttribute("room_type_active", "active");
            model.addAttribute("title", "Quản lý loại phòng - De'Amor Hotel");

            if (search != null) {
                model.addAttribute("search", search);
            }

            return "staff/roomtype/index";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra khi tải danh sách loại phòng: " + e.getMessage());
            return "error/error";
        }
    }

    @GetMapping("/room-types/view/{id}")
    public String viewRoomType(@PathVariable Long id, Model model) {
        try {
            Optional<RoomType> roomTypeOpt = roomTypeService.findById(id);

            if (roomTypeOpt.isEmpty()) {
                model.addAttribute("error", "Không tìm thấy loại phòng");
                return "redirect:/staff/room-types";
            }

            RoomType roomType = roomTypeOpt.get();
            model.addAttribute("roomType", roomType);
            model.addAttribute("room_type_active", "active");
            model.addAttribute("title", "Chi tiết loại phòng - De'Amor Hotel");

            return "staff/roomtype/view";
        } catch (Exception e) {
            model.addAttribute("error", "Có lỗi xảy ra: " + e.getMessage());
            return "redirect:/staff/room-types";
        }
    }
    
    @GetMapping("/room-types/create")
    public String createRoomType(Model model) {
        model.addAttribute("title", "Tạo loại phòng mới - De'Amor Hotel");
        model.addAttribute("roomType", new RoomType());
        model.addAttribute("room_type_active", "active");
        return "staff/roomtype/create";
    }
      @PostMapping("/room-types/create")
    public String storeRoomType(@Valid @ModelAttribute RoomType roomType,
                         BindingResult result,
                         @RequestParam(value = "image_roomtype", required = false) MultipartFile[] imageFiles,
                         RedirectAttributes redirectAttributes,
                         Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("title", "Tạo loại phòng mới - De'Amor Hotel");
            model.addAttribute("room_type_active", "active");
            model.addAttribute("bedTypes", List.of("Single", "Double", "Queen", "King", "Twin"));
            return "staff/roomtype/create";
        }

        try {
            // Upload và xử lý ảnh
            if (imageFiles != null && imageFiles.length > 0) {
                StringBuilder imagePathsBuilder = new StringBuilder();
                for (MultipartFile file : imageFiles) {
                    if (!file.isEmpty()) {
                        try {
                            String imagePath = fileUploadService.uploadImage(file, "uploads/room");
                            if (imagePath != null) {
                                if (imagePathsBuilder.length() > 0) {
                                    imagePathsBuilder.append(",");
                                }
                                imagePathsBuilder.append("/").append(imagePath);
                            }
                        } catch (Exception e) {
                            System.err.println("Không thể upload ảnh: " + e.getMessage());
                        }
                    }
                }
                roomType.setImages(imagePathsBuilder.toString());
            }

            RoomType savedRoomType = roomTypeService.save(roomType);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Loại phòng đã được tạo thành công: " + savedRoomType.getName());
            return "redirect:/staff/room-types";
        } catch (Exception e) {
            model.addAttribute("title", "Tạo loại phòng mới - De'Amor Hotel");
            model.addAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            model.addAttribute("room_type_active", "active");
            return "staff/roomtype/create";
        }
    }
    
    @GetMapping("/room-types/edit/{id}")
    public String editRoomType(@PathVariable Long id, Model model) {
        model.addAttribute("title", "Chỉnh sửa loại phòng - De'Amor Hotel");
        
        Optional<RoomType> roomTypeOpt = roomTypeService.findById(id);
        if (roomTypeOpt.isPresent()) {
            model.addAttribute("roomType", roomTypeOpt.get());
            model.addAttribute("room_type_active", "active");
            return "staff/roomtype/edit";
        } else {
            return "redirect:/staff/room-types?error=not_found";
        }
    }
      @PostMapping("/room-types/edit/{id}")
    public String updateRoomType(@PathVariable Long id,
                          @Valid @ModelAttribute RoomType roomType,
                          BindingResult result,
                          @RequestParam(value = "image_roomtype", required = false) MultipartFile[] imageFiles,
                          @RequestParam(value = "keepExistingImages", required = false, defaultValue = "false") boolean keepExistingImages,
                          RedirectAttributes redirectAttributes,
                          Model model) {
        
        if (result.hasErrors()) {
            model.addAttribute("title", "Chỉnh sửa loại phòng - De'Amor Hotel");
            model.addAttribute("room_type_active", "active");
            model.addAttribute("bedTypes", List.of("Single", "Double", "Queen", "King", "Twin"));
            return "staff/roomtype/edit";
        }

        try {
            roomType.setId(id);
            
            // Lấy thông tin loại phòng hiện tại để giữ lại ảnh cũ nếu cần
            RoomType existingRoomType = roomTypeService.findById(id).orElse(null);
            String existingImages = "";
            if (existingRoomType != null && keepExistingImages) {
                existingImages = existingRoomType.getImages() != null ? existingRoomType.getImages() : "";
            }
            
            // Upload và xử lý ảnh mới
            if (imageFiles != null && imageFiles.length > 0) {
                StringBuilder imagePathsBuilder = new StringBuilder();
                
                // Giữ lại ảnh cũ nếu được yêu cầu
                if (keepExistingImages && !existingImages.isEmpty()) {
                    imagePathsBuilder.append(existingImages);
                }
                
                for (MultipartFile file : imageFiles) {
                    if (!file.isEmpty()) {
                        try {
                            String imagePath = fileUploadService.uploadImage(file, "uploads/room");
                            if (imagePath != null) {
                                if (imagePathsBuilder.length() > 0) {
                                    imagePathsBuilder.append(",");
                                }
                                imagePathsBuilder.append("/").append(imagePath);
                            }
                        } catch (Exception e) {
                            System.err.println("Không thể upload ảnh: " + e.getMessage());
                        }
                    }
                }
                roomType.setImages(imagePathsBuilder.toString());
            } else if (keepExistingImages) {
                // Chỉ giữ lại ảnh cũ nếu không có ảnh mới
                roomType.setImages(existingImages);
            }
            
            roomTypeService.update(roomType);
            redirectAttributes.addFlashAttribute("successMessage", "Loại phòng đã được cập nhật thành công!");
            return "redirect:/staff/room-types";
        } catch (Exception e) {
            model.addAttribute("title", "Chỉnh sửa loại phòng - De'Amor Hotel");
            model.addAttribute("errorMessage", "Có lỗi xảy ra: " + e.getMessage());
            model.addAttribute("room_type_active", "active");
            return "staff/roomtype/edit";
        }
    }
}
