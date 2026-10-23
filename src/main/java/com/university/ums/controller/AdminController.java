package com.university.ums.controller;

import com.university.ums.model.Admin;
import com.university.ums.repository.AdminRepository;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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

    // READ all -> 200
    @GetMapping
    public List<Admin> getAll() {
        return adminRepository.findAll();
    }

    // READ one -> 200 or 404
    @GetMapping("/{id}")
    public ResponseEntity<Admin> getOne(@PathVariable Long id) {
        Optional<Admin> admin = adminRepository.findById(id);
        if (admin.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(admin.get());
    }

    // CREATE -> 201, 400 or 409
    @PostMapping
    public ResponseEntity<?> create(@RequestBody Admin admin) {
        if (isBlank(admin.getName()) || isBlank(admin.getUsername()) || isBlank(admin.getPassword())) {
            return ResponseEntity.badRequest().body(Map.of("error", "name, username and password are required"));
        }
        if (adminRepository.existsByUsername(admin.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "username already exists"));
        }
        admin.setId(null);
        admin.setPassword(passwordEncoder.encode(admin.getPassword()));
        return ResponseEntity.status(HttpStatus.CREATED).body(adminRepository.save(admin));
    }

    // UPDATE -> 200, 400, 404 or 409
    @PutMapping("/{id}")
    public ResponseEntity<?> update(@PathVariable Long id, @RequestBody Admin newData) {
        Optional<Admin> found = adminRepository.findById(id);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (isBlank(newData.getName()) || isBlank(newData.getUsername())) {
            return ResponseEntity.badRequest().body(Map.of("error", "name and username are required"));
        }
        Admin admin = found.get();
        if (!admin.getUsername().equals(newData.getUsername()) && adminRepository.existsByUsername(newData.getUsername())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body(Map.of("error", "username already exists"));
        }
        admin.setName(newData.getName());
        admin.setEmail(newData.getEmail());
        admin.setUsername(newData.getUsername());
        if (!isBlank(newData.getPassword())) {
            admin.setPassword(passwordEncoder.encode(newData.getPassword()));
        }
        return ResponseEntity.ok(adminRepository.save(admin));
    }

    // DELETE -> 204, 400 or 404  (an admin cannot delete their own account)
    @DeleteMapping("/{id}")
    public ResponseEntity<?> delete(@PathVariable Long id, Authentication auth) {
        Optional<Admin> found = adminRepository.findById(id);
        if (found.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        if (found.get().getUsername().equals(auth.getName())) {
            return ResponseEntity.badRequest().body(Map.of("error", "you cannot delete your own account"));
        }
        adminRepository.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    private boolean isBlank(String text) {
        return text == null || text.isBlank();
    }
}
