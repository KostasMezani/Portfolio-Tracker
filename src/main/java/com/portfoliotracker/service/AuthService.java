package com.portfoliotracker.service;

import com.portfoliotracker.exception.AuthenticationException;
import com.portfoliotracker.exception.ValidationException;
import com.portfoliotracker.model.User;
import com.portfoliotracker.repository.UserRepository;
import com.portfoliotracker.util.PasswordUtils;
import com.portfoliotracker.util.ValidationUtils;

import java.time.LocalDateTime;
import java.util.UUID;

public class AuthService {

    private UserRepository userRepository;

    public AuthService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Registers a new user
     * @param username the username
     * @param password the plain text password
     * @param email the email address
     * @return the newly created User
     */
    public User register(String username, String password, String email) {
        // Step 1: Validate fields
        if (!ValidationUtils.isNotEmpty(username)) {
            throw new ValidationException("Username cannot be empty");
        }
        if (!ValidationUtils.isValidPassword(password)) {
            throw new ValidationException("Password must be at least 6 characters");
        }
        if (!ValidationUtils.isValidEmail(email)) {
            throw new ValidationException("Invalid email address");
        }

        // Step 2: Check if username already exists
        if (userRepository.findByUsername(username) != null) {
            throw new ValidationException("Username already exists");
        }

        // Step 3: Hash password and create user
        String hashedPassword = PasswordUtils.hashPassword(password);
        User user = new User(
                UUID.randomUUID().toString(),
                username,
                email,
                hashedPassword,
                LocalDateTime.now()
        );

        // Step 4: Save and return User
        userRepository.save(user);
        return user;
    }

    /**
     * Authenticates a user with the given credentials
     * @param username the username of the user
     * @param password the plain text password
     * @return the authenticated User object
     */
    public User login(String username, String password) {

        // Step 1: Find the user
        User user = userRepository.findByUsername(username);

        //Step 2: Checks the credentials
        if (user == null) {
            throw new AuthenticationException("Invalid username or password");
        }

        //Step 3: Check password
        if (!PasswordUtils.verifyPassword(password, user.getPasswordHash())) {
            throw new AuthenticationException("Invalid username or password");
        }

        // Step 4:
        return user;
    }
}
