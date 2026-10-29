package com.shadowfax.helpdesk;

import com.shadowfax.helpdesk.exception.InvalidTicketOperationException;
import com.shadowfax.helpdesk.exception.TicketNotFoundException;
import com.shadowfax.helpdesk.model.*;
import com.shadowfax.helpdesk.repository.TicketRepository;
import com.shadowfax.helpdesk.service.TicketService;
import com.shadowfax.helpdesk.util.InputUtil;
import com.shadowfax.helpdesk.util.TicketIdGenerator;

import java.util.List;
import java.util.Scanner;

/**
 * Main application entry point for the Shadowfax Internal Helpdesk System.
 * Coordinates user interaction, displays the console menu, and delegates
 * business operations to the TicketService.
 */
public class Main {

    private final TicketService ticketService;
    private final InputUtil inputUtil;

    public Main(TicketService ticketService, InputUtil inputUtil) {
        this.ticketService = ticketService;
        this.inputUtil = inputUtil;
    }

    public static void main(String[] args) {
        if (args.length > 0) {
            if ("--web".equalsIgnoreCase(args[0])) {
                com.shadowfax.helpdesk.web.TicketWebServer.main(args);
                return;
            } else if ("--gui".equalsIgnoreCase(args[0])) {
                com.shadowfax.helpdesk.gui.TicketSwingApp.main(args);
                return;
            }
        }

        TicketRepository repository = new TicketRepository();
        TicketIdGenerator idGenerator = new TicketIdGenerator();
        TicketService ticketService = new TicketService(repository, idGenerator);
        Scanner scanner = new Scanner(System.in);
        InputUtil inputUtil = new InputUtil(scanner);

        Main app = new Main(ticketService, inputUtil);
        app.run();
    }

    /**
     * Main application loop. Displays the menu and processes user actions.
     */
    public void run() {
        boolean running = true;
        System.out.println();
        System.out.println("=================================================");
        System.out.println("  Welcome to ShadowDesk Internal Helpdesk System ");
        System.out.println("=================================================");

        while (running) {
            displayMainMenu();
            int choice = inputUtil.readInt("Select an option (0-13): ", 0, 13);
            System.out.println();

            switch (choice) {
                case 1:
                    handleCreateTicket();
                    break;
                case 2:
                    handleViewAllTickets();
                    break;
                case 3:
                    handleSearchTicket();
                    break;
                case 4:
                    handleUpdateTicket();
                    break;
                case 5:
                    handleChangeStatus();
                    break;
                case 6:
                    handleResolveTicket();
                    break;
                case 7:
                    handleCloseTicket();
                    break;
                case 8:
                    handleReopenTicket();
                    break;
                case 9:
                    handleFilterTickets();
                    break;
                case 10:
                    handleDashboard();
                    break;
                case 11:
                    handleSaveData();
                    break;
                case 12:
                    handleLaunchWebUi();
                    break;
                case 13:
                    handleLaunchSwingGui();
                    break;
                case 0:
                    handleExit();
                    running = false;
                    break;
                default:
                    System.out.println("[Error] Unknown menu selection.");
            }
            System.out.println();
        }
    }

    private void displayMainMenu() {
        System.out.println("=============================================");
        System.out.println("     SHADOWDESK INTERNAL HELPDESK SYSTEM     ");
        System.out.println("=============================================");
        System.out.println(" 1. Create New Ticket");
        System.out.println(" 2. View All Tickets");
        System.out.println(" 3. Search Ticket by ID");
        System.out.println(" 4. Update Ticket Details");
        System.out.println(" 5. Change Ticket Status");
        System.out.println(" 6. Resolve Ticket");
        System.out.println(" 7. Close Ticket");
        System.out.println(" 8. Reopen Ticket");
        System.out.println(" 9. Filter Tickets");
        System.out.println(" 10. Dashboard / Statistics");
        System.out.println(" 11. Save Data");
        System.out.println(" 12. Launch Web Dashboard (Browser UI)");
        System.out.println(" 13. Launch Desktop Window (Java Swing GUI)");
        System.out.println(" 0. Exit");
        System.out.println("=============================================");
    }

