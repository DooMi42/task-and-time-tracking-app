package com.tasktracker.controller;

import com.tasktracker.model.Task;
import com.tasktracker.model.User;
import com.tasktracker.service.TaskService;
import com.tasktracker.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/tasks")
public class TaskFormController {

    @Autowired
    private TaskService taskService;

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public String createTaskFromForm(
            @ModelAttribute Task task,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {
            // Get the current user
            String username = authentication.getName();
            User user = userService.getUserByUsername(username);
            task.setUser(user);

            // Set default values
            task.setCreatedAt(LocalDateTime.now());
            if (task.getStatus() == null) {
                task.setStatus(Task.TaskStatus.TODO);
            }

            // Log what we're saving for debugging
            System.out.println("Saving task: " + task.getTitle() + " with status: " + task.getStatus());

            // Save the task
            Task savedTask = taskService.saveTask(task);

            return "redirect:/tasks";
        } catch (Exception e) {
            e.printStackTrace();
            redirectAttributes.addFlashAttribute("errorMessage", "Failed to create task: " + e.getMessage());
            return "redirect:/error";
        }
    }
}
