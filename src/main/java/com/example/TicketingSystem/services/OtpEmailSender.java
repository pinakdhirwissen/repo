package com.example.TicketingSystem.services;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.thymeleaf.spring6.SpringTemplateEngine;
import org.thymeleaf.context.Context;

import java.util.Map;

@Component
public class OtpEmailSender {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    public void sendOtpEmail(String toEmail, Map<String, Object> model) throws MessagingException {
        Context context = new Context();
        context.setVariables(model);

        String html = templateEngine.process("otpTemplate", context); // make sure otpTemplate.html exists in /resources/templates

        MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage);
            helper.setTo(toEmail);
            helper.setSubject("Your OTP Code");
            helper.setText(html, true);
            mailSender.send(mimeMessage);

    }
}