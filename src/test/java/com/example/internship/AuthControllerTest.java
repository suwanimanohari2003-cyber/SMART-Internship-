package com.example.internship;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Member 10 — Integration Tests: AuthController
 * Tests login, logout, register, and protected route access.
 */
@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ---- 1. Login page is publicly accessible ----
    @Test
    void loginPage_isPublic() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk());
    }

    // ---- 2. Register page is publicly accessible ----
    @Test
    void registerPage_isPublic() throws Exception {
        mockMvc.perform(get("/register"))
                .andExpect(status().isOk());
    }

    // ---- 3. Accessing protected route without login redirects to /login ----
    @Test
    void protectedRoute_withoutAuth_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // ---- 4. Admin dashboard accessible with ADMIN role ----
    @Test
    @WithMockUser(username = "interngo26@gmail.com", roles = {"ADMIN"})
    void adminDashboard_withAdminRole_returnsOk() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk());
    }

    // ---- 5. Student cannot access admin dashboard (403) ----
    @Test
    @WithMockUser(username = "student@nsbm.ac.lk", roles = {"STUDENT"})
    void adminDashboard_withStudentRole_isForbidden() throws Exception {
        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().is(403));
    }

    // ---- 6. Logout clears session and redirects ----
    @Test
    @WithMockUser(username = "student@nsbm.ac.lk", roles = {"STUDENT"})
    void logout_authenticatedUser_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/logout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?logout"));
    }

    // ---- 7. POST to register with missing fields returns to register page ----
    @Test
    void register_withMissingFields_returnsRegisterPage() throws Exception {
        mockMvc.perform(post("/register")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .param("email", "")
                .param("password", "")
                .with(csrf()))
                .andExpect(status().is3xxRedirection());
    }
}
