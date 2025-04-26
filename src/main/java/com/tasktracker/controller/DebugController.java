package com.tasktracker.controller;

import com.tasktracker.model.Task;
import com.tasktracker.model.TimeEntry;
import com.tasktracker.model.User;
import com.tasktracker.repository.TaskRepository;
import com.tasktracker.repository.TimeEntryRepository;
import com.tasktracker.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/debug")
public class DebugController {

    @Autowired
    private TaskRepository taskRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TimeEntryRepository timeEntryRepository;

    @GetMapping("/tasks")
    public String debugTasks(Model model) {
        try {
            // Get all tasks directly from repository
            List<Task> allTasks = taskRepository.findAll();
            System.out.println("Total tasks in database: " + allTasks.size());

            for (Task task : allTasks) {
                System.out.println("Task ID: " + task.getId() +
                        ", Title: " + task.getTitle() +
                        ", User: " + (task.getUser() != null ? task.getUser().getUsername() : "null"));
            }

            model.addAttribute("allTasks", allTasks);
            return "debug";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "debug";
        }
    }

    @GetMapping("/users")
    public String debugUsers(Model model) {
        try {
            List<User> allUsers = userRepository.findAll();
            System.out.println("Total users in database: " + allUsers.size());

            for (User user : allUsers) {
                System.out.println("User ID: " + user.getId() +
                        ", Username: " + user.getUsername());
            }

            model.addAttribute("allUsers", allUsers);
            return "debug";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "debug";
        }
    }

    @GetMapping("/time-entries")
    public String debugTimeEntries(Model model) {
        try {
            List<TimeEntry> allTimeEntries = timeEntryRepository.findAll();
            System.out.println("Total time entries in database: " + allTimeEntries.size());

            for (TimeEntry entry : allTimeEntries) {
                System.out.println("Time Entry ID: " + entry.getId() +
                        ", Task: " + (entry.getTask() != null ? entry.getTask().getTitle() : "null") +
                        ", User: " + (entry.getUser() != null ? entry.getUser().getUsername() : "null") +
                        ", Start Time: " + entry.getStartTime() +
                        ", End Time: " + entry.getEndTime());
            }

            model.addAttribute("allTimeEntries", allTimeEntries);
            return "debug";
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("error", e.getMessage());
            return "debug";
        }
    }
}
