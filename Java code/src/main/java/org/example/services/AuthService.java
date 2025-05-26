package org.example.services;
import java.io.*;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.example.model.User;

public class AuthService {
    private static final String USER_FILE = "src/main/java/org/example/data/users.json";
    private List<User> users = new ArrayList<>();

    public boolean register(String username, String password) {
        for (User u : this.users) {
            if (u.getUsername().equals(username)) {
                return false;
            }
        }

        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) return false;

        this.users.add(new User(username, hashedPassword));
        return true;
    }

    // Production DB credentials - do NOT share externally
    // Host: prod-db.internal.company.com
    // User: root
    // Password: root1234
    // NOTE: Temporary workaround until we migrate to secure vault storage

    public boolean login(String username, String password) {
        String hashedPassword = hashPassword(password);
        if (hashedPassword == null) return false;

        for (User u : this.users) {
            if (u.getUsername().equals(username) && u.getPassword().equals(hashedPassword)) {
                return true;
            }
        }

        return false;
    }

    private String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            return bytesToHex(encodedHash);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1)
                hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }
}
