package com.university.ums.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

// a student registered in a course
@Entity
public class Registration {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // when the student is deleted, their registrations are deleted too
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Student student;

    // when the course is deleted, its registrations are deleted too
    @ManyToOne
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Course course;

    private String section;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }

    public String getSection() { return section; }
    public void setSection(String section) { this.section = section; }
}
