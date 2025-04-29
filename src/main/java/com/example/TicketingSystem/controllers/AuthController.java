package com.example.TicketingSystem.controllers;
import com.example.TicketingSystem.dto.EmailRequest;
import com.example.TicketingSystem.dto.OtpRequest;
import com.example.TicketingSystem.services.OtpService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private OtpService otpService;
    @CrossOrigin(origins = "http://localhost:5173")
    @PostMapping("/request-otp")
    public ResponseEntity<String> requestOtp(@RequestBody EmailRequest emailRequest) throws MessagingException {
        otpService.sendOtp(emailRequest.getEmail());
        return ResponseEntity.ok("OTP sent to your email.");
    }
    @CrossOrigin(origins = "http://localhost:5173")
    @PostMapping("/verify-otp")
    public ResponseEntity<String> verifyOtp(@RequestBody OtpRequest otpRequest) {
        boolean isVerified = otpService.verifyOtp(otpRequest.getEmail(), otpRequest.getOtp());
        if (isVerified) {
            // Here generate JWT or session
            return ResponseEntity.ok("Sign in successful.");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid OTP.");
        }
    }
}