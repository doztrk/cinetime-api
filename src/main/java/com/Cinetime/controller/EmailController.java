package com.Cinetime.controller;

import com.Cinetime.payload.dto.request.MailRequest;
import com.Cinetime.service.EmailService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/mail")
@RequiredArgsConstructor
public class EmailController {

    private final EmailService emailService;

    @PostMapping("/send")
    public void sendMail(@RequestBody @Valid MailRequest mailRequest) {
        emailService.sendMail(mailRequest);
    }
}
