package com.portfoliotracker.storage.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.portfoliotracker.model.User;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class UserFileStorage {

    private static final String FILE_PATH = "data/users.json";
    private final Gson gson;

    public UserFileStorage() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    /**
     * Saves all users to the JSON file
     * @param users the list of users to save
     */
    public void saveAll(List<User> users) {
        try {
            // Create the directory if it doesn't exist'
            Files.createDirectories(Path.of("data"));
            // Convert the list to JSON and write to the file
            String json = gson.toJson(users);
            Files.writeString(Path.of(FILE_PATH), json);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save users", e);
        }
    }

    /**
     * Loads all users from the JSON file
     * @return list of all users
     */
    public List<User> loadAll() {
        try {
            Path path = Path.of(FILE_PATH);
            // If the file does not exist, return an empty list.
            if (!Files.exists(path)) {
                return new ArrayList<>();
            }
            String json = Files.readString(path);
            Type listType = new TypeToken<List<User>>(){}.getType();
            return gson.fromJson(json, listType);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load users", e);
        }
    }
}