package com.example.dulich.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.example.dulich.entity.BookHotel;
import com.example.dulich.repository.BookHotelRepository;

@Service
public class BookingReminderService {

    @Autowired
    private BookHotelRepository bookHotelRepository;

    @Autowired
    private EmailService emailService;

    /**
     * Gửi email nhắc nhở trước 1 ngày check-in
     * Chạy mỗi ngày vào lúc 9:00 sáng
     */
    @Scheduled(cron = "0 0 9 * * ?")
    public void sendCheckInReminders() {
        try {
            LocalDateTime tomorrow = LocalDateTime.now().plusDays(1);
            LocalDateTime startOfTomorrow = tomorrow.toLocalDate().atStartOfDay();
            LocalDateTime endOfTomorrow = tomorrow.toLocalDate().atTime(23, 59, 59);

            // Tìm các booking có ngày check-in vào ngày mai và trạng thái đã thanh toán
            List<BookHotel> upcomingBookings = bookHotelRepository.findByCheckInDateBetweenAndStatus(
                    startOfTomorrow, endOfTomorrow, BookHotel.STATUS_PAID);

            System.out.println("Found " + upcomingBookings.size() + " bookings for tomorrow check-in reminder");

            for (BookHotel booking : upcomingBookings) {
                try {
                    emailService.sendCheckInReminderEmail(booking, booking.getHotel());
                } catch (Exception e) {
                    System.err.println("Error sending check-in reminder for booking ID " + booking.getId() + ": " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Error in sendCheckInReminders: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Tự động cập nhật trạng thái booking đã hoàn thành
     * Chạy mỗi ngày vào lúc 13:00
     */
    @Scheduled(cron = "0 0 13 * * ?")
    public void updateCompletedBookings() {
        try {
            LocalDateTime yesterday = LocalDateTime.now().minusDays(1);
            LocalDateTime endOfYesterday = yesterday.toLocalDate().atTime(23, 59, 59);

            // Tìm các booking có ngày check-out đã qua và vẫn ở trạng thái đã thanh toán
            List<BookHotel> expiredBookings = bookHotelRepository.findByCheckOutDateBeforeAndStatus(
                    endOfYesterday, BookHotel.STATUS_PAID);

            System.out.println("Found " + expiredBookings.size() + " bookings to mark as completed");

            for (BookHotel booking : expiredBookings) {
                try {
                    booking.setStatus(BookHotel.STATUS_COMPLETED);
                    bookHotelRepository.save(booking);
                    
                    // Gửi email thông báo hoàn thành
                    emailService.sendBookingStatusUpdateEmail(booking, booking.getHotel(), "Đã thanh toán", "Hoàn thành");
                } catch (Exception e) {
                    System.err.println("Error updating booking ID " + booking.getId() + " to completed: " + e.getMessage());
                }
            }

        } catch (Exception e) {
            System.err.println("Error in updateCompletedBookings: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
