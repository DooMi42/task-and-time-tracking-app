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
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
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
                                // Allow session authentication for API calls
                                .sessionManagement(session -> session
                                                // Use IF_REQUIRED instead of NEVER
                                                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED))
                                .exceptionHandling(exceptions -> exceptions
                                                .authenticationEntryPoint(jwtAuthenticationEntryPoint));

                // Add JWT filter but make it skip requests with valid session auth
                http.addFilterBefore(new OncePerRequestFilter() {
                        @Override
                        protected void doFilterInternal(HttpServletRequest request,
                                        HttpServletResponse response,
                                        FilterChain filterChain) throws ServletException, IOException {
                                // Check if user is already authenticated via session
                                Authentication auth = SecurityContextHolder.getContext().getAuthentication();
                                if (auth != null && auth.isAuthenticated()
                                                && !(auth instanceof AnonymousAuthenticationToken)) {
                                        // User is already authenticated via session, skip JWT processing
                                        filterChain.doFilter(request, response);
                                        return;
                                }
                                // Otherwise use JWT filter
                                jwtRequestFilter.doFilter(request, response, filterChain);
                        }
                }, UsernamePasswordAuthenticationFilter.class);

                // Share security context
                http.securityContext(securityContext -> securityContext.requireExplicitSave(false));

                return http.build();
        }

        // Web application security configuration (Form-based)
        @Bean
        @Order(2)
        public SecurityFilterChain webSecurityFilterChain(HttpSecurity http) throws Exception {
                http
                                .securityMatcher("/**") // Apply to all non-API endpoints
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers("/", "/index.html", "/css/**", "/js/**", "/images/**",
                                                                "/error")
                                                .permitAll()
                                                .requestMatchers("/login", "/register").permitAll()
                                                .anyRequest().authenticated())
                                .formLogin(form -> form
                                                .loginPage("/login")
                                                .defaultSuccessUrl("/tasks", true)
                                                .permitAll())
                                .logout(logout -> logout
                                                .logoutUrl("/perform_logout")
                                                .logoutSuccessUrl("/login?logout")
                                                .permitAll())
                                // Enable CSRF for web forms but with proper configuration
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers("/api/**"));

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