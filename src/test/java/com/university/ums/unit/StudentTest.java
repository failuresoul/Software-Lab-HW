package com.university.ums.unit;

import com.university.ums.model.Student;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StudentTest {

    @Test
    void testStudentDetails() {
        Student student = new Student();
        student.setId(1L);
        student.setName("Karim Hasan");
        student.setEmail("karim@university.edu");
        student.setRoll("2003001");
        student.setDepartment("CSE");
        student.setUsername("student1");
        student.setPassword("student123");

        assertEquals(1L, student.getId());
        assertEquals("Karim Hasan", student.getName());
        assertEquals("karim@university.edu", student.getEmail());
        assertEquals("2003001", student.getRoll());
        assertEquals("CSE", student.getDepartment());
        assertEquals("student1", student.getUsername());
        assertEquals("student123", student.getPassword());
    }
}

