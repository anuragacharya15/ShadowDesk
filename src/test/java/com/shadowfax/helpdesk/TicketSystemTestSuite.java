package com.shadowfax.helpdesk;

import com.shadowfax.helpdesk.exception.InvalidTicketOperationException;
import com.shadowfax.helpdesk.exception.TicketNotFoundException;
import com.shadowfax.helpdesk.model.*;
import com.shadowfax.helpdesk.repository.TicketRepository;
import com.shadowfax.helpdesk.service.TicketService;
import com.shadowfax.helpdesk.util.InputUtil;
import com.shadowfax.helpdesk.util.TicketIdGenerator;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Scanner;

/**
 * Automated test suite verifying all 17 functional and validation scenarios
 * specified for the Shadowfax Internal Helpdesk System.
 */
public class TicketSystemTestSuite {

    private static int testsPassed = 0;
    private static int testsFailed = 0;

    public static void main(String[] args) {
        System.out.println("===============================================================");
        System.out.println(" SHADOWFAX HELPDESK SYSTEM - COMPREHENSIVE TEST SUITE ");
        System.out.println("===============================================================");

        runTest("TEST 1: Create a valid ticket and verify that an ID is generated", TicketSystemTestSuite::test1_createValidTicket);
        runTest("TEST 2: Reject ticket creation with blank title", TicketSystemTestSuite::test2_rejectBlankTitle);
        runTest("TEST 3: Reject invalid priority or category", TicketSystemTestSuite::test3_rejectInvalidPriorityOrCategory);
        runTest("TEST 4: Create multiple tickets and verify IDs are unique", TicketSystemTestSuite::test4_uniqueTicketIds);
        runTest("TEST 5: Search for existing ticket and verify details", TicketSystemTestSuite::test5_searchExistingTicket);
        runTest("TEST 6: Search nonexistent ticket handled safely without crash", TicketSystemTestSuite::test6_searchNonexistentTicket);
        runTest("TEST 7: Update ticket title or priority and verify changes", TicketSystemTestSuite::test7_updateTicketDetails);
        runTest("TEST 8: Resolve ticket with resolution notes and verify", TicketSystemTestSuite::test8_resolveTicket);
        runTest("TEST 9: Reject closing an OPEN ticket", TicketSystemTestSuite::test9_rejectClosingOpenTicket);
        runTest("TEST 10: Complete lifecycle: OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED", TicketSystemTestSuite::test10_validLifecycleTransitions);
        runTest("TEST 11: Reject invalid status transition (CLOSED -> IN_PROGRESS)", TicketSystemTestSuite::test11_rejectInvalidTransition);
        runTest("TEST 12: Reopen a CLOSED ticket back to OPEN preserving notes", TicketSystemTestSuite::test12_reopenTicket);
        runTest("TEST 13: Filter tickets by status, priority, category, and creator", TicketSystemTestSuite::test13_filterTickets);
        runTest("TEST 14: Dynamic dashboard statistics validation", TicketSystemTestSuite::test14_dashboardStatistics);
        runTest("TEST 15: Data persistence across restarts (save, reload, counter recovery)", TicketSystemTestSuite::test15_dataPersistence);
        runTest("TEST 16: Graceful handling of invalid console inputs (letters, out-of-range)", TicketSystemTestSuite::test16_inputValidationResilience);
        runTest("TEST 17: Storage safety: Load failure does not wipe existing saved data", TicketSystemTestSuite::test17_safePersistenceOnLoadError);

        System.out.println("===============================================================");
        System.out.printf(" RESULTS: Passed: %d | Failed: %d | Total: %d%n", testsPassed, testsFailed, (testsPassed + testsFailed));
        System.out.println("===============================================================");

        if (testsFailed > 0) {
            System.exit(1);
        }
    }

    private static void runTest(String testName, TestCase testCase) {
        System.out.print("[RUNNING] " + testName + " ... ");
        try {
            testCase.execute();
            System.out.println("PASSED");
            testsPassed++;
        } catch (Throwable t) {
            System.out.println("FAILED");
            System.out.println("  --> Reason: " + t.getMessage());
            t.printStackTrace(System.out);
            testsFailed++;
        }
    }