    // 1. Create New Ticket
    private void handleCreateTicket() {
        System.out.println("--- [CREATE NEW SUPPORT TICKET] ---");
        String title = inputUtil.readNonBlankString("Enter ticket title/summary: ");
        String description = inputUtil.readNonBlankString("Enter detailed description: ");
        TicketCategory category = inputUtil.selectCategory("Select Ticket Category:");
        TicketPriority priority = inputUtil.selectPriority("Select Ticket Priority:");
        String createdBy = inputUtil.readNonBlankString("Enter requester name / Employee ID: ");

        System.out.println("Assign to Support Agent:");
        System.out.println("  1. Unassigned");
        System.out.println("  2. Rahul Sharma (IT Support)");
        System.out.println("  3. Priya Patel (Operations Lead)");
        System.out.println("  4. Arun Kumar (Logistics Helpdesk)");
        System.out.println("  5. Custom Agent Name");
        int assignChoice = inputUtil.readInt("Select assignee option (1-5): ", 1, 5);
        String assignedTo = "Unassigned";
        if (assignChoice == 2) assignedTo = "Rahul Sharma";
        else if (assignChoice == 3) assignedTo = "Priya Patel";
        else if (assignChoice == 4) assignedTo = "Arun Kumar";
        else if (assignChoice == 5) {
            assignedTo = inputUtil.readNonBlankString("Enter Support Agent name: ");
        }

        try {
            Ticket ticket = ticketService.createTicket(title, description, category, priority, createdBy, assignedTo);
            System.out.println();
            System.out.println("[SUCCESS] Ticket created successfully!");
            System.out.println("Generated Ticket ID : " + ticket.getTicketId());
            System.out.println("Status              : " + ticket.getStatus().getDisplayName());
            System.out.println("Priority            : " + ticket.getPriority().getDisplayName());
            System.out.println("Category            : " + ticket.getCategory().getDisplayName());
        } catch (IllegalArgumentException e) {
            System.out.println("[ERROR] Failed to create ticket: " + e.getMessage());
        }
    }

    // 2. View All Tickets
    private void handleViewAllTickets() {
        System.out.println("--- [ALL SUPPORT TICKETS] ---");
        List<Ticket> tickets = ticketService.getAllTickets();
        displayTicketTable(tickets);
    }

    // 3. Search Ticket by ID
    private void handleSearchTicket() {
        System.out.println("--- [SEARCH TICKET BY ID] ---");
        String ticketId = inputUtil.readNonBlankString("Enter Ticket ID (e.g. SF-1001): ");
        try {
            Ticket ticket = ticketService.getTicketById(ticketId);
            displayDetailedTicket(ticket);
        } catch (TicketNotFoundException e) {
            System.out.println("[NOT FOUND] " + e.getMessage());
        }
    }

    // 4. Update Ticket Details
    private void handleUpdateTicket() {
        System.out.println("--- [UPDATE TICKET DETAILS] ---");
        String ticketId = inputUtil.readNonBlankString("Enter Ticket ID to update: ");
        try {
            Ticket ticket = ticketService.getTicketById(ticketId);
            System.out.println("\nTarget Ticket: " + ticket.getTicketId() + " - " + ticket.getTitle());
            System.out.println("(Press Enter directly to keep existing value for text fields)\n");

            String newTitle = inputUtil.readOptionalString("New Title [" + ticket.getTitle() + "]: ", ticket.getTitle());
            String newDescription = inputUtil.readOptionalString("New Description [" + truncate(ticket.getDescription(), 30) + "]: ", ticket.getDescription());
            TicketCategory newCategory = inputUtil.selectOptionalCategory("Update Category:", ticket.getCategory());
            TicketPriority newPriority = inputUtil.selectOptionalPriority("Update Priority:", ticket.getPriority());
            String newAssignee = inputUtil.readOptionalString("New Assignee [" + ticket.getAssignedTo() + "]: ", ticket.getAssignedTo());

            ticketService.updateTicketDetails(ticketId, newTitle, newDescription, newCategory, newPriority, newAssignee);
            System.out.println("\n[SUCCESS] Ticket " + ticket.getTicketId() + " details updated successfully.");
        } catch (TicketNotFoundException e) {
            System.out.println("[NOT FOUND] " + e.getMessage());
        } catch (IllegalArgumentException e) {
            System.out.println("[ERROR] Validation error: " + e.getMessage());
        }
    }

