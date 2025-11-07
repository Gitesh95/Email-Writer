package com.email_writer.email_writer.Controller;

import com.email_writer.email_writer.DTO.EmailRequest;
import com.email_writer.email_writer.Service.EmailGeneratorService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/email")
@CrossOrigin(origins = "*")
public class EmailController {
    private final EmailGeneratorService emailGeneratorService;

    public EmailController(EmailGeneratorService emailGeneratorService) {
        this.emailGeneratorService = emailGeneratorService;
    }

    @PostMapping("/generate")
    public String generateReply(@RequestBody EmailRequest emailRequest) {
        // Service will generate reply, save in DB, and return the reply string
        return emailGeneratorService.generateEmailReply(emailRequest);
    }
}
