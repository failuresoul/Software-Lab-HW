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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class TeacherIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    // Integration Test 3: Verify GET /api/teachers returns 200 OK and list of teachers
    @Test
    @DisplayName("Integration Test 3: GET /api/teachers should return 200 OK with teachers list")
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void testGetAllTeachers() throws Exception {
        mockMvc.perform(get("/api/teachers")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}

