package org.example.services;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import org.example.model.Task;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class TaskServices {
    private static final String TASK_FILE = "src/main/java/org/example/data/tasks.json"; //Non vulnerable to file path traversal
    private List<Task> tasks = new ArrayList<>();
    private final Gson gson = new Gson();
    private int currentId = 1;

    public TaskServices() {
        loadTasks();
        if (!tasks.isEmpty()) {
            currentId = tasks.stream().mapToInt(Task::getId).max().getAsInt() + 1;
        }
    } //This always reads/writes to a fixed location like data/tasks.json and don't permit the file path traversal vulnerability

    private void loadTasks() {
        try (Reader reader = new FileReader(TASK_FILE)) {
            tasks = gson.fromJson(reader, new TypeToken<List<Task>>(){}.getType());
            if (tasks == null) tasks = new ArrayList<>();
        } catch (IOException e) {
            tasks = new ArrayList<>();
        }
    }

    private void saveTasks() {
        try (Writer writer = new FileWriter(TASK_FILE)) {
            gson.toJson(tasks, writer);
        } catch (IOException e) {
            System.out.println("Error when saving tasks");
        }
    }

    public void addTask(String username, String desc) {
        Task task = new Task(currentId++, username, desc);
        tasks.add(task);
        saveTasks();
    }

    public void listTasks(String username) {
        for (Task t : tasks) {
            if (t.getUsername().equals(username)) {
                System.out.printf("ID: %d | %s [%s]\n", t.getId(), t.getDescription(), t.isCompleted() ? "✔" : "✘");
            }
        }
    }

    public void updateTask(String username, int id, String newDesc) {
        for (Task t : tasks) {
            if (t.getId() == id && t.getUsername().equals(username)) {
                t.setDescription(newDesc);
                saveTasks();
                return;
            }
        }
        System.out.println("Task not found or not authorized");
    }

    public void deleteTask(String username, int id) {
        tasks.removeIf(t -> t.getId() == id && t.getUsername().equals(username));
        saveTasks();
    }

    public void toggleComplete(String username, int id) {
        for (Task t : tasks) {
            if (t.getId() == id && t.getUsername().equals(username)) {
                t.setCompleted(!t.isCompleted());
                saveTasks();
                return;
            }
        }
    }
}
