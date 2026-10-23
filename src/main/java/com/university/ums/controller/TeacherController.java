package com.university.ums.controller;

import com.university.ums.model.Teacher;
import com.university.ums.repository.TeacherRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    @GetMapping
    public List<Teacher> getAll() {
        return teacherRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Teacher> getOne(@PathVariable Long id) {
        Optional<Teacher> teacher = teacherRepository.findById(id);
        if (teacher.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(teacher.get());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Teacher teacher) {
        if (teacher.getUsername() == null || teacher.getUsername().isBlank()
                || teacher.getPassword() == null || teacher.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "username and password are required"));
        }
        if (teacherRepository.existsByUsername(teacher.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "username already exists"));
        }
        teacher.setId(null);
        teacher.setPassword(passwordEncoder.encode(teacher.getPassword()));
        Teacher saved = teacherRepository.save(teacher);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Teacher newData) {
        Optional<Teacher> found = teacherRepository.findById(id);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Teacher teacher = found.get();
        if (!teacher.getUsername().equals(newData.getUsername()) && teacherRepository.existsByUsername(newData.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "username already exists"));
        }
        teacher.setName(newData.getName());
        teacher.setUsername(newData.getUsername());
        teacher.setSubject(newData.getSubject());
        if (newData.getPassword() != null && !newData.getPassword().isBlank()) {
            teacher.setPassword(passwordEncoder.encode(newData.getPassword()));
        }
        return ResponseEntity.ok(teacherRepository.save(teacher));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!teacherRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        teacherRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