    // 5. Change Ticket Status
    private void handleChangeStatus() {
        System.out.println("--- [CHANGE TICKET STATUS] ---");
        String ticketId = inputUtil.readNonBlankString("Enter Ticket ID: ");
        try {
            Ticket ticket = ticketService.getTicketById(ticketId);
            System.out.println("Current Status: " + ticket.getStatus().getDisplayName());

            List<TicketStatus> availableTransitions = ticketService.getAvailableTransitions(ticket.getStatus());
            if (availableTransitions.isEmpty()) {
                System.out.println("[INFO] No direct status transitions available for status '" +
                        ticket.getStatus().getDisplayName() + "'. Use Reopen (Option 8) if needed.");
                return;
            }

            System.out.println("Permitted Next Statuses:");
            for (int i = 0; i < availableTransitions.size(); i++) {
                System.out.println("  " + (i + 1) + ". " + availableTransitions.get(i).getDisplayName());
            }
            System.out.println("  0. Cancel");

            int choice = inputUtil.readInt("Select transition (0-" + availableTransitions.size() + "): ", 0, availableTransitions.size());
            if (choice == 0) {
                System.out.println("Status transition cancelled.");
                return;
            }

            TicketStatus targetStatus = availableTransitions.get(choice - 1);
            if (targetStatus == TicketStatus.RESOLVED) {
                System.out.println("[INFO] To resolve a ticket, resolution notes are required.");
                String notes = inputUtil.readNonBlankString("Enter resolution notes: ");
                ticketService.resolveTicket(ticketId, notes);
                System.out.println("[SUCCESS] Ticket " + ticketId + " has been moved to RESOLVED.");
            } else {
                ticketService.changeStatus(ticketId, targetStatus);
                System.out.println("[SUCCESS] Ticket " + ticketId + " status changed to " + targetStatus.getDisplayName() + ".");
            }
        } catch (TicketNotFoundException e) {
            System.out.println("[NOT FOUND] " + e.getMessage());
        } catch (InvalidTicketOperationException e) {
            System.out.println("[REJECTED] " + e.getMessage());
        }
    }

    // 6. Resolve Ticket
    private void handleResolveTicket() {
        System.out.println("--- [RESOLVE TICKET] ---");
        String ticketId = inputUtil.readNonBlankString("Enter Ticket ID to resolve: ");
        try {
            Ticket ticket = ticketService.getTicketById(ticketId);
            System.out.println("Ticket: " + ticket.getTicketId() + " | Current Status: " + ticket.getStatus().getDisplayName());
            String notes = inputUtil.readNonBlankString("Enter detailed resolution notes: ");

            ticketService.resolveTicket(ticketId, notes);
            System.out.println("\n[SUCCESS] Ticket " + ticketId + " successfully marked as RESOLVED.");
        } catch (TicketNotFoundException e) {
            System.out.println("[NOT FOUND] " + e.getMessage());
        } catch (InvalidTicketOperationException e) {
            System.out.println("[REJECTED] " + e.getMessage());
        }
    }

    // 7. Close Ticket
    private void handleCloseTicket() {
        System.out.println("--- [CLOSE TICKET] ---");
        String ticketId = inputUtil.readNonBlankString("Enter Ticket ID to close: ");
        try {
            Ticket ticket = ticketService.getTicketById(ticketId);
            System.out.println("Ticket: " + ticket.getTicketId() + " | Current Status: " + ticket.getStatus().getDisplayName());

            ticketService.closeTicket(ticketId);
            System.out.println("\n[SUCCESS] Ticket " + ticketId + " has been officially CLOSED.");
        } catch (TicketNotFoundException e) {
            System.out.println("[NOT FOUND] " + e.getMessage());
        } catch (InvalidTicketOperationException e) {
            System.out.println("[REJECTED] " + e.getMessage());
        }
    }

