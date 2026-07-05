package com.example.internship.security;

import jakarta.servlet.http.Cookie;

public class CookieUtils {

    // Creates a highly secure cookie for sessions
    public static Cookie createSecureCookie(String name, String value, int maxAgeInSeconds) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);  // Prevents JavaScript access (stops XSS attacks)
        cookie.setSecure(true);    // Ensures it only works over HTTPS
        cookie.setPath("/");
        cookie.setMaxAge(maxAgeInSeconds);
        cookie.setAttribute("SameSite", "Strict"); // Prevents CSRF attacks
        return cookie;
    }
}
