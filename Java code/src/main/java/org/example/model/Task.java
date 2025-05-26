package org.example.model;

public class Task {
    private int id;
    private String username; // Owner of task
    private String description;
    private boolean completed;

    public Task() {}

    public Task(int id, String username, String description) {
        this.id = id;
        this.username = username;
        this.description = description;
        this.completed = false;
    }

    public int getId() { return id; }
    public String getUsername() { return username; }
    public String getDescription() { return description; }
    public boolean isCompleted() { return completed; }

    public void setDescription(String description) { this.description = description; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}
