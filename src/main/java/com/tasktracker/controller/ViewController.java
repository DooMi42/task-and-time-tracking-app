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
            String username = auth.getName();

            System.out.println("Loading tasks for user: " + username);

            try {
                List<Task> tasks = taskService.getTasksByUsername(username);
                System.out.println("Found " + tasks.size() + " tasks for user " + username);

                // Handle null values and set defaults
                tasks.forEach(task -> {
                    if (task.getTitle() == null)
                        task.setTitle("");
                    if (task.getDescription() == null)
                        task.setDescription("");
                    if (task.getStatus() == null)
                        task.setStatus(Task.TaskStatus.TODO);
                    if (task.getPriority() == null)
                        task.setPriority(Task.TaskPriority.MEDIUM);
                });

                // Create a map of task IDs to "completed" status for the template
                Map<Long, Boolean> completedMap = new HashMap<>();
                for (Task task : tasks) {
                    // Consider a task completed if its status is DONE
                    boolean isCompleted = Task.TaskStatus.DONE.equals(task.getStatus());
                    completedMap.put(task.getId(), isCompleted);
                }

                model.addAttribute("tasks", tasks);
                model.addAttribute("completedMap", completedMap); // Add this map for the template

            } catch (Exception taskEx) {
                System.err.println("Error loading tasks: " + taskEx.getMessage());
                taskEx.printStackTrace();
                model.addAttribute("tasks", new ArrayList<>());
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
            model.addAttribute("tasks", new ArrayList<>());
            model.addAttribute("newTask", new Task());
            return "tasks";
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
