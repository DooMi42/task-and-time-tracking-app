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
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
                return "redirect:/login";
            }

            String username = auth.getName();
            System.out.println("Loading tasks for user: " + username);

            // Add empty collections as defaults to prevent NPEs
            model.addAttribute("tasks", new ArrayList<>());
            model.addAttribute("completedMap", new HashMap<>());

            try {
                // Get tasks with exception handling
                List<Task> tasks = taskService.getTasksByUsername(username);
                System.out.println("Found " + tasks.size() + " tasks for user " + username);

                // Create a map of task IDs to "completed" status
                Map<Long, Boolean> completedMap = new HashMap<>();
                for (Task task : tasks) {
                    // Consider a task completed if its status is DONE
                    boolean isCompleted = task.getStatus() != null &&
                            Task.TaskStatus.DONE.equals(task.getStatus());
                    completedMap.put(task.getId(), isCompleted);
                }

                // Update model after successful retrieval
                model.addAttribute("tasks", tasks);
                model.addAttribute("completedMap", completedMap);

            } catch (Exception taskEx) {
                taskEx.printStackTrace();
                model.addAttribute("errorMessage", "Error loading tasks: " + taskEx.getMessage());
            }

            // Always add a new task object for the form
            Task newTask = new Task();
            newTask.setStatus(Task.TaskStatus.TODO);
            model.addAttribute("newTask", newTask);

            return "tasks";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Critical error in task view: " + e.getMessage());
            return "error"; // Return the error page as a fallback
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
