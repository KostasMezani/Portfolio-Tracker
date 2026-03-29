package com.portfoliotracker.service;

import com.portfoliotracker.model.User;
import java.util.List;

import com.portfoliotracker.repository.UserRepository;

public class UserService {

    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Finds a user by their username
     * @param username the username to search for
     * @return the User if found, null otherwise
     */
    public User findByUsername(String username){
        return userRepository.findByUsername(username);
    }

    /**
     * Finds a user by their id
     * @param id the id to search for
     * @return the User if found, null otherwise
     */
    public User findById(String id){
        return userRepository.findById(id);
    }

    /**
     * Returns all users
     * @return a list of all users
     */
    public List<User> getAllUsers(){
        return userRepository.findAll();
    }
}
