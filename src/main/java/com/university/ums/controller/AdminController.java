package com.university.ums.controller;

import com.university.ums.model.Admin;
import com.university.ums.repository.AdminRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/admins")
public class AdminController {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminController(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public List<Admin> getAll() {
        return adminRepository.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Admin> getOne(@PathVariable Long id) {
        Optional<Admin> admin = adminRepository.findById(id);
        if (admin.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(admin.get());
    }

    @PostMapping
    public ResponseEntity<?> create(@RequestBody Admin admin) {
        if (admin.getUsername() == null || admin.getUsername().isBlank()
                || admin.getPassword() == null || admin.getPassword().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "username and password are required"));
        }
        if (adminRepository.existsByUsername(admin.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "username already exists"));
        }
        admin.setId(null);
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        Admin saved = adminRepository.save(admin);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Admin newData) {
        Optional<Admin> found = adminRepository.findById(id);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        Admin admin = found.get();
        if (!admin.getUsername().equals(newData.getUsername()) && adminRepository.existsByUsername(newData.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "username already exists"));
        }
        admin.setName(newData.getName());
        admin.setUsername(newData.getUsername());
        if (newData.getPassword() != null && !newData.getPassword().isBlank()) {
            admin.setPassword(passwordEncoder.encode(newData.getPassword()));
        }
        return ResponseEntity.ok(adminRepository.save(admin));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        if (!adminRepository.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        adminRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }
}
