package com.example.internship.model;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true)
    private User user;

    @Column(name = "full_name", length = 100)
    private String fullName;

    @Column(unique = true, length = 100)
    private String email;

    @Column(length = 100)
    private String university;

    private Double gpa;

    @Column(columnDefinition = "TEXT")
    private String skills;

    @Column(name = "cv_path")
    private String cvPath;

    // FIXED: Using Object 'Boolean'
    @Column(name = "profile_complete")
    private Boolean profileComplete = false;

    // --- Getters and Setters ---
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getUniversity() { return university; }
    public void setUniversity(String university) { this.university = university; }

    public Double getGpa() { return gpa; }
    public void setGpa(Double gpa) { this.gpa = gpa; }

    public String getSkills() { return skills; }
    public void setSkills(String skills) { this.skills = skills; }

    public String getCvPath() { return cvPath; }
    public void setCvPath(String cvPath) { this.cvPath = cvPath; }

    // Null-safe getter
    public boolean isProfileComplete() { return profileComplete != null && profileComplete; }
    public void setProfileComplete(boolean profileComplete) { this.profileComplete = profileComplete; }
}
