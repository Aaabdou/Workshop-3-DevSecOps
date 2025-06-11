package org.example.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.mindrot.jbcrypt.BCrypt;

import java.io.*;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

public class AuthBcrypt {
    private static final String USER_FILE = "src/main/java/org/example/data/users.json";
    private static Map<String, String> users = new HashMap<>();
    private static final Gson gson = new Gson();

    static {
        loadUsers();
    }

    private static void loadUsers() {
        try (Reader reader = new FileReader(USER_FILE)) {
            Type type = new TypeToken<Map<String, String>>() {}.getType();
            users = gson.fromJson(reader, type);
            if (users == null) users = new HashMap<>();
        } catch (IOException e) {
            users = new HashMap<>();
        }
    }

    private static void saveUsers() {
        try {
            File file = new File(USER_FILE);
            try (Writer writer = new FileWriter(file)) {
                gson.toJson(users, writer);
            }
        } catch (IOException e) {
            System.err.println("Failed to save user data: " + e.getMessage());
        }
    }


    public static boolean login(String username, String password) {
        if (!users.containsKey(username)) return false;
        String storedHash = users.get(username);
        return BCrypt.checkpw(password, storedHash);
    }

    public static boolean register(String username, String password) {
        if (users.containsKey(username)) return false;
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt(12));
        users.put(username, hashed);
        saveUsers();
        return true;
    }
}