    // 8. Reopen Ticket
    private void handleReopenTicket() {
        System.out.println("--- [REOPEN TICKET] ---");
        String ticketId = inputUtil.readNonBlankString("Enter Ticket ID to reopen: ");
        try {
            Ticket ticket = ticketService.getTicketById(ticketId);
            System.out.println("Ticket: " + ticket.getTicketId() + " | Current Status: " + ticket.getStatus().getDisplayName());
            String reason = inputUtil.readOptionalString("Reason for reopening (optional): ", "Customer requested reopening");

            ticketService.reopenTicket(ticketId, reason);
            System.out.println("\n[SUCCESS] Ticket " + ticketId + " has been REOPENED. Status is now OPEN.");
            System.out.println("Historical resolution notes have been safely preserved.");
        } catch (TicketNotFoundException e) {
            System.out.println("[NOT FOUND] " + e.getMessage());
        } catch (InvalidTicketOperationException e) {
            System.out.println("[REJECTED] " + e.getMessage());
        }
    }

    // 9. Filter Tickets
    private void handleFilterTickets() {
        System.out.println("--- [FILTER TICKETS] ---");
        System.out.println("1. Filter by Status");
        System.out.println("2. Filter by Priority");
        System.out.println("3. Filter by Category");
        System.out.println("4. Filter by Creator / Requester");
        System.out.println("0. Cancel");

        int choice = inputUtil.readInt("Select filter type (0-4): ", 0, 4);
        if (choice == 0) return;

        List<Ticket> filtered = null;
        switch (choice) {
            case 1:
                System.out.println("Select Status to filter by:");
                TicketStatus[] statuses = TicketStatus.values();
                for (int i = 0; i < statuses.length; i++) {
                    System.out.println("  " + (i + 1) + ". " + statuses[i].getDisplayName());
                }
                int stChoice = inputUtil.readInt("Choice (1-" + statuses.length + "): ", 1, statuses.length);
                filtered = ticketService.filterTickets(statuses[stChoice - 1], null, null, null);
                break;
            case 2:
                TicketPriority priority = inputUtil.selectPriority("Select Priority to filter by:");
                filtered = ticketService.filterTickets(null, priority, null, null);
                break;
            case 3:
                TicketCategory category = inputUtil.selectCategory("Select Category to filter by:");
                filtered = ticketService.filterTickets(null, null, category, null);
                break;
            case 4:
                String creator = inputUtil.readNonBlankString("Enter requester name to search: ");
                filtered = ticketService.filterTickets(null, null, null, creator);
                break;
        }

        System.out.println();
        System.out.println("--- [FILTER RESULTS] ---");
        displayTicketTable(filtered);
    }

    // 10. Dashboard / Statistics
    private void handleDashboard() {
        DashboardStats stats = ticketService.getDashboardStatistics();
        System.out.println("=================================================");
        System.out.println("       HELPDESK OPERATIONAL DASHBOARD            ");
        System.out.println("=================================================");
        System.out.printf("  Total Tickets Logged   : %d%n", stats.getTotalTickets());
        System.out.printf("  [-] Open Tickets       : %d%n", stats.getOpenCount());
        System.out.printf("  [-] In-Progress Tickets: %d%n", stats.getInProgressCount());
        System.out.printf("  [-] Resolved Tickets   : %d%n", stats.getResolvedCount());
        System.out.printf("  [-] Closed Tickets     : %d%n", stats.getClosedCount());
        System.out.println("-------------------------------------------------");
        System.out.printf("  [!] High Priority      : %d%n", stats.getHighPriorityCount());
        System.out.printf("  [!] Critical Priority  : %d%n", stats.getCriticalPriorityCount());
        System.out.println("=================================================");
    }

    // 11. Save Data
    private void handleSaveData() {
        boolean ok = ticketService.saveData();
        if (ok) {
            System.out.println("[SUCCESS] All tickets have been saved to storage.");
        } else {
            System.out.println("[ERROR] Failed to save ticket data to storage.");
        }
    }

