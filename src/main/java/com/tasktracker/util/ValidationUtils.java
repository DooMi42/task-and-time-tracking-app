package com.tasktracker.util;

import org.springframework.util.StringUtils;

public class ValidationUtils {

    public static boolean isValidEmail(String email) {
        return StringUtils.hasText(email) && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    public static boolean isValidUsername(String username) {
        return StringUtils.hasText(username) && username.length() >= 3 && username.length() <= 20;
    }

    public static boolean isValidPassword(String password) {
        return StringUtils.hasText(password) && password.length() >= 8;
    }

    public static boolean isValidTaskName(String taskName) {
        return StringUtils.hasText(taskName) && taskName.length() <= 100;
    }

    public static boolean isValidTimeEntry(double hours) {
        return hours >= 0;
    }
}