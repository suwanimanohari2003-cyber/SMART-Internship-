# Smart Internship Placement Portal
## 🛠️ Code Changes & Bug Fixes Report

This document outlines all the modifications, bug fixes, and deployment preparations made to the Smart Internship Portal codebase.

---

### 1. Authentication Controller Bug Fix (`AuthController.java`)
**Issue:** When a new user registered, the details were being saved twice in the database, causing potential duplicate entries and errors.
**Fix:** Removed the duplicate `userRepository.save(newUser);` call.
```diff
         // 4. Save to database
         userRepository.save(newUser);
-        
-        // 4. Save to database
-        userRepository.save(newUser);
```

### 2. Admin Dashboard & Settings Fixes (`AdminController.java`)
**Issue 1:** The pass rate in the admin dashboard was hardcoded to `78.5%` instead of showing real data.
**Fix 1:** Injected `AnalyticsService` to fetch real data from the database.
```diff
- model.addAttribute("passRate", 78.5);
+ double passRate = analyticsService.calculateOverallPassRate();
+ model.addAttribute("passRate", passRate);
```

**Issue 2:** The Admin Profile settings page (Name & Email update) wasn't saving the changes to the database. It was a blank method.
**Fix 2:** Implemented the `updateSettings` method to fetch the admin by email and save the new details using `userRepository`.
```java
@PostMapping("/settings")
public String updateSettings(@RequestParam String name, @RequestParam String email, Principal principal) {
    User admin = userRepository.findByEmail(principal.getName()).orElseThrow();
    admin.setName(name);
    admin.setEmail(email);
    userRepository.save(admin);
    return "redirect:/admin/settings?success";
}
```

### 3. Application Configuration Security (`application.properties`)
**Issue:** Hardcoded database passwords and Gmail App passwords were in the source code, which is a major security risk for GitHub.
**Fix:** Replaced hardcoded values with Environment Variables. Also added HikariCP pool configurations for stable cloud connections.
```diff
- spring.datasource.url=jdbc:mysql://localhost:3306/intern_portal
- spring.datasource.password=mySecretPassword
+ spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:mysql://localhost:3307/intern_portal}
+ spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:}
```
*Note: Also moved this file to the correct Maven directory `src/main/resources/application.properties` to ensure it is packaged during deployment.*

### 4. Git Security & Ignore Rules (`.gitignore`)
**Issue:** Uploaded student CVs and local environment secrets could be accidentally uploaded to GitHub.
**Fix:** Added security exclusion rules.
```text
### Uploads (student CVs - do not commit) ###
uploads/
/tmp/

### Environment secrets ###
.env
*.env.local
```

### 5. Deployment Setup (`Dockerfile`)
**Feature:** Added a Dockerfile to containerize the Spring Boot application for Railway.app cloud deployment.
**Implementation:** Created a Multi-stage Docker build to keep the application lightweight and fast.
- **Stage 1:** Uses `eclipse-temurin:21-jdk-alpine` to run Maven and build the JAR.
- **Stage 2:** Uses `eclipse-temurin:21-jre-alpine` to run the application, exposing port `8080`.

---
*Report Generated on: July 2026*
