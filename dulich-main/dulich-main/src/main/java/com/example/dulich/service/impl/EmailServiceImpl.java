package com.example.dulich.service.impl;

import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import com.example.dulich.entity.BookHotel;
import com.example.dulich.entity.Hotel;
import com.example.dulich.service.EmailService;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailServiceImpl implements EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@dulich.com}")
    private String fromEmail;

    @Value("${app.name:Du Lịch}")
    private String appName;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private final NumberFormat currencyFormat = NumberFormat.getInstance(new Locale("vi", "VN"));

    @Override
    public void sendBookingConfirmationEmail(BookHotel booking, Hotel hotel) {
        try {
            String subject = String.format("[%s] Xác nhận đặt phòng #%d", appName, booking.getId());
            String content = createBookingConfirmationEmailContent(booking, hotel);
            sendHtmlEmail(booking.getEmail(), subject, content);
            
            System.out.println("✅ Đã gửi email xác nhận đặt phòng cho: " + booking.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Lỗi gửi email xác nhận đặt phòng: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void sendBookingStatusUpdateEmail(BookHotel booking, Hotel hotel, String oldStatus, String newStatus) {
        try {
            String subject = String.format("[%s] Cập nhật trạng thái đặt phòng #%d", appName, booking.getId());
            String content = createStatusUpdateEmailContent(booking, hotel, oldStatus, newStatus);
            sendHtmlEmail(booking.getEmail(), subject, content);
            
            System.out.println("✅ Đã gửi email cập nhật trạng thái cho: " + booking.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Lỗi gửi email cập nhật trạng thái: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void sendBookingCancellationEmail(BookHotel booking, Hotel hotel) {
        try {
            String subject = String.format("[%s] Hủy đặt phòng #%d", appName, booking.getId());
            String content = createCancellationEmailContent(booking, hotel);
            sendHtmlEmail(booking.getEmail(), subject, content);
            
            System.out.println("✅ Đã gửi email hủy đặt phòng cho: " + booking.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Lỗi gửi email hủy đặt phòng: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void sendCheckInReminderEmail(BookHotel booking, Hotel hotel) {
        try {
            String subject = String.format("[%s] Nhắc nhở: Sắp đến ngày nhận phòng #%d", appName, booking.getId());
            String content = createCheckInReminderEmailContent(booking, hotel);
            sendHtmlEmail(booking.getEmail(), subject, content);
            
            System.out.println("✅ Đã gửi email nhắc nhở cho: " + booking.getEmail());
        } catch (Exception e) {
            System.err.println("❌ Lỗi gửi email nhắc nhở: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @Override
    public void sendTestEmail(String to, String subject, String content) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(content);
            
            mailSender.send(message);
            System.out.println("✅ Đã gửi email test thành công cho: " + to);
        } catch (Exception e) {
            System.err.println("❌ Lỗi gửi email test: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
        
        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);
        
        mailSender.send(message);
    }

    private String createBookingConfirmationEmailContent(BookHotel booking, Hotel hotel) {
        StringBuilder content = new StringBuilder();
        content.append(getEmailHeader());
        content.append("<h2 style='color: #28a745;'>✅ Đặt phòng thành công!</h2>");
        content.append("<p>Chúng tôi đã nhận được yêu cầu đặt phòng của bạn. Dưới đây là thông tin chi tiết:</p>");
        content.append(getBookingDetailsSection(booking, hotel));
        content.append(getImportantNotesSection(booking));
        content.append(getEmailFooter());
        return content.toString();
    }

    private String createStatusUpdateEmailContent(BookHotel booking, Hotel hotel, String oldStatus, String newStatus) {
        StringBuilder content = new StringBuilder();
        content.append(getEmailHeader());
        content.append("<h2 style='color: #007bff;'>📋 Cập nhật trạng thái đặt phòng</h2>");
        content.append(String.format("<p>Trạng thái đặt phòng của bạn đã được cập nhật từ <strong>%s</strong> thành <strong style='color: #28a745;'>%s</strong>.</p>", oldStatus, newStatus));
        
        // Thêm thông tin cụ thể theo trạng thái
        if (booking.getStatus() == BookHotel.STATUS_CONFIRMED) {
            content.append("<div style='background-color: #e3f2fd; padding: 15px; border-radius: 5px; margin: 15px 0;'>");
            content.append("<p><strong>📞 Bước tiếp theo:</strong></p>");
            content.append("<ul>");
            content.append("<li>Chúng tôi sẽ liên hệ với bạn để xác nhận chi tiết trong vòng 24 giờ</li>");
            content.append("<li>Vui lòng chuẩn bị thanh toán theo hướng dẫn sẽ được gửi tới</li>");
            content.append("</ul>");
            content.append("</div>");
        } else if (booking.getStatus() == BookHotel.STATUS_PAID) {
            content.append("<div style='background-color: #d4edda; padding: 15px; border-radius: 5px; margin: 15px 0;'>");
            content.append("<p><strong>💳 Thanh toán thành công!</strong></p>");
            content.append("<p>Cảm ơn bạn đã thanh toán. Đặt phòng của bạn đã được xác nhận hoàn tất.</p>");
            content.append("</div>");
        } else if (booking.getStatus() == BookHotel.STATUS_COMPLETED) {
            content.append("<div style='background-color: #d1ecf1; padding: 15px; border-radius: 5px; margin: 15px 0;'>");
            content.append("<p><strong>🎉 Hoàn thành!</strong></p>");
            content.append("<p>Cảm ơn bạn đã sử dụng dịch vụ của chúng tôi. Hy vọng bạn đã có trải nghiệm tuyệt vời!</p>");
            content.append("</div>");
        }
        
        content.append(getBookingDetailsSection(booking, hotel));
        content.append(getEmailFooter());
        return content.toString();
    }

    private String createCancellationEmailContent(BookHotel booking, Hotel hotel) {
        StringBuilder content = new StringBuilder();
        content.append(getEmailHeader());
        content.append("<h2 style='color: #dc3545;'>❌ Đặt phòng đã được hủy</h2>");
        content.append("<p>Đặt phòng của bạn đã được hủy thành công. Dưới đây là thông tin đặt phòng đã hủy:</p>");
        content.append(getBookingDetailsSection(booking, hotel));
        
        content.append("<div style='background-color: #f8d7da; padding: 15px; border-radius: 5px; margin: 15px 0;'>");
        content.append("<p><strong>💰 Thông tin hoàn tiền:</strong></p>");
        content.append("<p>Nếu bạn đã thanh toán, chúng tôi sẽ hoàn tiền theo chính sách của khách sạn. ");
        content.append("Vui lòng liên hệ với chúng tôi nếu cần hỗ trợ thêm.</p>");
        content.append("</div>");
        
        content.append(getEmailFooter());
        return content.toString();
    }

    private String createCheckInReminderEmailContent(BookHotel booking, Hotel hotel) {
        StringBuilder content = new StringBuilder();
        content.append(getEmailHeader());
        content.append("<h2 style='color: #ffc107;'>⏰ Nhắc nhở: Sắp đến ngày nhận phòng</h2>");
        content.append("<p>Chúng tôi muốn nhắc nhở bạn rằng ngày nhận phòng của bạn sắp đến. Dưới đây là thông tin chi tiết:</p>");
        content.append(getBookingDetailsSection(booking, hotel));
        
        content.append("<div style='background-color: #fff3cd; padding: 15px; border-radius: 5px; margin: 15px 0;'>");
        content.append("<p><strong>📋 Lưu ý quan trọng:</strong></p>");
        content.append("<ul>");
        content.append("<li>Thời gian nhận phòng: từ 14:00</li>");
        content.append("<li>Vui lòng mang theo giấy tờ tùy thân khi làm thủ tục nhận phòng</li>");
        content.append("<li>Liên hệ trực tiếp với khách sạn nếu có thay đổi lịch trình</li>");
        content.append("</ul>");
        content.append("</div>");
        
        content.append(getEmailFooter());
        return content.toString();
    }

    private String getEmailHeader() {
        return String.format("""
            <html>
            <head>
                <meta charset="UTF-8">
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #007bff; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                    .content { background-color: #f8f9fa; padding: 20px; }
                    .info-table { width: 100%%; border-collapse: collapse; margin: 15px 0; }
                    .info-table th, .info-table td { padding: 10px; text-align: left; border-bottom: 1px solid #ddd; }
                    .info-table th { background-color: #e9ecef; font-weight: bold; }
                    .footer { background-color: #6c757d; color: white; padding: 15px; text-align: center; border-radius: 0 0 5px 5px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>%s</h1>
                        <p>Hệ thống quản lý đặt phòng khách sạn</p>
                    </div>
                    <div class="content">
            """, appName);
    }

    private String getBookingDetailsSection(BookHotel booking, Hotel hotel) {
        StringBuilder section = new StringBuilder();
        section.append("<h3>📋 Thông tin đặt phòng</h3>");
        section.append("<table class='info-table'>");
        section.append(String.format("<tr><th>Mã đặt phòng</th><td>#%d</td></tr>", booking.getId()));
        section.append(String.format("<tr><th>Khách sạn</th><td>%s</td></tr>", hotel.getName()));
        section.append(String.format("<tr><th>Địa chỉ khách sạn</th><td>%s</td></tr>", hotel.getAddress() != null ? hotel.getAddress() : "Chưa cập nhật"));
        section.append(String.format("<tr><th>Điện thoại khách sạn</th><td>%s</td></tr>", hotel.getPhone() != null ? hotel.getPhone() : "Chưa cập nhật"));
        section.append(String.format("<tr><th>Loại phòng</th><td>%s</td></tr>", booking.getRoomType() != null ? booking.getRoomType().getName() : "Chưa xác định"));
        
        if (booking.getRoom() != null) {
            section.append(String.format("<tr><th>Số phòng</th><td>%s</td></tr>", booking.getRoom().getRoomNumber()));
        }
        
        section.append(String.format("<tr><th>Khách hàng</th><td>%s</td></tr>", booking.getCustomerName()));
        section.append(String.format("<tr><th>Email</th><td>%s</td></tr>", booking.getEmail()));
        section.append(String.format("<tr><th>Điện thoại</th><td>%s</td></tr>", booking.getPhone()));
        
        if (booking.getAddress() != null && !booking.getAddress().trim().isEmpty()) {
            section.append(String.format("<tr><th>Địa chỉ</th><td>%s</td></tr>", booking.getAddress()));
        }
        
        section.append(String.format("<tr><th>Ngày nhận phòng</th><td>%s</td></tr>", booking.getCheckInDate().format(dateFormatter)));
        section.append(String.format("<tr><th>Ngày trả phòng</th><td>%s</td></tr>", booking.getCheckOutDate().format(dateFormatter)));
        section.append(String.format("<tr><th>Số đêm</th><td>%d đêm</td></tr>", booking.getTotalNights()));
        section.append(String.format("<tr><th>Số khách</th><td>%d khách</td></tr>", booking.getNumberOfGuests()));
        section.append(String.format("<tr><th>Giá phòng/đêm</th><td>%s VNĐ</td></tr>", currencyFormat.format(booking.getPricePerNight())));
        section.append(String.format("<tr><th>Tổng tiền</th><td><strong style='color: #28a745; font-size: 18px;'>%s VNĐ</strong></td></tr>", currencyFormat.format(booking.getTotalPrice())));
        section.append(String.format("<tr><th>Trạng thái</th><td><strong>%s</strong></td></tr>", booking.getStatusName()));
        
        if (booking.getSpecialRequests() != null && !booking.getSpecialRequests().trim().isEmpty()) {
            section.append(String.format("<tr><th>Yêu cầu đặc biệt</th><td>%s</td></tr>", booking.getSpecialRequests()));
        }
        
        section.append("</table>");
        return section.toString();
    }

    private String getImportantNotesSection(BookHotel booking) {
        return """
            <div style='background-color: #e3f2fd; padding: 15px; border-radius: 5px; margin: 15px 0;'>
                <h4 style='color: #1976d2; margin-top: 0;'>📌 Lưu ý quan trọng:</h4>
                <ul style='margin-bottom: 0;'>
                    <li>Vui lòng lưu lại <strong>mã đặt phòng</strong> để tra cứu thông tin</li>
                    <li>Chúng tôi sẽ liên hệ với bạn qua số điện thoại đã cung cấp để xác nhận đặt phòng</li>
                    <li>Thời gian nhận phòng: từ 14:00, trả phòng: trước 12:00</li>
                    <li>Vui lòng mang theo giấy tờ tùy thân khi làm thủ tục nhận phòng</li>
                </ul>
            </div>
            """;
    }

    private String getEmailFooter() {
        return String.format("""
                    </div>
                    <div class="footer">
                        <p>Cảm ơn bạn đã sử dụng dịch vụ của %s!</p>
                        <p>Email này được gửi tự động, vui lòng không trả lời.</p>
                        <p>Nếu cần hỗ trợ, vui lòng liên hệ với chúng tôi.</p>
                    </div>
                </div>
            </body>
            </html>
            """, appName);
    }
}
