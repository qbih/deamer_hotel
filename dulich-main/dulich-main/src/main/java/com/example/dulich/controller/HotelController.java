package com.example.dulich.controller;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.dulich.dto.BookHotelDto;
import com.example.dulich.entity.BookHotel;
import com.example.dulich.entity.Comment;
import com.example.dulich.entity.Hotel;
import com.example.dulich.entity.Room;
import com.example.dulich.entity.RoomType;
import com.example.dulich.entity.Service;
import com.example.dulich.entity.User;
import com.example.dulich.repository.CommentRepository;
import com.example.dulich.repository.HotelRepository;
import com.example.dulich.repository.LocationRepository;
import com.example.dulich.repository.UserRepository;
import com.example.dulich.service.BookHotelService;
import com.example.dulich.service.HotelService;
import com.example.dulich.service.RoomAvailabilityService;
import com.example.dulich.service.RoomService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;

@Controller
@RequestMapping("/hotel")
public class HotelController {

    @Autowired
    private HotelRepository hotelRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private LocationRepository locationRepository;

    @Autowired
    private BookHotelService bookHotelService;

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoomService roomService;
    @Autowired
    private HotelService hotelService;

    @Autowired
    private RoomAvailabilityService roomAvailabilityService;

    private static final int PAGE_SIZE = 9;