    @FunctionalInterface
    interface TestCase {
        void execute() throws Exception;
    }

    private static TicketService createIsolatedService() {
        String testFile = "data/test_tickets_" + System.currentTimeMillis() + ".csv";
        TicketRepository repo = new TicketRepository(testFile);
        TicketIdGenerator idGen = new TicketIdGenerator();
        return new TicketService(repo, idGen);
    }

    // TEST 1
    private static void test1_createValidTicket() {
        TicketService service = createIsolatedService();
        Ticket ticket = service.createTicket(
                "Driver App Login Failure",
                "Delivery driver cannot authenticate on Android app v4.2",
                TicketCategory.DELIVERY_OPERATIONS,
                TicketPriority.HIGH,
                "Ramesh (Driver #401)",
                "Rahul Sharma"
        );
        assertTrue(ticket != null, "Ticket should not be null");
        assertTrue(ticket.getTicketId() != null && ticket.getTicketId().startsWith("SF-"), "Ticket ID should start with SF-");
        assertTrue(ticket.getStatus() == TicketStatus.OPEN, "Initial status must be OPEN");
        assertTrue("Driver App Login Failure".equals(ticket.getTitle()), "Title should match");
    }

    // TEST 2
    private static void test2_rejectBlankTitle() {
        TicketService service = createIsolatedService();
        boolean threw = false;
        try {
            service.createTicket(
                    "   ",
                    "Valid description",
                    TicketCategory.TECHNICAL,
                    TicketPriority.LOW,
                    "Employee A",
                    "Unassigned"
            );
        } catch (IllegalArgumentException e) {
            threw = true;
        }
        assertTrue(threw, "Creating ticket with blank title must throw IllegalArgumentException");
    }

    // TEST 3
    private static void test3_rejectInvalidPriorityOrCategory() {
        TicketService service = createIsolatedService();
        boolean threwCategory = false;
        try {
            service.createTicket("Title", "Description", null, TicketPriority.LOW, "Employee A", "Unassigned");
        } catch (IllegalArgumentException e) {
            threwCategory = true;
        }
        assertTrue(threwCategory, "Null category must be rejected");

        boolean threwPriority = false;
        try {
            service.createTicket("Title", "Description", TicketCategory.TECHNICAL, null, "Employee A", "Unassigned");
        } catch (IllegalArgumentException e) {
            threwPriority = true;
        }
        assertTrue(threwPriority, "Null priority must be rejected");

        // Test Enum parsing of invalid strings
        assertTrue(TicketCategory.fromString("NON_EXISTENT") == null, "Invalid category string should parse to null");
        assertTrue(TicketPriority.fromString("SUPER_CRITICAL") == null, "Invalid priority string should parse to null");
    }

    // TEST 4
    private static void test4_uniqueTicketIds() {
        TicketService service = createIsolatedService();
        Ticket t1 = service.createTicket("Ticket 1", "Desc 1", TicketCategory.TECHNICAL, TicketPriority.LOW, "User 1", null);
        Ticket t2 = service.createTicket("Ticket 2", "Desc 2", TicketCategory.HARDWARE, TicketPriority.MEDIUM, "User 2", null);
        Ticket t3 = service.createTicket("Ticket 3", "Desc 3", TicketCategory.OTHER, TicketPriority.HIGH, "User 3", null);

        assertTrue(!t1.getTicketId().equals(t2.getTicketId()), "IDs must be unique");
        assertTrue(!t2.getTicketId().equals(t3.getTicketId()), "IDs must be unique");
        assertTrue(!t1.getTicketId().equals(t3.getTicketId()), "IDs must be unique");
    }

