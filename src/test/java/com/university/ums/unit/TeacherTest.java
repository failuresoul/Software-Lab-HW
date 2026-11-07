package com.university.ums.unit;

import com.university.ums.model.Teacher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class TeacherTest {

    @Test
    void testTeacherDetails() {
        Teacher teacher = new Teacher();
        teacher.setId(1L);
        teacher.setName("Rahim Uddin");
        teacher.setEmail("rahim@university.edu");
        teacher.setDepartment("CSE");
        teacher.setDesignation("Lecturer");
        teacher.setUsername("teacher1");
        teacher.setPassword("teacher123");

        assertEquals(1L, teacher.getId());
        assertEquals("Rahim Uddin", teacher.getName());
        assertEquals("rahim@university.edu", teacher.getEmail());
        assertEquals("CSE", teacher.getDepartment());
        assertEquals("Lecturer", teacher.getDesignation());
        assertEquals("teacher1", teacher.getUsername());
        assertEquals("teacher123", teacher.getPassword());
    }
}

