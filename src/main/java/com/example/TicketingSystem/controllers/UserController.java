package com.example.TicketingSystem.controllers;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class UserController {

    @CrossOrigin(origins = "http://localhost:5173")  // Allow this specific endpoint to be accessed by your frontend
    @GetMapping("/user")
    public Map<String, Object> user(@AuthenticationPrincipal Object principal) {
        // Check if the principal is an instance of OAuth2User (Google OAuth)
        if (principal instanceof OAuth2User) {
            OAuth2User oAuth2User = (OAuth2User) principal;
            return Map.of(
                    "name", oAuth2User.getAttribute("name"),
                    "email", oAuth2User.getAttribute("email")
            );
        }

        // If the principal is not OAuth2User, assume it's a UserDetails (email/password authentication)
        if (principal instanceof UserDetails) {
            UserDetails userDetails = (UserDetails) principal;
            return Map.of(
                    "username", userDetails.getUsername()  // Assuming email is the username
            );
        }

        // If no authenticated principal exists, return a default message
        return Map.of("message", "No authenticated user found");
    }
}
