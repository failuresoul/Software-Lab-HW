package com.university.ums.unit;

import com.university.ums.model.Course;
import com.university.ums.model.Teacher;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CourseTest {

    @Test
    void testCourseDetailsAndTeacherAssignment() {
        Teacher teacher = new Teacher();
        teacher.setUsername("teacher1");

        Course course = new Course();
        course.setId(1L);
        course.setCode("CSE3230");
        course.setTitle("Software Development Lab");
        course.setCredit(1.5);
        course.setSemester("3-2");
        course.setTeacher(teacher);

        assertEquals(1L, course.getId());
        assertEquals("CSE3230", course.getCode());
        assertEquals("Software Development Lab", course.getTitle());
        assertEquals(1.5, course.getCredit());
        assertEquals("3-2", course.getSemester());
        assertNotNull(course.getTeacher());
        assertEquals("teacher1", course.getTeacher().getUsername());
    }
}

