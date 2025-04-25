package com.tasktracker.service;

import com.tasktracker.dto.RegisterRequest;
import com.tasktracker.dto.UserDto;
import com.tasktracker.model.User;

import java.util.List;

public interface UserService {
    UserDto registerUser(RegisterRequest registerRequest);

    UserDto getUserById(Long id);

    UserDto getUserByUsername(String username);

    List<UserDto> getAllUsers();

    UserDto updateUser(Long id, UserDto userDto);

    void deleteUser(Long id);

    User getCurrentUser();

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    void save(User user);
}