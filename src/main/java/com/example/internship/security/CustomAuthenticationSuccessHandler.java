package com.example.internship.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Collection;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        String redirectUrl = null;

        Collection<? extends GrantedAuthority> authorities = authentication.getAuthorities();

        for (GrantedAuthority grantedAuthority : authorities) {
            String role = grantedAuthority.getAuthority();

            if (role.equals("ROLE_ADMIN") || role.equals("ADMIN")) {
                redirectUrl = "/admin/dashboard";
                break;
            } else if (role.equals("ROLE_COMPANY") || role.equals("COMPANY")) {
                redirectUrl = "/company/dashboard";
                break;
            } else if (role.equals("ROLE_STUDENT") || role.equals("STUDENT")) {
                redirectUrl = "/student/dashboard";
                break;
            }
        }

        if (redirectUrl == null) {
            throw new IllegalStateException("User role not recognized for redirection");
        }

        response.sendRedirect(redirectUrl);
    }
}
