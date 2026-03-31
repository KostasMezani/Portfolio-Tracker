package com.portfoliotracker.storage.file;

import com.google.gson.reflect.TypeToken;
import com.portfoliotracker.model.User;

import java.util.List;

public class UserFileStorage extends JsonFileStorage<User> {
    public UserFileStorage() {
        super("data/users.json",
                new TypeToken<List<User>>(){}.getType());
    }
}