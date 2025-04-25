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

            System.out.println("Loading tasks for user: " + username);

            try {
                List<Task> tasks = taskService.getTasksByUsername(username);
                System.out.println("Found " + tasks.size() + " tasks for user " + username);

                // Handle null values and set defaults to prevent UI errors
                tasks.forEach(task -> {
                    if (task.getTitle() == null)
                        task.setTitle("");
                    if (task.getDescription() == null)
                        task.setDescription("");

                    // Set default status if null
                    if (task.getStatus() == null)
                        task.setStatus(Task.TaskStatus.TODO);

                    // Handle potential null priority
                    if (task.getPriority() == null)
                        task.setPriority(Task.TaskPriority.MEDIUM);
                });

                model.addAttribute("tasks", tasks);
            } catch (Exception taskEx) {
                System.err.println("Error loading tasks: " + taskEx.getMessage());
                taskEx.printStackTrace();
                model.addAttribute("tasks", new ArrayList<>());
                model.addAttribute("errorMessage", "Error loading tasks: " + taskEx.getMessage());
            }

            // Always add a new task object for the form
            Task newTask = new Task();
            newTask.setStatus(Task.TaskStatus.TODO); // Set default status
            model.addAttribute("newTask", newTask);

            return "tasks";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("errorMessage", "Critical error in task view: " + e.getMessage());
            model.addAttribute("tasks", new ArrayList<>());
            model.addAttribute("newTask", new Task());
            return "tasks"; // Stay on tasks page with error message
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
