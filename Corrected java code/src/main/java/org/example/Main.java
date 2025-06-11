package org.example;//méthode services

import org.example.services.*;
import java.util.Scanner;//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        TaskServices taskService = new TaskServices();

        System.out.println("==== ToDo App ====");

        String username = null;
        while (username == null) {
            System.out.println("\n1. Connect\n2. Register\nChoice : ");
            int choix = sc.nextInt();
            sc.nextLine();

            System.out.print("Username : ");
            String user = sc.nextLine();
            System.out.print("Password: ");
            String pass = sc.nextLine();

            if (choix == 1 && AuthBcrypt.login(user, pass)) {//login function
                username = user;
                System.out.println("Successful connection !");
            } else if (choix == 2 && AuthBcrypt.register(user, pass)) {
                username = user;
                System.out.println("Successful registration !");
            } else {
                System.out.println("Failed. Please try again !");
            }
        }

        while (true) {
            System.out.println("\n--- Menu ---");
            System.out.println("1. See my tasks");
            System.out.println("2. Add a new task");
            System.out.println("3. Modify a task");
            System.out.println("4. Delete a task");
            System.out.println("5. Put a task as done/not done");
            System.out.println("0. Quit");

            int choix = sc.nextInt();
            sc.nextLine();

            switch (choix) {
                case 1 -> taskService.listTasks(username);
                case 2 -> {
                    System.out.print("Description : ");
                    String desc = sc.nextLine();
                    taskService.addTask(username, desc);
                }
                case 3 -> {
                    System.out.print("ID of the task : ");
                    int id = sc.nextInt();
                    sc.nextLine();
                    System.out.print("New description : ");
                    String newDesc = sc.nextLine();
                    taskService.updateTask(username, id, newDesc);
                }
                case 4 -> {
                    System.out.print("ID of the task : ");
                    int id = sc.nextInt();
                    sc.nextLine();
                    taskService.deleteTask(username, id);
                }
                case 5 -> {
                    System.out.print("ID of the task : ");
                    int id = sc.nextInt();
                    sc.nextLine();
                    taskService.toggleComplete(username, id);
                }
                case 0 -> {
                    System.out.println("Goodbye !");
                    return;
                }
                default -> System.out.println("Invalid choice");
            }
        }
    }
}