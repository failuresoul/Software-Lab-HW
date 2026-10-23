package com.university.ums.controller;

import com.university.ums.model.Student;
import com.university.ums.repository.StudentRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/students")
public class StudentController {

    private final StudentRepository studentRepository;
    private final PasswordEncoder passwordEncoder;

    public StudentController(StudentRepository studentRepository, PasswordEncoder passwordEncoder) {
        this.studentRepository = studentRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // READ all -> 200
    @GetMapping
    public List<Student> getAll() {
        return studentRepository.findAll();
    }

    // READ one -> 200 or 404
    @GetMapping("/{id}")
    public ResponseEntity<Student> getOne(@PathVariable Long id) {
        Optional<Student> student = studentRepository.findById(id);
        if (student.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(student.get());
    }

    // CREATE -> 201, 400 or 409
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Student student) {
        if (isBlank(student.getName()) || isBlank(student.getUsername()) || isBlank(student.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "name, username and password are required"));
        }
        if (studentRepository.existsByUsername(student.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "username already exists"));
        }
        student.setId(null);
        student.setPassword(passwordEncoder.encode(student.getPassword()));
        return ResponseEntity.status(HttpStatus.CREATED).body(studentRepository.save(student));
    }

    // UPDATE -> 200, 400, 403, 404 or 409  (a student can only update their own account)
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Student newData, Authentication auth) {
        Optional<Student> found = studentRepository.findById(id);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Student student = found.get();
        if (!isAdmin(auth) && !student.getUsername().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "you can only edit your own account"));
        }
        if (isBlank(newData.getName()) || isBlank(newData.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("error", "name and username are required"));
        }
        if (!student.getUsername().equals(newData.getUsername()) && studentRepository.existsByUsername(newData.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "username already exists"));
        }
        student.setName(newData.getName());
        student.setEmail(newData.getEmail());
        student.setRoll(newData.getRoll());
        student.setDepartment(newData.getDepartment());
        student.setUsername(newData.getUsername());
        if (!isBlank(newData.getPassword())) {
            student.setPassword(passwordEncoder.encode(newData.getPassword()));
        }
        return ResponseEntity.ok(studentRepository.save(student));
    }

    // DELETE -> 204, 403 or 404  (a student can only delete their own account)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        Optional<Student> found = studentRepository.findById(id);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!isAdmin(auth) && !found.get().getUsername().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "you can only delete your own account"));
        }
        studentRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