    @GetMapping
    public String index(Model model,
            @RequestParam(required = false) String keyHotel,
            @RequestParam(required = false) Long locationId,
            @RequestParam(required = false) String priceRange,
            @RequestParam(required = false) Integer capacity,
            @RequestParam(required = false) String roomType,
            @RequestParam(required = false) String[] amenities,
            @RequestParam(required = false) String[] features,
            @RequestParam(required = false, defaultValue = "latest") String sortBy,
            @RequestParam(defaultValue = "0") int page) {

        Pageable pageable = PageRequest.of(page, PAGE_SIZE, getSortOrder(sortBy)); // Thực hiện truy vấn dựa trên các
                                                                                   // tham số filter
        Page<Hotel> hotels;
        try {
            if (keyHotel != null && !keyHotel.isEmpty()) {
                hotels = hotelRepository.findByNameContainingAndStatus(keyHotel, 1, pageable);
            } else if (locationId != null) {
                hotels = hotelRepository.findByLocationIdAndStatus(locationId, 1, pageable);
            } else {
                hotels = hotelRepository.findByStatusOrderByIdDesc(1, pageable);
            }

            // Make sure no lazy loading is attempted after query execution
            hotels.getContent().size(); // Force initialization

        } catch (Exception e) {
            System.err.println("Error fetching hotels: " + e.getMessage());
            e.printStackTrace();
            hotels = Page.empty();
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() &&
                !auth.getPrincipal().equals("anonymousUser");

        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("hotels", hotels);
        model.addAttribute("locations", locationRepository.findAll());

        // Add search parameters for pagination links and form retention
        model.addAttribute("keyHotel", keyHotel);
        model.addAttribute("locationId", locationId);
        model.addAttribute("priceRange", priceRange);
        model.addAttribute("capacity", capacity);
        model.addAttribute("roomType", roomType);
        model.addAttribute("amenities", amenities);
        model.addAttribute("features", features);
        model.addAttribute("sortBy", sortBy);

        return "hotel/index";
    }

    private Sort getSortOrder(String sortBy) {
        switch (sortBy) {
            case "price-asc":
                return Sort.by("price").ascending();
            case "price-desc":
                return Sort.by("price").descending();
            case "name":
                return Sort.by("name").ascending();
            case "latest":
            default:
                return Sort.by("id").descending();
        }
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model) {
        // Load the hotel without eager loading services to avoid
        // ConcurrentModificationException
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khách sạn không tồn tại với id: " + id));

        // Copy essential properties to avoid accessing relationships that could cause
        // concurrency issues
        Hotel safeHotel = new Hotel(
                hotel.getId(), hotel.getName(), hotel.getImage(), hotel.getAlbumImage(),
                hotel.getAddress(), hotel.getPhone(), hotel.getNumberPeople(),
                hotel.getPrice(), hotel.getSale(), hotel.getDescription(), hotel.getContent(),
                hotel.getStatus(), hotel.getStartDate(), hotel.getEndDate(),
                hotel.getCreatedAt(), hotel.getUpdatedAt());

        // Set location manually if needed for related hotels lookup
        if (hotel.getLocation() != null) {
            safeHotel.setLocation(hotel.getLocation());
        } // Lấy danh sách đánh giá của khách sạn này
        List<Comment> comments = new ArrayList<>();
        try {
            List<Comment> foundComments = commentRepository.findByHotelIdAndStatus(id, 1);
            if (foundComments != null) {
                comments.addAll(foundComments);
            }
        } catch (Exception e) {
            System.err.println("Error loading comments for hotel " + id + ": " + e.getMessage());
            e.printStackTrace();
        }
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() &&
                !auth.getPrincipal().equals("anonymousUser");

        // Lấy danh sách phòng của khách sạn - sử dụng service thay vì truy cập trực
        // tiếp
        List<Room> availableRooms = new ArrayList<>();
        try {
            List<Room> rooms = roomService.findAvailableRoomsByHotelId(id);
            if (rooms != null) {
                availableRooms.addAll(rooms);
            }
        } catch (Exception e) {
            System.err.println("Error loading available rooms for hotel " + id + ": " + e.getMessage());
            e.printStackTrace();
        } // Lấy các loại phòng khác nhau - tạo Set mới để tránh
          // ConcurrentModificationException
        Set<RoomType> roomTypes = new HashSet<>();
        if (availableRooms != null && !availableRooms.isEmpty()) {
            // Tạo danh sách copy để tránh ConcurrentModificationException
            List<Room> roomsCopy = new ArrayList<>(availableRooms);
            for (Room room : roomsCopy) {
                if (room != null && room.getRoomType() != null) {
                    // Tạo copy của RoomType để tránh lazy loading issues
                    RoomType roomType = room.getRoomType();
                    RoomType safeCopy = new RoomType();
                    safeCopy.setId(roomType.getId());
                    safeCopy.setName(roomType.getName());
                    safeCopy.setDescription(roomType.getDescription());
                    safeCopy.setBasePrice(roomType.getBasePrice());
                    safeCopy.setMaxCapacity(roomType.getMaxCapacity());
                    safeCopy.setBedType(roomType.getBedType());
                    safeCopy.setRoomSize(roomType.getRoomSize());
                    safeCopy.setAmenities(roomType.getAmenities());
                    safeCopy.setStatus(roomType.getStatus());
                    roomTypes.add(safeCopy);
                }
            }
        } // Lấy dịch vụ của khách sạn - sử dụng service method để tránh
          // ConcurrentModificationException
        Set<Service> hotelServices = new HashSet<>();
        try {
            hotelServices = hotelService.getHotelServices(id);
        } catch (Exception e) {
            System.err.println("Error loading hotel services for hotel " + id + ": " + e.getMessage());
            e.printStackTrace();
        }

        // Thống kê phòng
        long totalRooms = 0;
        long availableRoomsCount = 0;
        try {
            totalRooms = roomService.countRoomsByHotel(id);
            availableRoomsCount = roomService.countAvailableRoomsByHotel(id);
        } catch (Exception e) {
            System.err.println("Error loading room counts for hotel " + id + ": " + e.getMessage());
            e.printStackTrace();
        }

        model.addAttribute("isAuthenticated", isAuthenticated);
        model.addAttribute("hotel", safeHotel); // Use the safe hotel object
        model.addAttribute("comments", comments);
        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("roomTypes", roomTypes);
        model.addAttribute("hotelServices", hotelServices);
        model.addAttribute("totalRooms", totalRooms);
        model.addAttribute("availableRoomsCount", availableRoomsCount);

        // Lấy các khách sạn liên quan (cùng khu vực) - sử dụng phương thức an toàn hơn
        List<Hotel> relatedHotels = new ArrayList<>();
        if (safeHotel.getLocation() != null) {
            try {
                // Use safer method with a pageable to limit to 4 hotels
                PageRequest pageRequest = PageRequest.of(0, 4);
                List<Hotel> foundHotels = hotelRepository.findRelatedHotelsSafe(
                        safeHotel.getLocation().getId(), id, 1, pageRequest);

                // Create defensive copies to prevent any potential concurrency issues
                for (Hotel foundHotel : foundHotels) {
                    if (foundHotel != null) {
                        relatedHotels.add(foundHotel);
                    }
                }

                System.out.println("Found " + relatedHotels.size() + " related hotels");
            } catch (Exception e) {
                System.err.println("Error fetching related hotels: " + e.getMessage());
                e.printStackTrace();
            }
        }
        model.addAttribute("relatedHotels", relatedHotels);

        return "hotel/detail";
    }

    @GetMapping("/{hotelId}/room/{roomId}")
    public String roomDetail(@PathVariable Long hotelId, @PathVariable Long roomId, Model model) {
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new IllegalArgumentException("Khách sạn không tồn tại với id: " + hotelId));

        Room room = roomService.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Phòng không tồn tại với id: " + roomId));

        // Kiểm tra phòng có thuộc khách sạn này không
        if (!room.getHotel().getId().equals(hotelId)) {
            throw new IllegalArgumentException("Phòng không thuộc khách sạn này");
        }

        // Lấy các phòng cùng loại
        List<Room> similarRooms = roomService.findAvailableRoomsByHotelAndType(hotelId, room.getRoomType().getId());

        model.addAttribute("hotel", hotel);
        model.addAttribute("room", room);
        model.addAttribute("similarRooms", similarRooms);

        return "hotel/room-detail";
    }

