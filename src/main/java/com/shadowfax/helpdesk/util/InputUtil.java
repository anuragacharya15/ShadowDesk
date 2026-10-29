package com.shadowfax.helpdesk.util;

import com.shadowfax.helpdesk.model.TicketCategory;
import com.shadowfax.helpdesk.model.TicketPriority;
import com.shadowfax.helpdesk.model.TicketStatus;

import java.util.List;
import java.util.Scanner;

/**
 * Reusable utility for safe console input reading and validation.
 * Uses line-based reading (nextLine) throughout to prevent Scanner newline bugs.
 */
public class InputUtil {

    private final Scanner scanner;

    public InputUtil(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * Reads a required non-blank string from the console.
     */
    public String readNonBlankString(String prompt) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            if (line != null && !line.trim().isEmpty()) {
                return line.trim();
            }
            System.out.println("[Error] Input cannot be blank. Please enter a valid value.");
        }
    }

    /**
     * Reads an optional string. If user presses Enter without typing, defaultValue is returned.
     */
    public String readOptionalString(String prompt, String defaultValue) {
        System.out.print(prompt);
        String line = scanner.nextLine();
        if (line == null || line.trim().isEmpty()) {
            return defaultValue;
        }
        return line.trim();
    }

    /**
     * Reads an integer within [min, max] range. Handles non-numeric inputs gracefully.
     */
    public int readInt(String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String line = scanner.nextLine();
            if (line == null || line.trim().isEmpty()) {
                System.out.println("[Error] Input cannot be empty. Please enter a number between " + min + " and " + max + ".");
                continue;
            }

            try {
                int value = Integer.parseInt(line.trim());
                if (value >= min && value <= max) {
                    return value;
                }
                System.out.println("[Error] Selection out of range. Expected between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("[Error] Invalid input '" + line.trim() + "'. Please enter a valid whole number.");
            }
        }
    }

    /**
     * Presents categories as a numbered menu and returns the user's validated selection.
     */
    public TicketCategory selectCategory(String prompt) {
        System.out.println(prompt);
        TicketCategory[] categories = TicketCategory.values();
        for (int i = 0; i < categories.length; i++) {
            System.out.println("  " + (i + 1) + ". " + categories[i].getDisplayName());
        }
        int choice = readInt("Select category (1-" + categories.length + "): ", 1, categories.length);
        return categories[choice - 1];
    }

    /**
     * Optional category selection: allows keeping current category or changing it.
     */
    public TicketCategory selectOptionalCategory(String prompt, TicketCategory current) {
        System.out.println(prompt);
        TicketCategory[] categories = TicketCategory.values();
        System.out.println("  0. Keep Current (" + current.getDisplayName() + ")");
        for (int i = 0; i < categories.length; i++) {
            System.out.println("  " + (i + 1) + ". " + categories[i].getDisplayName());
        }
        int choice = readInt("Select category (0-" + categories.length + "): ", 0, categories.length);
        if (choice == 0) {
            return current;
        }
        return categories[choice - 1];
    }

    /**
     * Presents priorities as a numbered menu and returns the user's validated selection.
     */
    public TicketPriority selectPriority(String prompt) {
        System.out.println(prompt);
        TicketPriority[] priorities = TicketPriority.values();
        for (int i = 0; i < priorities.length; i++) {
            System.out.println("  " + (i + 1) + ". " + priorities[i].getDisplayName());
        }
        int choice = readInt("Select priority (1-" + priorities.length + "): ", 1, priorities.length);
        return priorities[choice - 1];
    }

    /**
     * Optional priority selection: allows keeping current priority or changing it.
     */
    public TicketPriority selectOptionalPriority(String prompt, TicketPriority current) {
        System.out.println(prompt);
        TicketPriority[] priorities = TicketPriority.values();
        System.out.println("  0. Keep Current (" + current.getDisplayName() + ")");
        for (int i = 0; i < priorities.length; i++) {
            System.out.println("  " + (i + 1) + ". " + priorities[i].getDisplayName());
        }
        int choice = readInt("Select priority (0-" + priorities.length + "): ", 0, priorities.length);
        if (choice == 0) {
            return current;
        }
        return priorities[choice - 1];
    }

    /**
     * Prompts the user to select one of the allowed target statuses.
     */
    public TicketStatus selectStatusFromList(String prompt, List<TicketStatus> allowedStatuses) {
        if (allowedStatuses == null || allowedStatuses.isEmpty()) {
            return null;
        }
        System.out.println(prompt);
        for (int i = 0; i < allowedStatuses.size(); i++) {
            System.out.println("  " + (i + 1) + ". " + allowedStatuses.get(i).getDisplayName());
        }
        int choice = readInt("Select next status (1-" + allowedStatuses.size() + "): ", 1, allowedStatuses.size());
        return allowedStatuses.get(choice - 1);
    }

    /**
     * Reads a yes/no confirmation from the console.
     */
    public boolean readConfirmation(String prompt) {
        while (true) {
            System.out.print(prompt + " (y/n): ");
            String line = scanner.nextLine();
            if (line == null) {
                continue;
            }
            String trimmed = line.trim().toLowerCase();
            if ("y".equals(trimmed) || "yes".equals(trimmed)) {
                return true;
            }
            if ("n".equals(trimmed) || "no".equals(trimmed)) {
                return false;
            }
            System.out.println("[Error] Please enter 'y' for Yes or 'n' for No.");
        }
    }
}
