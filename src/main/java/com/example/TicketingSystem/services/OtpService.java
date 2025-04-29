package com.example.TicketingSystem.services;

import com.example.TicketingSystem.services.OtpEmailSender;
import jakarta.mail.MessagingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private Map<String, String> otpStore = new ConcurrentHashMap<>();

    @Autowired
    private OtpEmailSender otpEmailSender;

    public void sendOtp(String email) throws MessagingException {
        String otp = String.valueOf(new Random().nextInt(900000) + 100000);
        otpStore.put(email, otp);

        Map<String, Object> model = new HashMap<>();
        model.put("otp", otp);

        otpEmailSender.sendOtpEmail(email, model);
    }

    public boolean verifyOtp(String email, String otp) {
        String storedOtp = otpStore.get(email);
        if (storedOtp != null && storedOtp.equals(otp)) {
            otpStore.remove(email);
            return true;
        }
        return false;
    }
}