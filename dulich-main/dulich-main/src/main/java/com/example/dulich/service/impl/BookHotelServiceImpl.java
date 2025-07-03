package com.example.dulich.service.impl;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.dulich.dto.BookHotelDto;
import com.example.dulich.entity.BookHotel;
import com.example.dulich.entity.BookHotelRoom;
import com.example.dulich.entity.Hotel;
import com.example.dulich.entity.Room;
import com.example.dulich.entity.User;
import com.example.dulich.repository.BookHotelRepository;
import com.example.dulich.repository.BookHotelRoomRepository;
import com.example.dulich.repository.HotelRepository;
import com.example.dulich.repository.RoomRepository;
import com.example.dulich.repository.UserRepository;
import com.example.dulich.service.BookHotelService;
import com.example.dulich.service.EmailService;
import com.example.dulich.service.RoomService;

@Service
@Transactional
public class BookHotelServiceImpl implements BookHotelService {

    @Autowired
    private BookHotelRepository bookHotelRepository;

    @Autowired
    private BookHotelRoomRepository bookHotelRoomRepository;

    @Autowired
    private HotelRepository hotelRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoomRepository roomRepository;
    @Autowired
    private RoomService roomService;

    @Autowired
    private EmailService emailService;// Status constants
    private static final Map<Integer, String> STATUS_MAP = new HashMap<Integer, String>() {
        {
            put(BookHotel.STATUS_RECEIVED, "Đã nhận");
            put(BookHotel.STATUS_CONFIRMED, "Đã xác nhận");
            put(BookHotel.STATUS_PAID, "Đã thanh toán");
            put(BookHotel.STATUS_COMPLETED, "Hoàn thành");
            put(BookHotel.STATUS_CANCELLED, "Đã hủy");
        }
    };

