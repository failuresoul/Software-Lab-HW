package com.university.ums.integration;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class StudentIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Integration Test 2: Designed to fail to show GitHub Action error
    @Test
    @DisplayName("Integration Test 2: GET /api/students (Intentional Error)")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllStudentsWithError() throws Exception {
        // The endpoint returns 200 OK, but we assert 404 NOT FOUND so the test fails
        mockMvc.perform(get("/api/students")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound()); // <--- INTENTIONAL ERROR: Expecting 404, will receive 200
    }
}

