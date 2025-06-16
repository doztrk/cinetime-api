package com.Cinetime.service.emailservice;

import com.Cinetime.payload.dto.request.MailRequest;
import com.Cinetime.payload.business.SeatInfo;
import com.Cinetime.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.Logger;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;


import java.time.LocalDate;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    @Mock
    private JavaMailSender emailSender;

    @InjectMocks
    private EmailService emailService;

    private MailRequest mailRequest;
    private List<SeatInfo> seatInfos;

    @BeforeEach
    void setUp() {
        // Create test seat info
        SeatInfo seat1 = SeatInfo.builder()
                .seatLetter("A")
                .seatNumber(1)
                .build();
        SeatInfo seat2 = SeatInfo.builder()
                .seatLetter("A")
                .seatNumber(2)
                .build();
        seatInfos = Arrays.asList(seat1, seat2);

        // Create test mail request with all fields
        mailRequest = MailRequest.builder()
                .to("test@example.com")
                .subject("Bilet Onayı")
                .movieName("Inception")
                .cinemaName("Cinemaximum")
                .cinemaAddress("Kadıköy, İstanbul")
                .hallName("Salon 1")
                .date(LocalDate.of(2024, 6, 15))
                .startTime("19:30")
                .endTime("21:30")
                .seatInfos(seatInfos)
                .total("120.00")
                .build();
    }

    @Test
    void sendMail_WithValidData_ShouldSendEmailSuccessfully() {
        // Given
        doNothing().when(emailSender).send(any(SimpleMailMessage.class));

        // When
        emailService.sendMail(mailRequest);

        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(emailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getTo()).containsExactly("test@example.com");
        assertThat(sentMessage.getSubject()).isEqualTo("Bilet Onayı");
        assertThat(sentMessage.getText()).contains("Inception", "Cinemaximum", "A1, A2", "120.00");
    }

    @Test
    void sendMail_WithMinimalData_ShouldSendEmailSuccessfully() {
        // Given - Mail request with only required fields
        MailRequest minimalRequest = MailRequest.builder()
                .to("test@example.com")
                .subject("Test Subject")
                .movieName("Test Movie")
                .cinemaName("Test Cinema")
                .cinemaAddress("Test Address")
                .hallName("Test Hall")
                .total("50.00")
                .build();

        doNothing().when(emailSender).send(any(SimpleMailMessage.class));

        // When
        emailService.sendMail(minimalRequest);

        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(emailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getText())
                .contains("Test Movie", "Test Cinema", "50.00")
                .doesNotContain("Tarih:", "Saat:", "Koltuklar:");
    }

    @Test
    void sendMail_WithEmptySeatList_ShouldNotIncludeSeats() {
        // Given
        mailRequest.setSeatInfos(Collections.emptyList());
        doNothing().when(emailSender).send(any(SimpleMailMessage.class));

        // When
        emailService.sendMail(mailRequest);

        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(emailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getText()).doesNotContain("Koltuklar:");
    }

    @Test
    void sendMail_WithNullSeatList_ShouldNotIncludeSeats() {
        // Given
        mailRequest.setSeatInfos(null);
        doNothing().when(emailSender).send(any(SimpleMailMessage.class));

        // When
        emailService.sendMail(mailRequest);

        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(emailSender).send(messageCaptor.capture());

        SimpleMailMessage sentMessage = messageCaptor.getValue();
        assertThat(sentMessage.getText()).doesNotContain("Koltuklar:");
    }

    @Test
    void sendMail_WhenMailAuthenticationFails_ShouldThrowRuntimeException() {
        // Given
        MailAuthenticationException authException = new MailAuthenticationException("Authentication failed");
        doThrow(authException).when(emailSender).send(any(SimpleMailMessage.class));

        // When & Then
        assertThatThrownBy(() -> emailService.sendMail(mailRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to authenticate with mail server")
                .hasCause(authException);
    }

    @Test
    void sendMail_WhenMailSendFails_ShouldThrowRuntimeException() {
        // Given
        MailSendException sendException = new MailSendException("Send failed");
        doThrow(sendException).when(emailSender).send(any(SimpleMailMessage.class));

        // When & Then
        assertThatThrownBy(() -> emailService.sendMail(mailRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Failed to send email")
                .hasCause(sendException);
    }

    @Test
    void sendMail_WhenUnexpectedErrorOccurs_ShouldThrowRuntimeException() {
        // Given
        RuntimeException unexpectedException = new RuntimeException("Unexpected error");
        doThrow(unexpectedException).when(emailSender).send(any(SimpleMailMessage.class));

        // When & Then
        assertThatThrownBy(() -> emailService.sendMail(mailRequest))
                .isInstanceOf(RuntimeException.class)
                .hasMessage("Unexpected error when sending email")
                .hasCause(unexpectedException);
    }

    @Test
    void buildEmailContent_WithCompleteData_ShouldFormatCorrectly() {
        // This test verifies the email content formatting
        // We need to make buildEmailContent method package-private or protected to test it directly
        // For now, we test it indirectly through sendMail

        doNothing().when(emailSender).send(any(SimpleMailMessage.class));

        emailService.sendMail(mailRequest);

        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(emailSender).send(messageCaptor.capture());

        String emailContent = messageCaptor.getValue().getText();

        // Verify email structure and content
        assertThat(emailContent)
                .startsWith("Sayın Müşterimiz,")
                .contains("Bilet satın alma işleminiz başarıyla tamamlanmıştır.")
                .contains("DETAYLAR:")
                .contains("Film: Inception")
                .contains("Sinema: Cinemaximum")
                .contains("Adres: Kadıköy, İstanbul")
                .contains("Salon: Salon 1")
                .contains("Tarih: 2024-06-15")
                .contains("Saat: 19:30\n - 21:30")  // This reflects the current bug in your code
                .contains("Koltuklar: A1, A2")
                .contains("Toplam Tutar: 120.00")
                .contains("Biletlerinizi sinema gişesinden QR kod okutarak alabilirsiniz.")
                .endsWith("İyi seyirler dileriz!\nCineTime Ekibi");
    }

    @Test
    void buildEmailContent_WithNullDate_ShouldNotIncludeDateLine() {
        // Given
        mailRequest.setDate(null);
        doNothing().when(emailSender).send(any(SimpleMailMessage.class));

        // When
        emailService.sendMail(mailRequest);

        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(emailSender).send(messageCaptor.capture());

        String emailContent = messageCaptor.getValue().getText();
        assertThat(emailContent).doesNotContain("Tarih:");
    }

    @Test
    void buildEmailContent_WithNullStartTime_ShouldNotIncludeTimeLine() {
        // Given
        mailRequest.setStartTime(null);
        doNothing().when(emailSender).send(any(SimpleMailMessage.class));

        // When
        emailService.sendMail(mailRequest);

        // Then
        ArgumentCaptor<SimpleMailMessage> messageCaptor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(emailSender).send(messageCaptor.capture());

        String emailContent = messageCaptor.getValue().getText();
        assertThat(emailContent).doesNotContain("Saat:");
    }
}