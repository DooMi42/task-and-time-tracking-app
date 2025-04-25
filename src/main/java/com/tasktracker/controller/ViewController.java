package com.tasktracker.controller;

import com.tasktracker.model.Task;
import com.tasktracker.model.TimeEntry;
import com.tasktracker.model.User;
import com.tasktracker.service.TaskService;
import com.tasktracker.service.TimeEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

@Controller
public class ViewController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private TimeEntryService timeEntryService;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/tasks")
    public String tasks(Model model) {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String username = auth.getName();

            // Log the username for debugging
            System.out.println("Loading tasks for user: " + username);

            // Add detailed error logging
            try {
                List<Task> tasks = taskService.getTasksByUsername(username);
                System.out.println("Found " + tasks.size() + " tasks for user " + username);
                model.addAttribute("tasks", tasks);
            } catch (Exception taskEx) {
                System.err.println("Error loading tasks: " + taskEx.getMessage());
                taskEx.printStackTrace();
                // Add empty list to prevent UI errors
                model.addAttribute("tasks", new ArrayList<>());
            }

            // Add a new task object for the form
            model.addAttribute("newTask", new Task());

            return "tasks";
        } catch (Exception e) {
            // Log the exception
            System.err.println("Critical error in tasks view: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Error loading tasks: " + e.getMessage());
            // Still add empty lists so the page doesn't crash
            model.addAttribute("tasks", new ArrayList<>());
            model.addAttribute("newTask", new Task());
            return "tasks"; // Return tasks view instead of error view
        }
    }

    @GetMapping("/timeEntries")
    public String timeEntries(Model model, @RequestParam(required = false) Long taskId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        List<TimeEntry> entries;
        if (taskId != null) {
            entries = timeEntryService.getTimeEntriesByTaskId(taskId);
            model.addAttribute("taskId", taskId);
            model.addAttribute("taskTitle", taskService.getTaskById(taskId).getTitle());
        } else {
            entries = timeEntryService.getTimeEntriesByUsername(username);
        }

        model.addAttribute("timeEntries", entries);
        model.addAttribute("newTimeEntry", new TimeEntry());
        model.addAttribute("tasks", taskService.getTasksByUsername(username));

        return "timeEntries";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }
}
