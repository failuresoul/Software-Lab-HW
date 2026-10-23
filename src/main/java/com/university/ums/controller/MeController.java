package com.university.ums.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class MeController {

    // tells the web page who is logged in and what role they have
    @GetMapping("/api/me")
    public Map<String, String> me(Authentication authentication) {
        String role = authentication.getAuthorities().iterator().next().getAuthority().replace("ROLE_", "");
        return Map.of("username", authentication.getName(), "role", role);
    }
}