    @Override
    @Transactional(readOnly = true)
    public List<BookHotel> findAll() {
        return bookHotelRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookHotel> findAllPaged(Pageable pageable) {
        return bookHotelRepository.findAll(pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<BookHotel> findById(Long id) {
        return bookHotelRepository.findById(id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<BookHotel> findByUserId(Long userId) {
        System.out.println("DEBUG: BookHotelService.findByUserId called with userId: " + userId);
        List<BookHotel> result = bookHotelRepository.findByUserId(userId);
        System.out.println("DEBUG: BookHotelRepository.findByUserId returned " + result.size() + " bookings");
        for (BookHotel booking : result) {
            System.out.println("  - Booking ID: " + booking.getId() + ", User: " +
                    (booking.getUser() != null
                            ? booking.getUser().getUsername() + " (ID: " + booking.getUser().getId() + ")"
                            : "null")
                    +
                    ", Customer: " + booking.getCustomerName());
        }
        return result;
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookHotel> findByHotelId(Long hotelId, Pageable pageable) {
        return bookHotelRepository.findByHotelId(hotelId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<BookHotel> findWithFilters(Long hotelId, Long userId, Integer status,
            LocalDateTime checkInFrom, LocalDateTime checkInTo,
            Pageable pageable) {
        return bookHotelRepository.findWithFilters(hotelId, userId, status, checkInFrom, checkInTo, pageable);
    }

    @Override
    public BookHotel create(BookHotelDto bookHotelDto) {
        try {
            LocalDateTime checkInDateTime = bookHotelDto.getCheckInDateTime();
            LocalDateTime checkOutDateTime = bookHotelDto.getCheckOutDateTime();

            if (checkInDateTime == null || checkOutDateTime == null) {
                throw new IllegalArgumentException("Ngày nhận phòng và trả phòng không được để trống");
            }

            if (checkInDateTime.isAfter(checkOutDateTime)) {
                throw new IllegalArgumentException("Ngày nhận phòng không thể sau ngày trả phòng");
            }

            if (checkInDateTime.toLocalDate().isBefore(LocalDateTime.now().toLocalDate())) {
                throw new IllegalArgumentException("Ngày nhận phòng không thể trong quá khứ");
            }

        } catch (Exception e) {
            System.err.println("Date validation error: " + e.getMessage());
            throw new IllegalArgumentException("Lỗi định dạng ngày tháng: " + e.getMessage());
        }
        if (bookHotelDto.getRoomId() == null) {
            throw new IllegalArgumentException("Vui lòng chọn phòng");
        }

        Hotel hotel = hotelRepository.findById(bookHotelDto.getHotelId())
                .orElseThrow(() -> new IllegalArgumentException("Khách sạn không tồn tại"));
        System.out.println("DEBUG: Found hotel for booking:  (ID: " + bookHotelDto.getRoomId() + ")");
        Room selectedRoom = roomRepository.findById(bookHotelDto.getRoomId())
                .orElseThrow(() -> new IllegalArgumentException("Phòng không tồn tại"));
        System.out.println("DEBUG: Booking details - Hotel: " + selectedRoom.toString());

        if (!selectedRoom.getHotel().getId().equals(hotel.getId())) {
            throw new IllegalArgumentException("Phòng không thuộc khách sạn này");
        }
        User user = null;
        if (bookHotelDto.getUserId() != null) {
            user = userRepository.findById(bookHotelDto.getUserId()).orElse(null);
            System.out.println("DEBUG: Found user for booking: "
                    + (user != null ? user.getUsername() + " (ID: " + user.getId() + ")" : "null"));
        } else {
            System.out.println("DEBUG: No userId provided, creating guest booking");
        }

        LocalDateTime checkIn = bookHotelDto.getCheckInDateTime();
        LocalDateTime checkOut = bookHotelDto.getCheckOutDateTime();

        boolean isRoomAvailable = roomService.isRoomAvailable(selectedRoom.getId(), checkIn, checkOut);

        if (!isRoomAvailable) {
            throw new IllegalArgumentException(
                    String.format("Phòng %s không có sẵn trong thời gian từ %s đến %s",
                            selectedRoom.getRoomNumber(),
                            checkIn.toLocalDate().toString(),
                            checkOut.toLocalDate().toString()));
        }

        long totalNights = ChronoUnit.DAYS.between(checkIn.toLocalDate(), checkOut.toLocalDate());
        if (totalNights <= 0) {
            totalNights = 1;
        }
        double pricePerNight = selectedRoom.getPricePerNight();
        double totalPrice = totalNights * pricePerNight;        // Create booking
        BookHotel bookHotel = new BookHotel();
        bookHotel.setHotel(hotel);
        bookHotel.setRoomType(selectedRoom.getRoomType());
        bookHotel.setRoom(selectedRoom); // Set the selected room to fix database saving issue
        bookHotel.setUser(user);
        bookHotel.setCustomerName(bookHotelDto.getCustomerName());
        bookHotel.setEmail(bookHotelDto.getEmail());
        bookHotel.setPhone(bookHotelDto.getPhone());
        bookHotel.setAddress(bookHotelDto.getAddress());
        bookHotel.setCheckInDate(checkIn);
        bookHotel.setCheckOutDate(checkOut);
        bookHotel.setNumberOfGuests(bookHotelDto.getNumberOfGuests());
        bookHotel.setPricePerNight(pricePerNight);
        bookHotel.setTotalNights((int) totalNights);
        bookHotel.setTotalPrice(totalPrice);
        bookHotel.setSpecialRequests(bookHotelDto.getSpecialRequests());
        bookHotel.setStatus(BookHotel.STATUS_RECEIVED);

        // Save booking
        BookHotel savedBooking = bookHotelRepository.save(bookHotel);
        System.out.println("DEBUG: Booking saved with ID: " + savedBooking.getId() + ", User: " +
                (savedBooking.getUser() != null
                        ? savedBooking.getUser().getUsername() + " (ID: " + savedBooking.getUser().getId() + ")"
                        : "null"));

        // Create BookHotelRoom entry directly with the selected room
        BookHotelRoom bookHotelRoom = new BookHotelRoom();
        bookHotelRoom.setBookHotel(savedBooking);
        bookHotelRoom.setRoom(selectedRoom);
        bookHotelRoomRepository.save(bookHotelRoom);

        System.out.println("Booking created successfully with ID: " + savedBooking.getId());
        System.out.println("Room assigned: " + selectedRoom.getRoomNumber());

        // Gửi email xác nhận đặt phòng
        try {
            emailService.sendBookingConfirmationEmail(savedBooking, hotel);
        } catch (Exception e) {
            System.err.println("Lỗi gửi email xác nhận đặt phòng: " + e.getMessage());
            // Không throw exception để không ảnh hưởng đến quá trình đặt phòng
        }

        return savedBooking;
    }

    @Override
    public BookHotel update(BookHotelDto bookHotelDto, Long id) {
        BookHotel existingBookHotel = bookHotelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Đặt phòng không tồn tại"));

        // Update basic information
        existingBookHotel.setCustomerName(bookHotelDto.getCustomerName());
        existingBookHotel.setEmail(bookHotelDto.getEmail());
        existingBookHotel.setPhone(bookHotelDto.getPhone());
        existingBookHotel.setAddress(bookHotelDto.getAddress());
        existingBookHotel.setSpecialRequests(bookHotelDto.getSpecialRequests());

        // Update status if provided
        if (bookHotelDto.getStatus() != null) {
            existingBookHotel.setStatus(bookHotelDto.getStatus());
        }

        return bookHotelRepository.save(existingBookHotel);
    }

    @Override
    @Transactional
    public void delete(Long id) {
        BookHotel bookHotel = bookHotelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Đặt phòng không tồn tại"));

        try {
            // Delete associated BookHotelRoom entries first to avoid foreign key constraint
            List<BookHotelRoom> bookHotelRooms = bookHotelRoomRepository.findByBookHotelId(id);
            if (!bookHotelRooms.isEmpty()) {
                bookHotelRoomRepository.deleteAll(bookHotelRooms);
            }

            // Then delete the booking
            bookHotelRepository.delete(bookHotel);

        } catch (Exception e) {
            System.err.println("Error deleting booking: " + e.getMessage());
            throw new RuntimeException("Không thể xóa đặt phòng: " + e.getMessage(), e);
        }
    }

    @Override
    public BookHotel updateStatus(Long id, Integer status) {
        BookHotel bookHotel = bookHotelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Đặt phòng không tồn tại"));

        // Lưu trạng thái cũ để gửi email
        String oldStatus = bookHotel.getStatusName();
        Integer oldStatusCode = bookHotel.getStatus();

        bookHotel.setStatus(status);
        BookHotel updatedBooking = bookHotelRepository.save(bookHotel);

        // Gửi email thông báo thay đổi trạng thái (nếu trạng thái thực sự thay đổi)
        if (!oldStatusCode.equals(status)) {
            try {
                String newStatus = updatedBooking.getStatusName();

                // Gửi email hủy đặt phòng nếu trạng thái là hủy
                if (status == BookHotel.STATUS_CANCELLED) {
                    emailService.sendBookingCancellationEmail(updatedBooking, updatedBooking.getHotel());
                } else {
                    // Gửi email cập nhật trạng thái bình thường
                    emailService.sendBookingStatusUpdateEmail(updatedBooking, updatedBooking.getHotel(), oldStatus,
                            newStatus);
                }
            } catch (Exception e) {
                System.err.println("Lỗi gửi email cập nhật trạng thái: " + e.getMessage());
                // Không throw exception để không ảnh hưởng đến quá trình cập nhật
            }
        }

        return updatedBooking;
    }

    @Override
    public boolean updatePaymentStatus(Long id, String paymentMethod) {
        try {
            BookHotel bookHotel = bookHotelRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Đặt phòng không tồn tại"));

            // Cập nhật trạng thái thanh toán thành "đã thanh toán"
            bookHotel.setStatus(BookHotel.STATUS_PAID);

            // Lưu phương thức thanh toán vào specialRequests nếu cần
            String currentRequests = bookHotel.getSpecialRequests();
            String paymentInfo = "Thanh toán qua " + paymentMethod + " vào " + LocalDateTime.now();

            if (currentRequests != null && !currentRequests.isEmpty()) {
                bookHotel.setSpecialRequests(currentRequests + "\n" + paymentInfo);
            } else {
                bookHotel.setSpecialRequests(paymentInfo);
            }

            bookHotelRepository.save(bookHotel);

            // Gửi email thông báo thanh toán thành công
            try {
                emailService.sendBookingStatusUpdateEmail(
                        bookHotel,
                        bookHotel.getHotel(),
                        "Chờ thanh toán",
                        "Đã thanh toán");
            } catch (Exception e) {
                System.err.println("Lỗi gửi email thông báo thanh toán: " + e.getMessage());
                // Không throw exception để không ảnh hưởng đến quá trình thanh toán
            }

            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public Map<Integer, String> getStatusList() {
        return STATUS_MAP;
    }

    @Override
    @Transactional(readOnly = true)
    public long countByStatus(Integer status) {
        return bookHotelRepository.countByStatus(status);
    }

    @Override
    @Transactional(readOnly = true)
    public double getTotalRevenue() {
        return bookHotelRepository.findAll().stream()
                .filter(booking -> booking.getStatus() == BookHotel.STATUS_PAID)
                .mapToDouble(BookHotel::getTotalPrice)
                .sum();
    }

    @Override
    @Transactional(readOnly = true)
    public double getTotalRevenueByStatus(Integer status) {
        return bookHotelRepository.findAll().stream()
                .filter(booking -> booking.getStatus().equals(status))
                .mapToDouble(BookHotel::getTotalPrice)
                .sum();
    }

    @Override
    @Transactional(readOnly = true)
    public double getTotalRevenueInRange(LocalDateTime startDate, LocalDateTime endDate) {
        return bookHotelRepository.findAll().stream()
                .filter(booking -> booking.getStatus() == BookHotel.STATUS_PAID)
                .filter(booking -> booking.getCheckInDate().isAfter(startDate)
                        || booking.getCheckInDate().isEqual(startDate))
                .filter(booking -> booking.getCheckInDate().isBefore(endDate)
                        || booking.getCheckInDate().isEqual(endDate))
                .mapToDouble(BookHotel::getTotalPrice)
                .sum();
    }

    @Override
    public BookHotelDto convertToDto(BookHotel bookHotel) {
        if (bookHotel == null) {
            return null;
        }

        BookHotelDto dto = new BookHotelDto();
        dto.setId(bookHotel.getId());
        dto.setHotelId(bookHotel.getHotel() != null ? bookHotel.getHotel().getId() : null);
        dto.setHotelName(bookHotel.getHotel() != null ? bookHotel.getHotel().getName() : null);

        if (bookHotel.getRoomType() != null) {
            dto.setRoomTypeId(bookHotel.getRoomType().getId());
            dto.setRoomTypeName(bookHotel.getRoomType().getName());
        }
        dto.setUserId(bookHotel.getUser() != null ? bookHotel.getUser().getId() : null);
        dto.setUserName(bookHotel.getUser() != null ? bookHotel.getUser().getUsername() : null);
        dto.setCustomerName(bookHotel.getCustomerName());
        dto.setEmail(bookHotel.getEmail());
        dto.setPhone(bookHotel.getPhone());
        dto.setAddress(bookHotel.getAddress());

        // Set date fields by using the setter methods which handle conversion
        dto.setCheckInDateTime(bookHotel.getCheckInDate());
        dto.setCheckOutDateTime(bookHotel.getCheckOutDate());

        dto.setNumberOfGuests(bookHotel.getNumberOfGuests());
        dto.setPricePerNight(bookHotel.getPricePerNight());
        dto.setTotalNights(bookHotel.getTotalNights());
        dto.setTotalPrice(bookHotel.getTotalPrice());
        dto.setSpecialRequests(bookHotel.getSpecialRequests());
        dto.setStatus(bookHotel.getStatus());

        return dto;
    }

    @Override
    public BookHotel convertToEntity(BookHotelDto bookHotelDto) {
        if (bookHotelDto == null) {
            return null;
        }
        BookHotel bookHotel = new BookHotel();
        bookHotel.setId(bookHotelDto.getId());
        bookHotel.setCustomerName(bookHotelDto.getCustomerName());
        bookHotel.setEmail(bookHotelDto.getEmail());
        bookHotel.setPhone(bookHotelDto.getPhone());
        bookHotel.setAddress(bookHotelDto.getAddress());

        // Set date fields using getter methods which handle conversion
        bookHotel.setCheckInDate(bookHotelDto.getCheckInDateTime());
        bookHotel.setCheckOutDate(bookHotelDto.getCheckOutDateTime());

        bookHotel.setNumberOfGuests(bookHotelDto.getNumberOfGuests());
        bookHotel.setPricePerNight(bookHotelDto.getPricePerNight());
        bookHotel.setTotalNights(bookHotelDto.getTotalNights());
        bookHotel.setTotalPrice(bookHotelDto.getTotalPrice());
        bookHotel.setSpecialRequests(bookHotelDto.getSpecialRequests());
        bookHotel.setStatus(bookHotelDto.getStatus());

        return bookHotel;
    }
}
