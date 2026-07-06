package com.example.internship;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Member 10 — Integration Tests: ApplicationController
 * Tests apply, status update, and access control for applications.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ApplicationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    // ---- 1. Unauthenticated apply attempt redirects to login ----
    @Test
    void applyForJob_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/student/apply/1").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    // ---- 2. Authenticated student can apply for a job ----
    @Test
    @WithMockUser(username = "amandiewanthika@gmail.com", roles = {"STUDENT"})
    void applyForJob_authenticatedStudent_redirectsAfterApply() throws Exception {
        mockMvc.perform(post("/student/apply/1").with(csrf()))
                .andExpect(status().is3xxRedirection());
    }

    // ---- 3. Company cannot access apply endpoint ----
    @Test
    @WithMockUser(username = "hr@virtusa.com", roles = {"COMPANY"})
    void applyForJob_companyRole_isForbidden() throws Exception {
        mockMvc.perform(post("/student/apply/1").with(csrf()))
                .andExpect(status().is(403));
    }

    // ---- 4. Student applications list page loads for authenticated student ----
    @Test
    @WithMockUser(username = "amandiewanthika@gmail.com", roles = {"STUDENT"})
    void myApplications_authenticatedStudent_returnsOk() throws Exception {
        mockMvc.perform(get("/student/applications"))
                .andExpect(status().isOk());
    }

    // ---- 5. Company applicants page loads for authenticated company ----
    @Test
    @WithMockUser(username = "hr@virtusa.com", roles = {"COMPANY"})
    void applicantsPage_authenticatedCompany_returnsOk() throws Exception {
        mockMvc.perform(get("/company/applicants"))
                .andExpect(status().isOk());
    }

    // ---- 6. Status update without auth is redirected ----
    @Test
    void statusUpdate_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/applications/1/status")
                .param("status", "SHORTLISTED")
                .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }
}
