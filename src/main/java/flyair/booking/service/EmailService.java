package flyair.booking.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    /**
     * Send simple text email
     */
    public void sendSimpleEmail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            
            mailSender.send(message);
            log.info("Simple email sent successfully to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send simple email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    /**
     * Send HTML email
     */
    public void sendHtmlEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            log.info("HTML email sent successfully to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {}", to, e);
            throw new RuntimeException("Failed to send email", e);
        }
    }
    
    /**
     * Send password reset email
     */
    public void sendPasswordResetEmail(String to, String resetToken, String userName) {
        String subject = "FlyAir - Password Reset Request";
        String resetUrl = "http://localhost:5173/reset-password?token=" + resetToken;
        
        String htmlContent = buildPasswordResetEmailContent(userName, resetUrl);
        sendHtmlEmail(to, subject, htmlContent);
    }
    
    /**
     * Send booking confirmation email
     */
    public void sendBookingConfirmationEmail(String to, String userName, String bookingReference, 
                                           String flightNumber, String departureTime) {
        String subject = "FlyAir - Booking Confirmation #" + bookingReference;
        String htmlContent = buildBookingConfirmationEmailContent(userName, bookingReference, 
                                                                 flightNumber, departureTime);
        sendHtmlEmail(to, subject, htmlContent);
    }
    
    /**
     * Send two-factor authentication setup email
     */
    public void send2FASetupEmail(String to, String userName) {
        String subject = "FlyAir - Two-Factor Authentication Enabled";
        String htmlContent = build2FASetupEmailContent(userName);
        sendHtmlEmail(to, subject, htmlContent);
    }
    
    /**
     * Send welcome email for new users
     */
    public void sendWelcomeEmail(String to, String userName) {
        String subject = "Welcome to FlyAir - Your Account is Ready!";
        String htmlContent = buildWelcomeEmailContent(userName);
        sendHtmlEmail(to, subject, htmlContent);
    }
    
    private String buildPasswordResetEmailContent(String userName, String resetUrl) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #007bff; color: white; padding: 20px; text-align: center; }" +
                ".content { padding: 20px; background-color: #f9f9f9; }" +
                ".button { display: inline-block; background-color: #007bff; color: white; padding: 12px 24px; " +
                "text-decoration: none; border-radius: 4px; margin: 20px 0; }" +
                ".footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>FlyAir</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<h2>Password Reset Request</h2>" +
                "<p>Hello " + userName + ",</p>" +
                "<p>We received a request to reset your password. Click the button below to create a new password:</p>" +
                "<a href='" + resetUrl + "' class='button'>Reset Password</a>" +
                "<p>This link will expire in 1 hour for security purposes.</p>" +
                "<p>If you didn't request this password reset, please ignore this email.</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>&copy; 2024 FlyAir. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
    
    private String buildBookingConfirmationEmailContent(String userName, String bookingReference, 
                                                      String flightNumber, String departureTime) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #28a745; color: white; padding: 20px; text-align: center; }" +
                ".content { padding: 20px; background-color: #f9f9f9; }" +
                ".booking-details { background-color: white; padding: 15px; border-radius: 4px; margin: 15px 0; }" +
                ".footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>FlyAir</h1>" +
                "<h2>Booking Confirmed!</h2>" +
                "</div>" +
                "<div class='content'>" +
                "<p>Hello " + userName + ",</p>" +
                "<p>Your flight booking has been confirmed successfully!</p>" +
                "<div class='booking-details'>" +
                "<h3>Booking Details</h3>" +
                "<p><strong>Booking Reference:</strong> " + bookingReference + "</p>" +
                "<p><strong>Flight Number:</strong> " + flightNumber + "</p>" +
                "<p><strong>Departure Time:</strong> " + departureTime + "</p>" +
                "</div>" +
                "<p>Please save this confirmation for your records. You can check in online 24 hours before departure.</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>&copy; 2024 FlyAir. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
    
    private String build2FASetupEmailContent(String userName) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #6f42c1; color: white; padding: 20px; text-align: center; }" +
                ".content { padding: 20px; background-color: #f9f9f9; }" +
                ".footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>FlyAir</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<h2>Two-Factor Authentication Enabled</h2>" +
                "<p>Hello " + userName + ",</p>" +
                "<p>Two-factor authentication has been successfully enabled on your FlyAir account.</p>" +
                "<p>Your account is now more secure. You'll need to enter a verification code from your authenticator app each time you log in.</p>" +
                "<p>If you didn't enable this feature, please contact our support team immediately.</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>&copy; 2024 FlyAir. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
    
    private String buildWelcomeEmailContent(String userName) {
        return "<!DOCTYPE html>" +
                "<html>" +
                "<head>" +
                "<style>" +
                "body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }" +
                ".container { max-width: 600px; margin: 0 auto; padding: 20px; }" +
                ".header { background-color: #17a2b8; color: white; padding: 20px; text-align: center; }" +
                ".content { padding: 20px; background-color: #f9f9f9; }" +
                ".footer { text-align: center; padding: 20px; font-size: 12px; color: #666; }" +
                "</style>" +
                "</head>" +
                "<body>" +
                "<div class='container'>" +
                "<div class='header'>" +
                "<h1>Welcome to FlyAir!</h1>" +
                "</div>" +
                "<div class='content'>" +
                "<h2>Your Account is Ready</h2>" +
                "<p>Hello " + userName + ",</p>" +
                "<p>Welcome to FlyAir! Your account has been successfully created.</p>" +
                "<p>You can now:</p>" +
                "<ul>" +
                "<li>Search and book flights</li>" +
                "<li>Manage your bookings</li>" +
                "<li>Check in online</li>" +
                "<li>View your travel history</li>" +
                "</ul>" +
                "<p>Thank you for choosing FlyAir for your travel needs!</p>" +
                "</div>" +
                "<div class='footer'>" +
                "<p>&copy; 2024 FlyAir. All rights reserved.</p>" +
                "</div>" +
                "</div>" +
                "</body>" +
                "</html>";
    }
}