    // TEST 5
    private static void test5_searchExistingTicket() throws Exception {
        TicketService service = createIsolatedService();
        Ticket created = service.createTicket("Warehouse scanner broken", "Scanner #12 barcode laser not emitting",
                TicketCategory.HARDWARE, TicketPriority.CRITICAL, "Sunil (Hub Lead)", "Arun Kumar");

        Ticket found = service.getTicketById(created.getTicketId());
        assertTrue(found != null, "Ticket should be found");
        assertTrue(found.getTicketId().equals(created.getTicketId()), "IDs must match");
        assertTrue("Warehouse scanner broken".equals(found.getTitle()), "Title must match");
        assertTrue(found.getPriority() == TicketPriority.CRITICAL, "Priority must match");

        // Case-insensitive ID lookup test (e.g. "sf-1001" vs "SF-1001")
        Ticket foundLower = service.getTicketById(created.getTicketId().toLowerCase());
        assertTrue(foundLower != null, "Lookup should be case-insensitive");
    }

    // TEST 6
    private static void test6_searchNonexistentTicket() {
        TicketService service = createIsolatedService();
        boolean caught = false;
        try {
            service.getTicketById("SF-99999");
        } catch (TicketNotFoundException e) {
            caught = true;
            assertTrue(e.getMessage().contains("SF-99999"), "Exception message should mention nonexistent ID");
        }
        assertTrue(caught, "TicketNotFoundException should be thrown for unknown ID");
    }

    // TEST 7
    private static void test7_updateTicketDetails() throws Exception {
        TicketService service = createIsolatedService();
        Ticket t = service.createTicket("Original Title", "Original Desc", TicketCategory.TECHNICAL, TicketPriority.LOW, "User 1", "None");

        service.updateTicketDetails(t.getTicketId(), "Updated Title", "Updated Desc", TicketCategory.ACCOUNT_ACCESS, TicketPriority.HIGH, "Priya Patel");

        Ticket updated = service.getTicketById(t.getTicketId());
        assertTrue("Updated Title".equals(updated.getTitle()), "Title should be updated");
        assertTrue("Updated Desc".equals(updated.getDescription()), "Description should be updated");
        assertTrue(updated.getCategory() == TicketCategory.ACCOUNT_ACCESS, "Category should be updated");
        assertTrue(updated.getPriority() == TicketPriority.HIGH, "Priority should be updated");
        assertTrue("Priya Patel".equals(updated.getAssignedTo()), "Assignee should be updated");
    }

    // TEST 8
    private static void test8_resolveTicket() throws Exception {
        TicketService service = createIsolatedService();
        Ticket t = service.createTicket("Issue 8", "Desc 8", TicketCategory.TECHNICAL, TicketPriority.MEDIUM, "User 1", null);

        // Blank resolution notes must be rejected
        boolean rejectedBlankNotes = false;
        try {
            service.resolveTicket(t.getTicketId(), "   ");
        } catch (InvalidTicketOperationException e) {
            rejectedBlankNotes = true;
        }
        assertTrue(rejectedBlankNotes, "Blank resolution notes must be rejected");

        // Valid resolution
        service.resolveTicket(t.getTicketId(), "Replaced network patch cord on switch port 4.");
        Ticket resolved = service.getTicketById(t.getTicketId());
        assertTrue(resolved.getStatus() == TicketStatus.RESOLVED, "Status must be RESOLVED");
        assertTrue(resolved.getResolutionNotes().contains("patch cord"), "Resolution notes must be recorded");
    }

    // TEST 9
    private static void test9_rejectClosingOpenTicket() {
        TicketService service = createIsolatedService();
        Ticket t = service.createTicket("Issue 9", "Desc 9", TicketCategory.TECHNICAL, TicketPriority.LOW, "User 1", null);

        boolean rejected = false;
        try {
            service.closeTicket(t.getTicketId());
        } catch (InvalidTicketOperationException e) {
            rejected = true;
            assertTrue(e.getMessage().contains("RESOLVED"), "Error message should state ticket must be resolved first");
        } catch (Exception e) {
            throw new RuntimeException("Unexpected exception: " + e);
        }
        assertTrue(rejected, "Closing an OPEN ticket directly must be rejected");
    }

