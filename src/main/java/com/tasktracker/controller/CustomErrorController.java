package com.tasktracker.controller;

import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.http.HttpServletRequest;

import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class CustomErrorController implements ErrorController {

    @RequestMapping("/error")
    public String handleError(HttpServletRequest request, Model model) {
        Object status = request.getAttribute(RequestDispatcher.ERROR_STATUS_CODE);
        Object message = request.getAttribute(RequestDispatcher.ERROR_MESSAGE);
        Object exception = request.getAttribute(RequestDispatcher.ERROR_EXCEPTION);

        if (status != null) {
            model.addAttribute("status", status);
        }

        if (message != null) {
            model.addAttribute("message", message);
        } else {
            model.addAttribute("message", "An unexpected error occurred");
        }

        if (exception != null) {
            model.addAttribute("exception", exception);
            model.addAttribute("trace", ((Exception) exception).getStackTrace());
        }

        return "error";
    }
}
