package com.example.customerarchive.controller;

import com.example.customerarchive.dto.AuthenticationRequestDto;
import com.example.customerarchive.model.User;
import com.example.customerarchive.repository.UserRepository;
import com.example.customerarchive.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthenticationController {
    private static final Logger logger = LoggerFactory.getLogger(AuthenticationController.class);

    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationController(AuthenticationManager authenticationManager, JwtUtil jwtUtil, UserDetailsService userDetailsService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody User newUser) {
        logger.info("Registering new user: {}", newUser.getUsername());

        Optional<User> existingUserByUsername = userRepository.findByUsername(newUser.getUsername());
        if (existingUserByUsername.isPresent()) {
            logger.warn("Username {} is already taken", newUser.getUsername());
            return ResponseEntity.badRequest().body("Error: Username is already taken!");
        }
        else {
            User existingUserByEmail = userRepository.findByEmail(newUser.getEmail());
            if (existingUserByEmail != null) {
                logger.warn("Email {} is already in use", newUser.getEmail());
                return ResponseEntity.badRequest().body("Error: Email is already in use!");
            }

            newUser.setPassword(passwordEncoder.encode(newUser.getPassword()));
            userRepository.save(newUser);
            logger.info("User registered successfully: {}", newUser.getUsername());


            final UserDetails userDetails = userDetailsService.loadUserByUsername(newUser.getUsername());
            final String jwt = jwtUtil.generateToken(userDetails.getUsername());
            logger.info("JWT generated for user: {}", newUser.getUsername());

            return ResponseEntity.ok(jwt);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody AuthenticationRequestDto authenticationRequest) {
        logger.info("Authenticating user: {}", authenticationRequest.getUsername());

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        } catch (BadCredentialsException e) {
            logger.warn("Incorrect username or password for user: {}", authenticationRequest.getUsername());
            return ResponseEntity.badRequest().body("Incorrect username or password");
        }

        final UserDetails userDetails = userDetailsService.loadUserByUsername(authenticationRequest.getUsername());
        final String jwt = jwtUtil.generateToken(userDetails.getUsername());
        logger.info("User authenticated successfully: {}", authenticationRequest.getUsername());

        return ResponseEntity.ok(jwt);
    }

}