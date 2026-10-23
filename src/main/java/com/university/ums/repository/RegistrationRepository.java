package com.university.ums.repository;

import com.university.ums.model.Course;
import com.university.ums.model.Registration;
import com.university.ums.model.Student;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RegistrationRepository extends JpaRepository<Registration, Long> {

    // registrations of one student
    List<Registration> findByStudentUsername(String username);

    // registrations in the courses of one teacher
    List<Registration> findByCourseTeacherUsername(String username);

    boolean existsByStudentAndCourse(Student student, Course course);
}
