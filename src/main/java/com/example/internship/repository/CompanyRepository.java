package com.example.internship.repository;

import com.example.internship.model.Company;
import com.example.internship.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CompanyRepository extends JpaRepository<Company, Long> {

    Optional<Company> findByUser(User user);

    Optional<Company> findByUserId(Long userId);

}
