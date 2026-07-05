package com.example.internship.model;

import jakarta.persistence.*;

@Entity
@Table(name = "companies")
public class Company extends BaseEntity {

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true)
    private User user;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(length = 100)
    private String industry;

    @Column(length = 100)
    private String location;

    @Column(length = 200)
    private String website;

    @Column(columnDefinition = "TEXT")
    private String description;

    // FIXED: Using Object 'Boolean'
    @Column(name = "profile_complete")
    private Boolean profileComplete = false;

    // --- Getters and Setters ---
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getIndustry() { return industry; }
    public void setIndustry(String industry) { this.industry = industry; }

    public String getLocation() { return location; }
    public void setLocation(String location) { this.location = location; }

    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // Null-safe getter
    public boolean isProfileComplete() { return profileComplete != null && profileComplete; }
    public void setProfileComplete(boolean profileComplete) { this.profileComplete = profileComplete; }
}