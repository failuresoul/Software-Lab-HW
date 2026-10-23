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
        Optional<Admin> admin = adminRepository.findByUsername(username);
        if (admin.isPresent()) {
            return User.withUsername(username).password(admin.get().getPassword()).roles("ADMIN").build();
        }

        Optional<Teacher> teacher = teacherRepository.findByUsername(username);
        if (teacher.isPresent()) {
            return User.withUsername(username).password(teacher.get().getPassword()).roles("TEACHER").build();
        }

        Optional<Student> student = studentRepository.findByUsername(username);
        if (student.isPresent()) {
            return User.withUsername(username).password(student.get().getPassword()).roles("STUDENT").build();
        }

        throw new UsernameNotFoundException("User not found: " + username);
    }
}
