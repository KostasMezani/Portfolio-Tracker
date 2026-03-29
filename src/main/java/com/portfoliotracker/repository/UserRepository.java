package com.portfoliotracker.repository;

import com.portfoliotracker.model.User;
import com.portfoliotracker.storage.file.UserFileStorage;

import java.util.List;

public class UserRepository {

    private UserFileStorage userFileStorage;

    public UserRepository(UserFileStorage userFileStorage) {
        this.userFileStorage = userFileStorage;
    }

    public User save(User user){
        List<User> users = userFileStorage.loadAll();

        // Remove if already exists (for update)
        users.removeIf(u -> u.getId().equals(user.getId()));

        // Add the new/updated user
        users.add(user);

        // Save them all
        userFileStorage.saveAll(users);
        return user;
    }

    public User findById(String id){
       List<User> users = userFileStorage.loadAll();
       for (User u : users) {
           if (u.getId().equals(id)) {
               return u;
           }
       }
       return null; // if not found
    }

    public User findByUsername(String username){
        List<User> users = userFileStorage.loadAll();
        for (User u : users) {
            if (u.getUsername().equals(username)) {
                return u;
            }
        }
        return null;
    }

    public List<User> findAll(){
        return userFileStorage.loadAll();
    }

    public void delete(String id){
        List<User> users = userFileStorage.loadAll();
        users.removeIf(u -> u.getId().equals(id));
        userFileStorage.saveAll(users);
    }
}
