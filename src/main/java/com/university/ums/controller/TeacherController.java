package com.university.ums.controller;

import com.university.ums.model.Teacher;
import com.university.ums.repository.TeacherRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/teachers")
public class TeacherController {

    private final TeacherRepository teacherRepository;
    private final PasswordEncoder passwordEncoder;

    public TeacherController(TeacherRepository teacherRepository, PasswordEncoder passwordEncoder) {
        this.teacherRepository = teacherRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // READ all -> 200
    @GetMapping
    public List<Teacher> getAll() {
        return teacherRepository.findAll();
    }

    // READ one -> 200 or 404
    @GetMapping("/{id}")
    public ResponseEntity<Teacher> getOne(@PathVariable Long id) {
        Optional<Teacher> teacher = teacherRepository.findById(id);
        if (teacher.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(teacher.get());
    }

    // CREATE -> 201, 400 or 409
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Teacher teacher) {
        if (isBlank(teacher.getName()) || isBlank(teacher.getUsername()) || isBlank(teacher.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "name, username and password are required"));
        }
        if (teacherRepository.existsByUsername(teacher.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "username already exists"));
        }
        teacher.setId(null);
        teacher.setPassword(passwordEncoder.encode(teacher.getPassword()));
        return ResponseEntity.status(HttpStatus.CREATED).body(teacherRepository.save(teacher));
    }

    // UPDATE -> 200, 400, 403, 404 or 409  (a teacher can only update their own account)
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Teacher newData, Authentication auth) {
        Optional<Teacher> found = teacherRepository.findById(id);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Teacher teacher = found.get();
        if (!isAdmin(auth) && !teacher.getUsername().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "you can only edit your own account"));
        }
        if (isBlank(newData.getName()) || isBlank(newData.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("error", "name and username are required"));
        }
        if (!teacher.getUsername().equals(newData.getUsername()) && teacherRepository.existsByUsername(newData.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "username already exists"));
        }
        teacher.setName(newData.getName());
        teacher.setEmail(newData.getEmail());
        teacher.setDepartment(newData.getDepartment());
        teacher.setDesignation(newData.getDesignation());
        teacher.setUsername(newData.getUsername());
        if (!isBlank(newData.getPassword())) {
            teacher.setPassword(passwordEncoder.encode(newData.getPassword()));
        }
        return ResponseEntity.ok(teacherRepository.save(teacher));
    }

    // DELETE -> 204, 403 or 404  (a teacher can only delete their own account)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        Optional<Teacher> found = teacherRepository.findById(id);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (!isAdmin(auth) && !found.get().getUsername().equals(auth.getName())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of("error", "you can only delete your own account"));
        }
        teacherRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }

    private boolean isAdmin(Authentication auth) {
        return auth.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
    }
}
