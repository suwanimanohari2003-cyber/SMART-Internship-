package com.example.internship.controller;

import com.example.internship.model.Role;
import com.example.internship.model.User;
import com.example.internship.repository.CompanyRepository;
import com.example.internship.repository.RoleRepository;
import com.example.internship.repository.StudentRepository;
import com.example.internship.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.internship.model.Company;
import com.example.internship.model.Student;

@Controller
public class AuthController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final CompanyRepository companyRepository;
    private final StudentRepository studentRepository;

    public AuthController(UserRepository userRepository,
                          RoleRepository roleRepository,
                          PasswordEncoder passwordEncoder,
                          CompanyRepository companyRepository,
                          StudentRepository studentRepository) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.companyRepository = companyRepository;
        this.studentRepository = studentRepository;
    }

    @GetMapping("/login")
    public String showLoginForm(@RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                Model model) {
        if (error != null) { model.addAttribute("errorMsg", "Invalid email or password."); }
        if (logout != null) { model.addAttribute("msg", "You have been logged out successfully."); }
        return "auth/login";
    }

    // Redirects the empty home page to the profile page
    @GetMapping("/")
    public String home() {
        return "redirect:/profile";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        return "auth/register";
    }

    // THIS IS NEW: Handles the registration form submission
    @PostMapping("/register")
    public String registerUser(@RequestParam String username,
                               @RequestParam String email,
                               @RequestParam String password,
                               @RequestParam String role,
                               Model model) {

        // 1. Check if email or username already exists
        if (userRepository.existsByEmail(email) || userRepository.existsByUsername(username)) {
            model.addAttribute("errorMsg", "Email or Username is already taken!");
            return "auth/register";
        }

        // 2. Create the new user
        User newUser = new User();
        newUser.setUsername(username);
        newUser.setEmail(email);
        newUser.setPasswordHash(passwordEncoder.encode(password)); // Encrypt the password!

        // 3. Assign the correct role (STUDENT or COMPANY)
        Role userRole = roleRepository.findByName(role).orElseThrow();
        newUser.getRoles().add(userRole);

        // 4. Save to database
        userRepository.save(newUser);

        if ("COMPANY".equalsIgnoreCase(role)) {
            Company newCompany = new Company();
            newCompany.setUser(newUser);
            newCompany.setName(username);
            companyRepository.save(newCompany);
        }
        else if ("STUDENT".equalsIgnoreCase(role)) {
            Student newStudent = new Student();
            newStudent.setUser(newUser);
            newStudent.setFullName(username);
            studentRepository.save(newStudent);
        }

        // Redirect to login page with success message
        return "redirect:/login?msg=Registration Successful! Please login.";
    }

    @GetMapping("/profile")
    public String showProfile() {
        return "auth/profile";
    }
}
