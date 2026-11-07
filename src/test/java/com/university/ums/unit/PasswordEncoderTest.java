package com.university.ums.unit;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class PasswordEncoderTest {

    @Test
    void testPasswordEncodingAndMatching() {
        PasswordEncoder encoder = new BCryptPasswordEncoder();
        String rawPassword = "mypassword123";

        String encodedPassword = encoder.encode(rawPassword);

        // Encrypted password must not equal raw password
        assertNotEquals(rawPassword, encodedPassword);
        // Encrypted password must verify against the original raw password
        assertTrue(encoder.matches(rawPassword, encodedPassword));
    }
}

