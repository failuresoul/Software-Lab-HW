package com.university.ums.unit;

import com.university.ums.model.Course;
import com.university.ums.model.Registration;
import com.university.ums.model.Student;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RegistrationTest {

    @Test
    void testRegistrationDetails() {
        Student student = new Student();
        student.setUsername("student1");

        Course course = new Course();
        course.setCode("CSE3230");

        Registration registration = new Registration();
        registration.setId(1L);
        registration.setStudent(student);
        registration.setCourse(course);
        registration.setSection("A");

        assertEquals(1L, registration.getId());
        assertEquals(student, registration.getStudent());
        assertEquals(course, registration.getCourse());
        assertEquals("A", registration.getSection());
    }
}