    @GetMapping("/{id}/book")
    public String bookForm(@PathVariable Long id,
            @RequestParam(required = false) Long roomId,
            @RequestParam(required = false) String checkin,
            @RequestParam(required = false) String checkout,
            @RequestParam(required = false) Integer guests,
            Model model, HttpServletRequest request) {
        // Determine if user is authenticated
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() &&
                !auth.getPrincipal().equals("anonymousUser");
        model.addAttribute("isAuthenticated", isAuthenticated);

        // Create a new BookHotelDto object for the form
        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khách sạn không tồn tại với id: " + id));

        BookHotelDto bookHotelDto = new BookHotelDto();
        bookHotelDto.setHotelId(id); // Prefill user info for authenticated
        if (isAuthenticated) {
            User currentUser = userRepository.findByUsername(auth.getName()).orElse(null);
            if (currentUser != null) {
                bookHotelDto.setCustomerName(currentUser.getFullName());
                bookHotelDto.setEmail(currentUser.getEmail());
                bookHotelDto.setPhone(currentUser.getPhone());
                System.out.println("DEBUG: Prefilled booking form for user: " + currentUser.getUsername() + " ("
                        + currentUser.getFullName() + ")");
            }
        }

        // Pre-fill form with URL parameters if provided
        if (checkin != null && !checkin.isEmpty()) {
            bookHotelDto.setCheckInDate(checkin);
        } else {
            bookHotelDto.setCheckInDate("");
        }

        if (checkout != null && !checkout.isEmpty()) {
            bookHotelDto.setCheckOutDate(checkout);
        } else {
            bookHotelDto.setCheckOutDate("");
        }

        if (guests != null && guests > 0) {
            bookHotelDto.setNumberOfGuests(guests);
        } else {
            bookHotelDto.setNumberOfGuests(1);
        } // Initialize other required fields to prevent Thymeleaf binding errors (only if
          // not already set)
        if (bookHotelDto.getCustomerName() == null) {
            bookHotelDto.setCustomerName("");
        }
        if (bookHotelDto.getEmail() == null) {
            bookHotelDto.setEmail("");
        }
        if (bookHotelDto.getPhone() == null) {
            bookHotelDto.setPhone("");
        }        // If a specific room is selected, set the room ID and room type
        Room selectedRoom = null;
        if (roomId != null) {
            selectedRoom = roomService.findById(roomId).orElse(null);
            if (selectedRoom != null && selectedRoom.getHotel().getId().equals(id)) {
                bookHotelDto.setRoomId(selectedRoom.getId());
                bookHotelDto.setRoomTypeId(selectedRoom.getRoomType().getId());
                model.addAttribute("selectedRoom", selectedRoom);
            }
        }        // Load all available rooms for this hotel
        List<Room> availableRooms = roomService.findAvailableRoomsByHotelId(id);
        
        System.out.println(availableRooms.size() + " available rooms found for hotel ID: " + id);
        model.addAttribute("title", "Đặt phòng - " + hotel.getName());
        model.addAttribute("hotel", hotel);
        model.addAttribute("bookHotelDto", bookHotelDto);
        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("httpServletRequest", request);

        return "hotel/book";
    }

