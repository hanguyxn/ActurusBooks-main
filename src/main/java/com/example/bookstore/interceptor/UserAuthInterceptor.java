package com.example.bookstore.interceptor;

import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import com.example.bookstore.entity.User;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Component
public class UserAuthInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        HttpSession session = request.getSession();

        // Check if user is logged in as customer
        User user = (User) session.getAttribute("user");

        if (user == null) {
            // Redirect to customer login page
            response.sendRedirect("/login");
            return false;
        }

        return true;
    }
}
