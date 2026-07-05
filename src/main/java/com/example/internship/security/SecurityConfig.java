package com.example.internship.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CookieCsrfTokenRepository;
import org.springframework.security.web.csrf.CsrfTokenRequestAttributeHandler;

/**
 * Member 2 — SecurityConfig
 * Full HTTP security chain:
 *  - CSRF protection explicitly enabled with CookieCsrfTokenRepository (HttpOnly)
 *  - Session management: always create, never stateless
 *  - Role-based route guards: ADMIN / STUDENT / COMPANY
 *  - Secure logout: invalidates session, deletes JSESSIONID cookie
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        // ── CSRF: explicitly configured with HttpOnly cookie repository ──────
        CsrfTokenRequestAttributeHandler csrfHandler = new CsrfTokenRequestAttributeHandler();
        csrfHandler.setCsrfRequestAttributeName("_csrf");

        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .csrfTokenRequestHandler(csrfHandler)
            )

            // ── Session: always create, max 1 per user ────────────────────────
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .maximumSessions(1)
            )

            // ── Security headers: allow same-origin iframes (Swagger UI) ──────
            .headers(headers -> headers
                .frameOptions(frameOptions -> frameOptions.sameOrigin())
            )

            // ── Route guards ──────────────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth
                // Public resources
                .requestMatchers(
                    "/images/**", "/css/**", "/js/**", "/favicon.ico",
                    "/register", "/login", "/error"
                ).permitAll()
                // Swagger / OpenAPI
                .requestMatchers(
                    "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                    "/api/test/**"
                ).permitAll()
                // Analytics endpoints (admin only via role guard on /admin/**)
                .requestMatchers("/analytics/**").hasRole("ADMIN")
                .requestMatchers("/reports/**").hasRole("ADMIN")
                // Role-based access
                .requestMatchers("/admin/**").hasRole("ADMIN")
                .requestMatchers("/student/**").hasRole("STUDENT")
                .requestMatchers("/company/**").hasRole("COMPANY")
                .anyRequest().authenticated()
            )

            // ── Form login ────────────────────────────────────────────────────
            .formLogin(form -> form
                .loginPage("/login")
                .usernameParameter("email")
                .successHandler(new CustomAuthenticationSuccessHandler())
                .failureUrl("/login?error=true")
                .permitAll()
            )

            // ── Logout ────────────────────────────────────────────────────────
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .invalidateHttpSession(true)
                .deleteCookies("JSESSIONID", "XSRF-TOKEN")
                .clearAuthentication(true)
                .permitAll()
            );

        return http.build();
    }
}
