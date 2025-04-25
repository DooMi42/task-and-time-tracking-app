package com.tasktracker.controller;

import com.tasktracker.model.Task;
import com.tasktracker.model.TimeEntry;
import com.tasktracker.model.User;
import com.tasktracker.service.TaskService;
import com.tasktracker.service.TimeEntryService;
import com.tasktracker.dto.TaskDto;
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

            // Always ensure we have these attributes to prevent errors
            model.addAttribute("tasks", new ArrayList<>());
            model.addAttribute("completedMap", new HashMap<>());

            try {
                // Create a DTO-based approach to avoid lazy loading issues
                List<TaskDto> taskDtos = taskService.getTaskDtosByUsername(username);
                System.out.println("Found " + taskDtos.size() + " tasks for user " + username);

                // Convert to regular Task objects that are detached from Hibernate
                List<Task> tasks = new ArrayList<>();
                Map<Long, Boolean> completedMap = new HashMap<>();

                for (TaskDto dto : taskDtos) {
                    Task task = new Task();
                    task.setId(dto.getId());
                    task.setTitle(dto.getTitle());
                    task.setDescription(dto.getDescription());
                    task.setStatus(dto.getStatus());
                    task.setPriority(dto.getPriority());
                    task.setDueDate(dto.getDueDate());

                    // No need to set user or timeEntries - they won't be accessed in the template

                    tasks.add(task);
                    completedMap.put(task.getId(), Task.TaskStatus.DONE.equals(task.getStatus()));
                }

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
            model.addAttribute("errorMessage", "Critical error: " + e.getMessage());
            // Return tasks view anyway with empty lists
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