    // TEST 10
    private static void test10_validLifecycleTransitions() throws Exception {
        TicketService service = createIsolatedService();
        Ticket t = service.createTicket("Lifecycle Issue", "Tracking state progression", TicketCategory.DELIVERY_OPERATIONS, TicketPriority.HIGH, "Ops Team", "Agent");
        assertTrue(t.getStatus() == TicketStatus.OPEN, "Starts OPEN");

        service.changeStatus(t.getTicketId(), TicketStatus.IN_PROGRESS);
        assertTrue(service.getTicketById(t.getTicketId()).getStatus() == TicketStatus.IN_PROGRESS, "Moved to IN_PROGRESS");

        service.resolveTicket(t.getTicketId(), "Fleet re-routed via alternative transit hub.");
        assertTrue(service.getTicketById(t.getTicketId()).getStatus() == TicketStatus.RESOLVED, "Moved to RESOLVED");

        service.closeTicket(t.getTicketId());
        assertTrue(service.getTicketById(t.getTicketId()).getStatus() == TicketStatus.CLOSED, "Moved to CLOSED");
    }

    // TEST 11
    private static void test11_rejectInvalidTransition() throws Exception {
        TicketService service = createIsolatedService();
        Ticket t = service.createTicket("Issue 11", "Testing illegal moves", TicketCategory.OTHER, TicketPriority.LOW, "User 1", null);
        service.resolveTicket(t.getTicketId(), "Temporary workaround applied");
        service.closeTicket(t.getTicketId());

        boolean rejected = false;
        try {
            // Cannot jump directly from CLOSED to IN_PROGRESS without reopening!
            service.changeStatus(t.getTicketId(), TicketStatus.IN_PROGRESS);
        } catch (InvalidTicketOperationException e) {
            rejected = true;
        }
        assertTrue(rejected, "Direct transition from CLOSED to IN_PROGRESS must be rejected");
    }

    // TEST 12
    private static void test12_reopenTicket() throws Exception {
        TicketService service = createIsolatedService();
        Ticket t = service.createTicket("Issue 12", "Desc", TicketCategory.TECHNICAL, TicketPriority.MEDIUM, "User 1", null);
        service.resolveTicket(t.getTicketId(), "Fixed printer driver.");
        service.closeTicket(t.getTicketId());

        service.reopenTicket(t.getTicketId(), "Printer started jamming again.");
        Ticket reopened = service.getTicketById(t.getTicketId());

        assertTrue(reopened.getStatus() == TicketStatus.OPEN, "Status must be OPEN after reopening");
        assertTrue(reopened.getResolutionNotes().contains("Fixed printer driver"), "Old resolution notes must be preserved");
        assertTrue(reopened.getResolutionNotes().contains("jamming again"), "Reopen reason must be recorded");
    }

    // TEST 13
    private static void test13_filterTickets() {
        TicketService service = createIsolatedService();
        service.createTicket("T1", "Desc", TicketCategory.TECHNICAL, TicketPriority.LOW, "Anil", null);
        service.createTicket("T2", "Desc", TicketCategory.HARDWARE, TicketPriority.HIGH, "Sunil", null);
        service.createTicket("T3", "Desc", TicketCategory.TECHNICAL, TicketPriority.CRITICAL, "Anil", null);

        List<Ticket> byCategory = service.filterTickets(null, null, TicketCategory.TECHNICAL, null);
        assertTrue(byCategory.size() == 2, "Should find 2 TECHNICAL tickets");

        List<Ticket> byPriority = service.filterTickets(null, TicketPriority.CRITICAL, null, null);
        assertTrue(byPriority.size() == 1, "Should find 1 CRITICAL ticket");

        List<Ticket> byCreator = service.filterTickets(null, null, null, "anil");
        assertTrue(byCreator.size() == 2, "Should find 2 tickets created by Anil");
    }

