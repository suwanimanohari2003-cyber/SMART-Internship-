package com.example.internship;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Member 10 — Integration Tests: StudentController
 * Tests student dashboard, profile, and CV endpoints.
 */
@SpringBootTest
@AutoConfigureMockMvc
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ---- 1. Unauthenticated access should redirect to login ----
    @Test
    void studentDashboard_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    void studentProfile_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/student/profile"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // ---- 2. Authenticated STUDENT can access dashboard ----
    @Test
    @WithMockUser(username = "student@nsbm.ac.lk", roles = {"STUDENT"})
    void studentDashboard_authenticatedStudent_returnsOk() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().isOk());
    }

    // ---- 3. COMPANY role should not access student dashboard ----
    @Test
    @WithMockUser(username = "hr@virtusa.com", roles = {"COMPANY"})
    void studentDashboard_companyRole_isForbidden() throws Exception {
        mockMvc.perform(get("/student/dashboard"))
                .andExpect(status().is(403));
    }

    // ---- 4. Student can access My CV page ----
    @Test
    @WithMockUser(username = "student@nsbm.ac.lk", roles = {"STUDENT"})
    void myCvPage_authenticatedStudent_returnsOk() throws Exception {
        mockMvc.perform(get("/student/my-cv"))
                .andExpect(status().isOk());
    }

    // ---- 5. Student applications page loads ----
    @Test
    @WithMockUser(username = "student@nsbm.ac.lk", roles = {"STUDENT"})
    void applicationsPage_authenticatedStudent_returnsOk() throws Exception {
        mockMvc.perform(get("/student/applications"))
                .andExpect(status().isOk());
    }
}
