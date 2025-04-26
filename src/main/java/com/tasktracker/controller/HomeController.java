package com.tasktracker.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller // Change from @RestController to @Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        // Check if user is authenticated
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.isAuthenticated() &&
                !auth.getName().equals("anonymousUser")) {
            // Redirect authenticated users to tasks page
            return "redirect:/tasks";
        }
        // Redirect unauthenticated users to login page
        return "redirect:/login";
    }
}