    @PostMapping("/{id}/book")
    public String book(@PathVariable Long id,
            @Valid @ModelAttribute("bookHotelDto") BookHotelDto bookHotelDto,
            BindingResult result,
            RedirectAttributes redirectAttributes,
            Model model,
            HttpServletRequest request,
            HttpSession session) {
                System.out.println("DEBUG: Starting booking process for hotel ID: " + bookHotelDto.toString());
        // Determine authentication status once
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() &&
                !auth.getPrincipal().equals("anonymousUser");
        // Always add isAuthenticated to model
        model.addAttribute("isAuthenticated", isAuthenticated);

        // If authenticated, set userId on DTO
        if (isAuthenticated) {
            User currentUser = userRepository.findByUsername(auth.getName()).orElse(null);
            if (currentUser != null) {
                bookHotelDto.setUserId(currentUser.getId());
                System.out.println("DEBUG: Setting userId for authenticated user: " + currentUser.getId() + " ("
                        + currentUser.getUsername() + ")");
            } else {
                System.out.println("WARNING: Authenticated user not found in database: " + auth.getName());
            }
        } else {
            System.out.println("DEBUG: User is not authenticated, booking as guest");
        }

        // Log booking attempt
        System.out.println("Booking attempt for hotel ID: " + id);
        System.out.println("BookHotelDto: " + bookHotelDto.toString());
        System.out.println("DEBUG: isAuthenticated = " + isAuthenticated + ", userId = " + bookHotelDto.getUserId());        Hotel hotel = hotelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Khách sạn không tồn tại với id: " + id));
        List<Room> availableRooms = roomService.findAvailableRoomsByHotelId(id);

        // Add required attributes to model
        model.addAttribute("availableRooms", availableRooms);
        model.addAttribute("title", "Đặt phòng - " + hotel.getName());
        model.addAttribute("hotel", hotel);
        model.addAttribute("httpServletRequest", request);

        if (result.hasErrors()) {
            for (org.springframework.validation.FieldError error : result.getFieldErrors()) {
                model.addAttribute(error.getField() + "Error", error.getDefaultMessage());
            }
            // Add general error message
            model.addAttribute("errorMessage", "Vui lòng kiểm tra lại thông tin đặt phòng");
            return "hotel/book";
        }

        try {            // Additional validation - check if room is selected
            if (bookHotelDto.getRoomId() == null) {
                model.addAttribute("roomIdError", "Vui lòng chọn phòng");
                model.addAttribute("errorMessage", "Vui lòng chọn phòng trước khi đặt");
                return "hotel/book";
            }

            // Additional validation - check dates
            if (bookHotelDto.getCheckInDate() == null || bookHotelDto.getCheckInDate().isEmpty() ||
                    bookHotelDto.getCheckOutDate() == null || bookHotelDto.getCheckOutDate().isEmpty()) {
                model.addAttribute("errorMessage", "Vui lòng chọn ngày nhận phòng và trả phòng");
                return "hotel/book";
            }

            try {
                // Parse dates and validate them
                LocalDateTime checkIn = bookHotelDto.getCheckInDateTime();
                LocalDateTime checkOut = bookHotelDto.getCheckOutDateTime();
                if (checkIn == null || checkOut == null) {
                    model.addAttribute("errorMessage", "Không thể xử lý ngày tháng. Vui lòng chọn ngày hợp lệ.");
                    return "hotel/book";
                }
                // Additional validation - check if check-in date is after check-out date
                if (checkIn.isAfter(checkOut)) {
                    model.addAttribute("checkInDateError", "Ngày nhận phòng không thể sau ngày trả phòng");
                    model.addAttribute("errorMessage", "Ngày nhận phòng không thể sau ngày trả phòng");
                    return "hotel/book";
                }
                // Additional validation - check if check-in date is in the past
                if (checkIn.toLocalDate().isBefore(LocalDateTime.now().toLocalDate())) {
                    model.addAttribute("checkInDateError", "Ngày nhận phòng không thể trong quá khứ");
                    model.addAttribute("errorMessage", "Ngày nhận phòng không thể trong quá khứ");
                    return "hotel/book";
                }
            } catch (Exception e) {
                model.addAttribute("errorMessage", "Lỗi định dạng ngày tháng: " + e.getMessage());
                return "hotel/book";
            }

            // Process booking
            BookHotel savedBooking = bookHotelService.create(bookHotelDto);

            redirectAttributes.addFlashAttribute("successMessage",
                    "Đặt phòng thành công! Mã đặt phòng của bạn là: " + savedBooking.getId());
            // Redirect based on authentication status
            if (isAuthenticated) {
                return "redirect:/user/bookings";
            } else {
                session.setAttribute("guestBookingId", savedBooking.getId());
                return "redirect:/hotel/guest-booking-success";
            }
        } catch (IllegalArgumentException e) {
            // Specific error handling for validation errors
            model.addAttribute("errorMessage", e.getMessage());
            System.err.println("Booking validation error: " + e.getMessage());
            return "hotel/book";
        } catch (Exception e) {
            // General error handling
            model.addAttribute("errorMessage", "Có lỗi xảy ra khi đặt phòng: " + e.getMessage());
            e.printStackTrace(); // Log the detailed error
            return "hotel/book";
        }
    }

