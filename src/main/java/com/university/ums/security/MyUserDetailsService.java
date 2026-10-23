package com.university.ums.security;

import com.university.ums.model.Admin;
import com.university.ums.model.Student;
import com.university.ums.model.Teacher;
import com.university.ums.repository.AdminRepository;
import com.university.ums.repository.StudentRepository;
import com.university.ums.repository.TeacherRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.Optional;

@Service
public class MyUserDetailsService implements UserDetailsService {

    private final AdminRepository adminRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;

    public MyUserDetailsService(AdminRepository adminRepository,
                                TeacherRepository teacherRepository,
                                StudentRepository studentRepository) {
        this.adminRepository = adminRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String role = selectedRole();

        if (role == null || role.equals("ADMIN")) {
            Optional<Admin> admin = adminRepository.findByUsername(username);
            if (admin.isPresent()) {
                return User.withUsername(username).password(admin.get().getPassword()).roles("ADMIN").build();
            }
        }

        if (role == null || role.equals("TEACHER")) {
            Optional<Teacher> teacher = teacherRepository.findByUsername(username);
            if (teacher.isPresent()) {
                return User.withUsername(username).password(teacher.get().getPassword()).roles("TEACHER").build();
            }
        }

        if (role == null || role.equals("STUDENT")) {
            Optional<Student> student = studentRepository.findByUsername(username);
            if (student.isPresent()) {
                return User.withUsername(username).password(student.get().getPassword()).roles("STUDENT").build();
            }
        }

        throw new UsernameNotFoundException("User not found: " + username);
    }

    // the role chosen on the login page (null when logging in from Postman/curl, then all tables are checked)
    private String selectedRole() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            return null;
        }
        return attributes.getRequest().getParameter("role");
    }
}
