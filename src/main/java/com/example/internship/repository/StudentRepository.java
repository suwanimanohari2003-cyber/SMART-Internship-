package com.example.internship.repository;

import com.example.internship.model.Student;
import com.example.internship.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    // Finds a student profile using their Login Account ID
    Optional<Student> findByUserId(Long userId);

    Optional<Student> findByEmail(String email);

    Optional<Student> findByUser(User user);
}