    // TEST 14
    private static void test14_dashboardStatistics() throws Exception {
        TicketService service = createIsolatedService();
        Ticket t1 = service.createTicket("T1", "D1", TicketCategory.TECHNICAL, TicketPriority.CRITICAL, "U1", null);
        Ticket t2 = service.createTicket("T2", "D2", TicketCategory.HARDWARE, TicketPriority.HIGH, "U2", null);
        Ticket t3 = service.createTicket("T3", "D3", TicketCategory.OTHER, TicketPriority.LOW, "U3", null);

        service.changeStatus(t2.getTicketId(), TicketStatus.IN_PROGRESS);
        service.resolveTicket(t3.getTicketId(), "Completed");

        DashboardStats stats = service.getDashboardStatistics();
        assertTrue(stats.getTotalTickets() == 3, "Total tickets should be 3");
        assertTrue(stats.getOpenCount() == 1, "Open count should be 1");
        assertTrue(stats.getInProgressCount() == 1, "In-progress count should be 1");
        assertTrue(stats.getResolvedCount() == 1, "Resolved count should be 1");
        assertTrue(stats.getClosedCount() == 0, "Closed count should be 0");
        assertTrue(stats.getHighPriorityCount() == 1, "High priority count should be 1");
        assertTrue(stats.getCriticalPriorityCount() == 1, "Critical priority count should be 1");
    }

    // TEST 15
    private static void test15_dataPersistence() throws Exception {
        String testFile = "data/test_persistence_" + System.currentTimeMillis() + ".csv";
        TicketRepository repo1 = new TicketRepository(testFile);
        TicketIdGenerator idGen1 = new TicketIdGenerator();
        TicketService service1 = new TicketService(repo1, idGen1);

        Ticket t1 = service1.createTicket("Persist Test", "Multiline\nDescription with, commas and \"quotes\"",
                TicketCategory.DELIVERY_OPERATIONS, TicketPriority.HIGH, "Ops Hub", "Rahul");
        String originalId = t1.getTicketId();
        service1.saveData();

        // Simulate application restart with a new TicketService and TicketIdGenerator
        TicketRepository repo2 = new TicketRepository(testFile);
        TicketIdGenerator idGen2 = new TicketIdGenerator();
        TicketService service2 = new TicketService(repo2, idGen2);

        Ticket loaded = service2.getTicketById(originalId);
        assertTrue(loaded != null, "Ticket must be restored from disk");
        assertTrue(loaded.getTitle().equals("Persist Test"), "Title must match");
        assertTrue(loaded.getDescription().contains("commas and \"quotes\""), "Quoted and comma-separated text must be preserved");

        // Verify ID sequence continuity: next ID must increment from highest stored ID
        Ticket t2 = service2.createTicket("Next Ticket", "Should have incremented ID", TicketCategory.OTHER, TicketPriority.LOW, "User", null);
        assertTrue(!t2.getTicketId().equals(originalId), "Next ID must be different and incremented");

        // Cleanup
        Files.deleteIfExists(Paths.get(testFile));
    }

    // TEST 16
    private static void test16_inputValidationResilience() {
        // Simulate user entering invalid letters, blank lines, out-of-range numbers, then valid "3"
        String simulatedInput = "abc\n\n-1\n99\n3\n";
        Scanner scanner = new Scanner(new ByteArrayInputStream(simulatedInput.getBytes(StandardCharsets.UTF_8)));
        InputUtil inputUtil = new InputUtil(scanner);

        int result = inputUtil.readInt("Select option (1-5): ", 1, 5);
        assertTrue(result == 3, "InputUtil should smoothly reprompt and eventually return valid input 3");
    }

    // TEST 17
    private static void test17_safePersistenceOnLoadError() throws Exception {
        String testFile = "data/test_corrupt_" + System.currentTimeMillis() + ".csv";
        TicketRepository corruptRepo = new TicketRepository(testFile) {
            @Override
            public java.util.Map<String, Ticket> loadAll() {
                // Simulate I/O corruption during load
                java.util.Map<String, Ticket> map = super.loadAll();
                setLastLoadHadError(true);
                return map;
            }
        };

        // Trigger load which marks error
        corruptRepo.loadAll();
        assertTrue(corruptRepo.hadLoadError(), "hadLoadError should be true");

        // Attempting to save empty list after a corrupted load must be refused
        boolean saveResult = corruptRepo.saveAll(Collections.emptyList());
        assertTrue(!saveResult, "Repository must refuse to overwrite storage with empty collection after severe load error");

        Files.deleteIfExists(Paths.get(testFile));
    }

    private static void assertTrue(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError("Assertion Failed: " + message);
        }
    }
}
