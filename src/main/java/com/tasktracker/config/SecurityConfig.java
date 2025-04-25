package com.tasktracker.config;

import com.tasktracker.security.JwtAuthenticationEntryPoint;
import com.tasktracker.security.JwtRequestFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collections;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Autowired
        private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

        @Autowired
        private JwtRequestFilter jwtRequestFilter;

        // API endpoints security configuration (JWT-based)
        @Bean
        @Order(1)
        public SecurityFilterChain apiSecurityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> {
                                })
                                .csrf(csrf -> csrf.disable())
                                .securityMatcher("/api/**")
                                .authorizeHttpRequests(authorize -> authorize
                                                // Allow both the login and register endpoints
                                                .requestMatchers("/api/auth/login", "/api/auth/register").permitAll()
                                                // Protect all other API endpoints
                                                .anyRequest().authenticated())
                                // Configure session management to allow both JWT and session auth
                                .sessionManagement(session -> session
                                                // Change from STATELESS to NEVER to allow existing sessions
                                                .sessionCreationPolicy(SessionCreationPolicy.NEVER))
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint(
                                                                (AuthenticationEntryPoint) jwtAuthenticationEntryPoint));

                // Add JWT filter for API requests
                http.addFilterBefore(jwtRequestFilter, UsernamePasswordAuthenticationFilter.class);

                // Make sure security context is properly configured
                http.securityContext(securityContext -> securityContext.requireExplicitSave(false));

                return http.build();
        }

        @Bean
        @Order(2)
        public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
                http
                                .cors(cors -> {
                                })
                                // Enable CSRF for web views - don't disable it here!
                                .csrf(csrf -> {
                                })
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers("/login", "/register", "/css/**", "/js/**",
                                                                "/debug/**")
                                                .permitAll()
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .loginProcessingUrl("/perform_login")
                                                .defaultSuccessUrl("/tasks", true)
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/perform_logout")
                                                .logoutSuccessUrl("/login?logout")
                                                .permitAll());

                return http.build();
        }

        @Bean
        public AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration)
                        throws Exception {
                return authenticationConfiguration.getAuthenticationManager();
        }

        @Bean
        public PasswordEncoder passwordEncoder() {
                return new BCryptPasswordEncoder();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();
                configuration.setAllowedOrigins(Collections.singletonList("*"));
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
                configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }
}