    // 12. Launch Web Dashboard
    private void handleLaunchWebUi() {
        System.out.println("--- [LAUNCHING WEB DASHBOARD] ---");
        try {
            com.shadowfax.helpdesk.web.TicketWebServer webServer = new com.shadowfax.helpdesk.web.TicketWebServer(ticketService, 8080);
            webServer.start();
            System.out.println("[SUCCESS] Web Dashboard started successfully on http://localhost:8080");
            System.out.println("You can view, search, resolve, and create tickets in your web browser!");

            if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI("http://localhost:8080"));
                System.out.println("Opening browser automatically...");
            }
        } catch (Exception e) {
            System.out.println("[INFO] Web server status/info: " + e.getMessage());
            System.out.println("If already running, open http://localhost:8080 in your browser.");
        }
    }

    // 13. Launch Desktop Window
    private void handleLaunchSwingGui() {
        System.out.println("--- [LAUNCHING DESKTOP WINDOW] ---");
        try {
            javax.swing.SwingUtilities.invokeLater(() -> {
                try {
                    javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
                } catch (Exception ignored) {}
                com.shadowfax.helpdesk.gui.TicketSwingApp gui = new com.shadowfax.helpdesk.gui.TicketSwingApp(ticketService);
                gui.setDefaultCloseOperation(javax.swing.JFrame.DISPOSE_ON_CLOSE);
                gui.setVisible(true);
            });
            System.out.println("[SUCCESS] Desktop Swing window opened!");
        } catch (Exception e) {
            System.out.println("[ERROR] Unable to open Desktop window: " + e.getMessage());
        }
    }

    // 0. Exit
    private void handleExit() {
        System.out.println("Saving ticket data before exit...");
        ticketService.saveData();
        System.out.println("Thank you for using ShadowDesk Helpdesk System. Goodbye!");
    }

    // UI Helper: Table view
    private void displayTicketTable(List<Ticket> tickets) {
        if (tickets == null || tickets.isEmpty()) {
            System.out.println("No tickets found matching the specified criteria.");
            return;
        }

        String format = "| %-8s | %-24s | %-16s | %-9s | %-12s | %-14s | %-16s |%n";
        String separator = "+----------+--------------------------+------------------+-----------+--------------+----------------+------------------+";

        System.out.println(separator);
        System.out.printf(format, "ID", "Title", "Category", "Priority", "Status", "Creator", "Created At");
        System.out.println(separator);

        for (Ticket t : tickets) {
            System.out.printf(format,
                    t.getTicketId(),
                    truncate(t.getTitle(), 24),
                    truncate(t.getCategory().getDisplayName(), 16),
                    t.getPriority().getDisplayName(),
                    t.getStatus().getDisplayName(),
                    truncate(t.getCreatedBy(), 14),
                    t.getFormattedCreatedAt());
        }
        System.out.println(separator);
        System.out.println("Total displayed: " + tickets.size() + " ticket(s). Use option 3 to view full details.");
    }

    // UI Helper: Detailed View
    private void displayDetailedTicket(Ticket t) {
        System.out.println("----------------------------------------------------------------------");
        System.out.println(" TICKET DETAILS: " + t.getTicketId());
        System.out.println("----------------------------------------------------------------------");
        System.out.println(" Title           : " + t.getTitle());
        System.out.println(" Status          : " + t.getStatus().getDisplayName());
        System.out.println(" Priority        : " + t.getPriority().getDisplayName());
        System.out.println(" Category        : " + t.getCategory().getDisplayName());
        System.out.println(" Requester       : " + t.getCreatedBy());
        System.out.println(" Support Assignee: " + t.getAssignedTo());
        System.out.println(" Created At      : " + t.getFormattedCreatedAt());
        System.out.println(" Last Updated    : " + t.getFormattedUpdatedAt());
        System.out.println("----------------------------------------------------------------------");
        System.out.println(" DESCRIPTION:");
        System.out.println(" " + t.getDescription());
        System.out.println("----------------------------------------------------------------------");
        System.out.println(" RESOLUTION / AUDIT NOTES:");
        if (t.getResolutionNotes() != null && !t.getResolutionNotes().trim().isEmpty()) {
            System.out.println(" " + t.getResolutionNotes());
        } else {
            System.out.println(" (None recorded yet)");
        }
        System.out.println("----------------------------------------------------------------------");
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }
}