    @GetMapping("/check-room-availability")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> checkRoomAvailability(
            @RequestParam Long hotelId,
            @RequestParam Long roomTypeId,
            @RequestParam String checkInDate,
            @RequestParam String checkOutDate) {
        Map<String, Object> response = new HashMap<>();

        try {
            // Parse dates
            LocalDateTime checkIn = LocalDateTime.parse(checkInDate + "T14:00:00");
            LocalDateTime checkOut = LocalDateTime.parse(checkOutDate + "T12:00:00");

            // Kiểm tra số lượng phòng khả dụng
            int availableCount = roomAvailabilityService.countAvailableRooms(
                    hotelId, roomTypeId, checkIn, checkOut);

            // Lấy danh sách phòng khả dụng
            List<Room> availableRooms = roomAvailabilityService.findAvailableRooms(
                    hotelId, roomTypeId, checkIn, checkOut);

            response.put("available", availableCount > 0);
            response.put("count", availableCount);
            response.put("rooms", availableRooms.size());

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "Có lỗi xảy ra khi kiểm tra tình trạng phòng: " + e.getMessage());
            response.put("available", false);
            response.put("count", 0);
            return ResponseEntity.status(500).body(response);
        }
    } // Guest booking success page

    @GetMapping("/guest-booking-success")
    public String guestBookingSuccess(Model model, HttpSession session) {
        model.addAttribute("title", "Đặt phòng thành công");

        Long bookingId = (Long) session.getAttribute("guestBookingId");
        if (bookingId == null) {
            return "redirect:/"; // no booking found
        }
        Optional<BookHotel> bookingOpt = bookHotelService.findById(bookingId);
        if (bookingOpt.isPresent()) {
            BookHotelDto dto = bookHotelService.convertToDto(bookingOpt.get());
            model.addAttribute("booking", dto);
        } else {
            model.addAttribute("errorMessage", "Đặt phòng không tìm thấy");
        }
        return "hotel/guest-booking-success";
    }

    @GetMapping("/{hotelId}/booking/{bookingId}")
    public String bookingDetail(@PathVariable Long hotelId, @PathVariable Long bookingId, Model model) {
        // Verify hotel exists
        Hotel hotel = hotelRepository.findById(hotelId)
                .orElseThrow(() -> new IllegalArgumentException("Khách sạn không tồn tại với id: " + hotelId));

        // Get booking details
        BookHotel booking = bookHotelService.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Đặt phòng không tồn tại với id: " + bookingId));

        // Verify booking belongs to this hotel
        if (!booking.getHotel().getId().equals(hotelId)) {
            throw new IllegalArgumentException("Đặt phòng không thuộc khách sạn này");
        }

        // Check if user has permission to view this booking
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null && auth.isAuthenticated() &&
                !auth.getPrincipal().equals("anonymousUser");

        boolean canView = false;
        if (isAuthenticated) {
            User currentUser = userRepository.findByUsername(auth.getName()).orElse(null);
            if (currentUser != null) {
                // User can view their own bookings
                canView = booking.getUser() != null && booking.getUser().getId().equals(currentUser.getId());
            }
        }

        if (!canView) {
            throw new IllegalArgumentException("Bạn không có quyền xem đặt phòng này");
        }

        model.addAttribute("title", "Chi tiết đặt phòng - " + hotel.getName());
        model.addAttribute("hotel", hotel);
        model.addAttribute("booking", booking);

        return "hotel/booking-detail";
    }
}