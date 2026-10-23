package com.university.ums.controller;

import com.university.ums.model.Course;
import com.university.ums.model.Registration;
import com.university.ums.model.Student;
import com.university.ums.repository.CourseRepository;
import com.university.ums.repository.RegistrationRepository;
import com.university.ums.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/registrations")
public class RegistrationController {

    private final RegistrationRepository registrationRepository;
    private final CourseRepository courseRepository;
    private final StudentRepository studentRepository;

    public RegistrationController(RegistrationRepository registrationRepository,
                                  CourseRepository courseRepository,
                                  StudentRepository studentRepository) {
        this.registrationRepository = registrationRepository;
        this.courseRepository = courseRepository;
        this.studentRepository = studentRepository;
    }

    // READ all -> 200  (admin: all, teacher: registrations in their courses, student: their own)
    @GetMapping
    public List<Registration> getAll(Authentication auth) {
        if (hasRole(auth, "ADMIN")) {
            return registrationRepository.findAll();
        }
        if (hasRole(auth, "TEACHER")) {
            return registrationRepository.findByCourseTeacherUsername(auth.getName());
        }
        return registrationRepository.findByStudentUsername(auth.getName());
    }

    // READ one -> 200, 403 or 404
    @GetMapping("/{id}")
    public ResponseEntity<?> getOne(@PathVariable Long id, Authentication auth) {
        Optional<Registration> registration = registrationRepository.findById(id);
        if (registration.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!canSee(registration.get(), auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "this is not your registration"));
        }
        return ResponseEntity.ok(registration.get());
    }

    // CREATE -> 201, 400 or 409  (a student registers themself, the admin chooses the student)
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Registration registration, Authentication auth) {
        if (isBlank(registration.getSection())) {
            return ResponseEntity.badRequest().body(Map.of("error", "section is required"));
        }
        Optional<Course> course = Optional.empty();
        if (registration.getCourse() != null) {
            course = courseRepository.findByCode(registration.getCourse().getCode());
        }
        if (course.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "course not found"));
        }
        Optional<Student> student = findStudent(registration, auth);
        if (student.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "student not found"));
        }
        if (registrationRepository.existsByStudentAndCourse(student.get(), course.get())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "already registered in this course"));
        }
        registration.setId(null);
        registration.setCourse(course.get());
        registration.setStudent(student.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(registrationRepository.save(registration));
    }

    // UPDATE -> 200, 400, 403 or 404  (only the section can be changed, a student only in their own registrations)
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Registration newData, Authentication auth) {
        Optional<Registration> found = registrationRepository.findById(id);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Registration registration = found.get();
        if (!isOwnerOrAdmin(registration, auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "you can only change your own registrations"));
        }
        if (isBlank(newData.getSection())) {
            return ResponseEntity.badRequest().body(Map.of("error", "section is required"));
        }
        registration.setSection(newData.getSection());
        return ResponseEntity.ok(registrationRepository.save(registration));
    }

    // DELETE -> 204, 403 or 404  (drop a course: a student only their own registrations)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        Optional<Registration> found = registrationRepository.findById(id);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!isOwnerOrAdmin(found.get(), auth)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "you can only drop your own registrations"));
        }
        registrationRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // the admin chooses the student by username, a student always registers themself
    private Optional<Student> findStudent(Registration data, Authentication auth) {
        if (!hasRole(auth, "ADMIN")) {
            return studentRepository.findByUsername(auth.getName());
        }
        if (data.getStudent() == null) {
            return Optional.empty();
        }
        return studentRepository.findByUsername(data.getStudent().getUsername());
    }

    private boolean isOwnerOrAdmin(Registration registration, Authentication auth) {
        return hasRole(auth, "ADMIN") || registration.getStudent().getUsername().equals(auth.getName());
    }

    // admin: every registration, teacher: registrations in their courses, student: their own
    private boolean canSee(Registration registration, Authentication auth) {
        if (hasRole(auth, "ADMIN")) {
            return true;
        }
        if (hasRole(auth, "TEACHER")) {
            return registration.getCourse().getTeacher().getUsername().equals(auth.getName());
        }
        return registration.getStudent().getUsername().equals(auth.getName());
    }

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    private boolean hasRole(Authentication auth, String role) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_" + role));
    }
}
