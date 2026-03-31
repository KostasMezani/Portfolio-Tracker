package com.portfoliotracker.storage.file;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public abstract class JsonFileStorage<T> {

    protected final Gson gson;
    protected final String filePath;
    protected final Type type;

    public JsonFileStorage(String filePath, Type type) {
        this.filePath = filePath;
        this.type = type;
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .create();
    }

    /**
     * Saves all items to the JSON file
     * @param items the list of items to save
     */
    public void saveAll(List<T> items) {
        try {
            Files.createDirectories(Path.of("data"));
            String json = gson.toJson(items);
            Files.writeString(Path.of(filePath), json);
        } catch (IOException e) {
            throw new RuntimeException("Failed to save to " + filePath, e);
        }
    }

    /**
     * Loads all items from the JSON file
     * @return list of all items
     */
    public List<T> loadAll() {
        try {
            Path path = Path.of(filePath);
            if (!Files.exists(path)) {
                return new ArrayList<>();
            }
            String json = Files.readString(path);
            return gson.fromJson(json, type);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load from " + filePath, e);
        }
    }
}