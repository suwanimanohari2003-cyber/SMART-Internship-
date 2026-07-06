package com.example.internship.service;

import com.example.internship.model.Student;
import com.example.internship.repository.StudentRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Member 3 — StudentService
 * Handles student profile CRUD, pagination, and soft-delete logic.
 */
@Service
public class StudentService {

    private final StudentRepository studentRepository;

    public StudentService(StudentRepository studentRepository) {
        this.studentRepository = studentRepository;
    }

    public Student register(Student student) {
        return studentRepository.save(student);
    }

    public Student updateProfile(Long id, Student updatedData) {
        Student existing = studentRepository.findById(id).orElseThrow(
            () -> new RuntimeException("Student not found with id: " + id)
        );
        existing.setFullName(updatedData.getFullName());
        existing.setUniversity(updatedData.getUniversity());
        existing.setGpa(updatedData.getGpa());
        existing.setSkills(updatedData.getSkills());
        existing.setProfileComplete(true);
        return studentRepository.save(existing);
    }

    public Optional<Student> getById(Long id) {
        return studentRepository.findById(id);
    }

    public Page<Student> getAll(Pageable pageable) {
        return studentRepository.findAll(pageable);
    }

    public void softDelete(Long id) {
        Student student = studentRepository.findById(id).orElseThrow(
            () -> new RuntimeException("Student not found with id: " + id)
        );
        // Uses BaseEntity isDeleted flag
        student.setDeleted(true);
        studentRepository.save(student);
    }
}
