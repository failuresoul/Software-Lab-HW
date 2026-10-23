package com.university.ums.controller;

import com.university.ums.model.Course;
import com.university.ums.model.Teacher;
import com.university.ums.repository.CourseRepository;
import com.university.ums.repository.TeacherRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/courses")
public class CourseController {

    private final CourseRepository courseRepository;
    private final TeacherRepository teacherRepository;

    public CourseController(CourseRepository courseRepository, TeacherRepository teacherRepository) {
        this.courseRepository = courseRepository;
        this.teacherRepository = teacherRepository;
    }

    // READ all -> 200  (everyone can see all courses)
    @GetMapping
    public List<Course> getAll() {
        return courseRepository.findAll();
    }

    // READ one -> 200 or 404
    @GetMapping("/{id}")
    public ResponseEntity<Course> getOne(@PathVariable Long id) {
        Optional<Course> course = courseRepository.findById(id);
        if (course.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(course.get());
    }

    // CREATE -> 201, 400 or 409  (a teacher's new course is their own, the admin chooses the teacher)
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Course course, Authentication auth) {
        if (isBlank(course.getCode()) || isBlank(course.getTitle())) {
            return ResponseEntity.badRequest().body(Map.of("error", "course code and title are required"));
        }
        if (courseRepository.existsByCode(course.getCode())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "course code already exists"));
        }
        Optional<Teacher> teacher = findTeacher(course, auth);
        if (teacher.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "teacher not found"));
        }
        course.setId(null);
        course.setTeacher(teacher.get());
        return ResponseEntity.status(HttpStatus.CREATED).body(courseRepository.save(course));
    }

    // UPDATE -> 200, 400, 403, 404 or 409  (a teacher can only update their own courses)
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Course newData, Authentication auth) {
        Optional<Course> found = courseRepository.findById(id);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Course course = found.get();
        if (!isAdmin(auth) && !course.getTeacher().getUsername().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "you can only edit your own courses"));
        }
        if (isBlank(newData.getCode()) || isBlank(newData.getTitle())) {
            return ResponseEntity.badRequest().body(Map.of("error", "course code and title are required"));
        }
        if (!course.getCode().equals(newData.getCode()) && courseRepository.existsByCode(newData.getCode())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "course code already exists"));
        }
        // only the admin can give the course to another teacher
        if (isAdmin(auth) && newData.getTeacher() != null) {
            Optional<Teacher> teacher = findTeacher(newData, auth);
            if (teacher.isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("error", "teacher not found"));
            }
            course.setTeacher(teacher.get());
        }
        course.setCode(newData.getCode());
        course.setTitle(newData.getTitle());
        course.setCredit(newData.getCredit());
        course.setSemester(newData.getSemester());
        return ResponseEntity.ok(courseRepository.save(course));
    }

    // DELETE -> 204, 403 or 404  (a teacher can only delete their own courses, registrations are deleted too)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        Optional<Course> found = courseRepository.findById(id);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!isAdmin(auth) && !found.get().getTeacher().getUsername().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "you can only delete your own courses"));
        }
        courseRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // the admin chooses the teacher by username, a teacher is always the teacher of their own course
    private Optional<Teacher> findTeacher(Course data, Authentication auth) {
        if (!isAdmin(auth)) {
            return teacherRepository.findByUsername(auth.getName());
        }
        if (data.getTeacher() == null) {
            return Optional.empty();
        }
        return teacherRepository.findByUsername(data.getTeacher().getUsername());
    }

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
