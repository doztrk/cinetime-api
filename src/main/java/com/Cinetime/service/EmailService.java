package com.Cinetime.service;


import com.Cinetime.payload.business.SeatInfo;
import com.Cinetime.payload.dto.request.MailRequest;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender emailSender;

    public void sendPasswordResetEmail(String email, String resetCode) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(email);
            message.setSubject("CineTime Password Reset");
            message.setText("Your password reset code is: " + resetCode +
                    "\n\nPlease use this code to reset your password. " +
                    "If you did not request this reset, please ignore this email.");

            // Add logging before sending
            logger.info("Attempting to send email to: {}", email);

            emailSender.send(message);

            logger.info("Email sent successfully to: {}", email);
        } catch (MailAuthenticationException authEx) {
            logger.error("Mail authentication failed: {}", authEx.getMessage());
            throw new RuntimeException("Failed to authenticate with mail server", authEx);
        } catch (MailSendException sendEx) {
            logger.error("Failed to send mail: {}", sendEx.getMessage());
            throw new RuntimeException("Failed to send email", sendEx);
        } catch (Exception ex) {
            logger.error("Unexpected error in email service: {}", ex.getMessage());
            throw new RuntimeException("Unexpected error when sending email", ex);
        }
    }


    public void sendMail(MailRequest mailRequest) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(mailRequest.getTo());
            message.setSubject(mailRequest.getSubject());
            message.setText(buildEmailContent(mailRequest)); // Extract to method

            emailSender.send(message); // THIS IS MISSING!
            logger.info("Ticket confirmation email sent successfully to: {}", mailRequest.getTo());
        } catch (MailAuthenticationException authEx) {
            logger.error("Mail authentication failed: {}", authEx.getMessage());
            throw new RuntimeException("Failed to authenticate with mail server", authEx);
        } catch (MailSendException sendEx) {
            logger.error("Failed to send mail: {}", sendEx.getMessage());
            throw new RuntimeException("Failed to send email", sendEx);
        } catch (Exception ex) {
            logger.error("Unexpected error in email service: {}", ex.getMessage());
            throw new RuntimeException("Unexpected error when sending email", ex);
        }
    }

    private String buildEmailContent(MailRequest mailRequest) {
        StringBuilder content = new StringBuilder();
        content.append("Sayın Müşterimiz,\n\n");
        content.append("Bilet satın alma işleminiz başarıyla tamamlanmıştır.\n\n");
        content.append("DETAYLAR:\n");
        content.append("Film: ").append(mailRequest.getMovieName()).append("\n");
        content.append("Sinema: ").append(mailRequest.getCinemaName()).append("\n");
        content.append("Adres: ").append(mailRequest.getCinemaAddress()).append("\n");
        content.append("Salon: ").append(mailRequest.getHallName()).append("\n");

        if (mailRequest.getDate() != null) {
            content.append("Tarih: ").append(mailRequest.getDate()).append("\n");
        }
        if (mailRequest.getStartTime() != null) {
            content.append("Saat: ").append(mailRequest.getStartTime()).append("\n").append(" - ").append(mailRequest.getEndTime());
        }

        if (mailRequest.getSeatInfos() != null && !mailRequest.getSeatInfos().isEmpty()) {
            content.append("Koltuklar: ");
            List<String> seatNames = mailRequest.getSeatInfos().stream()
                    .map(SeatInfo::getFullSeatName)
                    .toList();
            content.append(String.join(", ", seatNames)).append("\n");
        }

        content.append("Toplam Tutar: ").append(mailRequest.getTotal()).append("\n\n");
        content.append("Biletlerinizi sinema gişesinden QR kod okutarak alabilirsiniz.\n\n");
        content.append("İyi seyirler dileriz!\n");
        content.append("CineTime Ekibi");

        return content.toString();
    }

}

