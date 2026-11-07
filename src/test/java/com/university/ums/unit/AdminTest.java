package com.university.ums.unit;

import com.university.ums.model.Admin;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class AdminTest {

    @Test
    void testAdminDetails() {
        Admin admin = new Admin();
        admin.setId(1L);
        admin.setName("Main Admin");
        admin.setEmail("admin@university.edu");
        admin.setUsername("admin");
        admin.setPassword("admin123");

        assertEquals(1L, admin.getId());
        assertEquals("Main Admin", admin.getName());
        assertEquals("admin@university.edu", admin.getEmail());
        assertEquals("admin", admin.getUsername());
        assertEquals("admin123", admin.getPassword());
    }
}

