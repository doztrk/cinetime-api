package com.Cinetime.service;


import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


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


}
