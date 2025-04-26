package com.tasktracker.controller;

import com.tasktracker.model.Task;
import com.tasktracker.model.TimeEntry;
import com.tasktracker.model.User;
import com.tasktracker.service.TaskService;
import com.tasktracker.service.TimeEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

// Import HttpServletRequest instead of HttpRequest
import jakarta.servlet.http.HttpServletRequest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return "redirect:/login";
        }

        String username = auth.getName();
        System.out.println("Loading tasks for user: " + username);

        try {
            // Get tasks from service
            List<Task> tasks = taskService.getTasksByUsername(username);
            System.out.println("Found " + tasks.size() + " tasks for user " + username);

            // Create completed map
            Map<Long, Boolean> completedMap = new HashMap<>();
            for (Task task : tasks) {
                boolean completed = task.getStatus() == Task.TaskStatus.DONE;
                completedMap.put(task.getId(), completed);
            }

            // Add to model
            model.addAttribute("tasks", tasks);
            model.addAttribute("completedMap", completedMap);

        } catch (Exception e) {
            System.err.println("Error loading tasks: " + e.getMessage());
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to load tasks: " + e.getMessage());
            model.addAttribute("tasks", new ArrayList<>());
            model.addAttribute("completedMap", new HashMap<>());
        }

        // Always add a new task for the form
        model.addAttribute("newTask", new Task());
        return "tasks";
    }

    @GetMapping("/timeEntries")
    public String timeEntries(Model model, @RequestParam(required = false) Long taskId) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();

        try {
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
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Failed to load time entries: " + e.getMessage());
            model.addAttribute("timeEntries", new ArrayList<>());
            model.addAttribute("tasks", new ArrayList<>());
        }

        return "timeEntries";
    }

    @GetMapping("/register")
    public String register(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }
}
