package com.tasktracker.controller;

import com.tasktracker.dto.LoginRequest;
import com.tasktracker.dto.LoginResponse;
import com.tasktracker.dto.RegisterRequest;
import com.tasktracker.dto.UserDto;
import com.tasktracker.model.User;
import com.tasktracker.service.UserService;
import com.tasktracker.security.JwtTokenUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AuthControllerTest {

    @InjectMocks
    private AuthController authController;

    @Mock
    private UserService userService;

    @Mock
    private AuthenticationManager authManager;

    @Mock
    private JwtTokenUtil jwtTokenUtil;

    @Mock
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void testRegisterUser_Success() {
        // Create a full RegisterRequest with all required fields
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("username");
        registerRequest.setEmail("email@example.com");
        registerRequest.setPassword("password");

        UserDto mockUserDto = new UserDto();
        mockUserDto.setUsername("username");

        when(userService.registerUser(any(RegisterRequest.class))).thenReturn(mockUserDto);

        ResponseEntity<?> response = authController.registerUser(registerRequest);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("User registered successfully", response.getBody());
        verify(userService).registerUser(any(RegisterRequest.class));
    }

    @Test
    public void testRegisterUser_BadRequest() {
        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername("username");
        registerRequest.setEmail("email@example.com");
        registerRequest.setPassword("password");

        when(userService.registerUser(any(RegisterRequest.class)))
                .thenThrow(new IllegalArgumentException("User already exists"));

        ResponseEntity<?> response = authController.registerUser(registerRequest);

        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("User already exists", response.getBody());
    }

    @Test
    public void testLogin_Success() {
        LoginRequest loginRequest = new LoginRequest("username", "password");

        // Create a properly initialized User with roles set
        User user = new User();
        user.setUsername("username");
        user.setRoles(new HashSet<>(Collections.singletonList("USER")));

        Authentication authentication = mock(Authentication.class);
        when(authManager.authenticate(any())).thenReturn(authentication);
        when(authentication.getPrincipal()).thenReturn(user);
        when(jwtTokenUtil.generateToken(user.getUsername())).thenReturn("jwt-token");

        ResponseEntity<LoginResponse> response = authController.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("jwt-token", response.getBody().getJwt());
        assertEquals("username", response.getBody().getUsername());
        assertEquals("USER", response.getBody().getRole());
    }
}