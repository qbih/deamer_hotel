package com.example.dulich.service;

import com.example.dulich.entity.BookHotel;
import com.example.dulich.entity.Hotel;

public interface EmailService {
    
    /**
     * Gửi email xác nhận đặt phòng cho khách hàng
     */
    void sendBookingConfirmationEmail(BookHotel booking, Hotel hotel);
    
    /**
     * Gửi email thông báo thay đổi trạng thái đặt phòng
     */
    void sendBookingStatusUpdateEmail(BookHotel booking, Hotel hotel, String oldStatus, String newStatus);
    
    /**
     * Gửi email hủy đặt phòng
     */
    void sendBookingCancellationEmail(BookHotel booking, Hotel hotel);
    
    /**
     * Gửi email nhắc nhở trước ngày nhận phòng
     */
    void sendCheckInReminderEmail(BookHotel booking, Hotel hotel);
    
    /**
     * Gửi email test để kiểm tra cấu hình
     */
    void sendTestEmail(String to, String subject, String content);
}
