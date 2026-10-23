package com.university.ums;

import com.university.ums.model.Admin;
import com.university.ums.model.Course;
import com.university.ums.model.Registration;
import com.university.ums.model.Student;
import com.university.ums.model.Teacher;
import com.university.ums.repository.AdminRepository;
import com.university.ums.repository.CourseRepository;
import com.university.ums.repository.RegistrationRepository;
import com.university.ums.repository.StudentRepository;
import com.university.ums.repository.TeacherRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

// runs once when the app starts and adds sample data (only into empty tables)
@Component
public class DataLoader implements CommandLineRunner {

    private final AdminRepository adminRepository;
    private final TeacherRepository teacherRepository;
    private final StudentRepository studentRepository;
    private final CourseRepository courseRepository;
    private final RegistrationRepository registrationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataLoader(AdminRepository adminRepository, TeacherRepository teacherRepository,
                      StudentRepository studentRepository, CourseRepository courseRepository,
                      RegistrationRepository registrationRepository, PasswordEncoder passwordEncoder) {
        this.adminRepository = adminRepository;
        this.teacherRepository = teacherRepository;
        this.studentRepository = studentRepository;
        this.courseRepository = courseRepository;
        this.registrationRepository = registrationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        if (adminRepository.count() == 0) {
            Admin admin = new Admin();
            admin.setName("Main Admin");
            admin.setEmail("admin@university.edu");
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123"));
            adminRepository.save(admin);
        }

        if (teacherRepository.count() == 0) {
            Teacher teacher = new Teacher();
            teacher.setName("Rahim Uddin");
            teacher.setEmail("rahim@university.edu");
            teacher.setDepartment("CSE");
            teacher.setDesignation("Lecturer");
            teacher.setUsername("teacher1");
            teacher.setPassword(passwordEncoder.encode("teacher123"));
            teacherRepository.save(teacher);
        }

        if (studentRepository.count() == 0) {
            Student student = new Student();
            student.setName("Karim Hasan");
            student.setEmail("karim@university.edu");
            student.setRoll("2003001");
            student.setDepartment("CSE");
            student.setUsername("student1");
            student.setPassword(passwordEncoder.encode("student123"));
            studentRepository.save(student);
        }

        // two courses of teacher1
        Optional<Teacher> teacher1 = teacherRepository.findByUsername("teacher1");
        if (courseRepository.count() == 0 && teacher1.isPresent()) {
            courseRepository.save(newCourse("CSE3230", "Software Development Lab", 1.5, "3-2", teacher1.get()));
            courseRepository.save(newCourse("CSE3201", "Operating Systems", 3.0, "3-2", teacher1.get()));
        }

        // student1 registered in CSE3230
        Optional<Student> student1 = studentRepository.findByUsername("student1");
        Optional<Course> lab = courseRepository.findByCode("CSE3230");
        if (registrationRepository.count() == 0 && student1.isPresent() && lab.isPresent()) {
            Registration registration = new Registration();
            registration.setStudent(student1.get());
            registration.setCourse(lab.get());
            registration.setSection("A");
            registrationRepository.save(registration);
        }
    }

    private Course newCourse(String code, String title, Double credit, String semester, Teacher teacher) {
        Course course = new Course();
        course.setCode(code);
        course.setTitle(title);
        course.setCredit(credit);
        course.setSemester(semester);
        course.setTeacher(teacher);
        return course;
    }
}
