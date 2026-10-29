package com.shadowfax.helpdesk.repository;

import com.shadowfax.helpdesk.model.Ticket;
import com.shadowfax.helpdesk.model.TicketCategory;
import com.shadowfax.helpdesk.model.TicketPriority;
import com.shadowfax.helpdesk.model.TicketStatus;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Repository responsible for persisting and loading tickets to/from standard CSV storage.
 * Follows RFC-4180 CSV specifications (handles commas, quotes, and multiline text).
 * Implements atomic writes and load failure protection to prevent data corruption.
 */
public class TicketRepository {

    private static final String CSV_HEADER = "ticketId,title,description,category,priority,status,createdBy,assignedTo,resolutionNotes,createdAt,updatedAt";
    private final Path filePath;
    private boolean lastLoadHadError = false;

    public TicketRepository() {
        this(Paths.get("data", "tickets.csv").toString());
    }

    public TicketRepository(String customFilePath) {
        this.filePath = Paths.get(customFilePath);
    }

    public Path getFilePath() {
        return filePath;
    }

    public boolean hadLoadError() {
        return lastLoadHadError;
    }

    public void setLastLoadHadError(boolean lastLoadHadError) {
        this.lastLoadHadError = lastLoadHadError;
    }

    /**
     * Loads all tickets from disk into a Map keyed by ticketId.
     * Preserves insertion order using LinkedHashMap.
     */
    public Map<String, Ticket> loadAll() {
        Map<String, Ticket> tickets = new LinkedHashMap<>();
        lastLoadHadError = false;

        if (!Files.exists(filePath)) {
            // File does not exist yet; normal for a fresh installation
            return tickets;
        }

        try (BufferedReader reader = Files.newBufferedReader(filePath, StandardCharsets.UTF_8)) {
            List<List<String>> records = parseCsv(reader);

            if (records.isEmpty()) {
                return tickets;
            }

            // Skip header if present
            int startIndex = 0;
            List<String> firstRow = records.get(0);
            if (!firstRow.isEmpty() && "ticketId".equalsIgnoreCase(firstRow.get(0).trim())) {
                startIndex = 1;
            }

            for (int i = startIndex; i < records.size(); i++) {
                List<String> fields = records.get(i);
                if (fields.isEmpty() || (fields.size() == 1 && fields.get(0).trim().isEmpty())) {
                    continue; // Skip blank lines
                }

                if (fields.size() < 11) {
                    System.err.println("[WARNING] Skipping malformed record on row " + (i + 1) + ": insufficient columns (" + fields.size() + "/11).");
                    continue;
                }

                try {
                    String ticketId = fields.get(0).trim();
                    String title = fields.get(1);
                    String description = fields.get(2);
                    TicketCategory category = TicketCategory.valueOf(fields.get(3).trim());
                    TicketPriority priority = TicketPriority.valueOf(fields.get(4).trim());
                    TicketStatus status = TicketStatus.valueOf(fields.get(5).trim());
                    String createdBy = fields.get(6).trim();
                    String assignedTo = fields.get(7).trim();
                    String resolutionNotes = fields.get(8);
                    LocalDateTime createdAt = LocalDateTime.parse(fields.get(9).trim(), Ticket.STORAGE_FORMATTER);
                    LocalDateTime updatedAt = LocalDateTime.parse(fields.get(10).trim(), Ticket.STORAGE_FORMATTER);

                    Ticket ticket = new Ticket(ticketId, title, description, category, priority, status,
                            createdBy, assignedTo, resolutionNotes, createdAt, updatedAt);

                    tickets.put(ticketId, ticket);
                } catch (Exception parseEx) {
                    System.err.println("[WARNING] Malformed ticket row skipped (row " + (i + 1) + "): " + parseEx.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to read ticket data from " + filePath + ": " + e.getMessage());
            lastLoadHadError = true;
        }

        return tickets;
    }

    /**
     * Persists all tickets to disk atomically to prevent partial/corrupt writes.
     */
    public synchronized boolean saveAll(Collection<Ticket> tickets) {
        if (lastLoadHadError && (tickets == null || tickets.isEmpty())) {
            System.err.println("[REFUSED] Aborting save: Previous load encountered severe errors and tickets collection is empty. Preventing data loss.");
            return false;
        }

        try {
            Path parentDir = filePath.getParent();
            if (parentDir != null && !Files.exists(parentDir)) {
                Files.createDirectories(parentDir);
            }

            Path tempFile = filePath.resolveSibling(filePath.getFileName().toString() + ".tmp");

            try (BufferedWriter writer = Files.newBufferedWriter(tempFile, StandardCharsets.UTF_8)) {
                writer.write(CSV_HEADER);
                writer.newLine();

                if (tickets != null) {
                    for (Ticket ticket : tickets) {
                        writer.write(formatTicketCsv(ticket));
                        writer.newLine();
                    }
                }
            }

            // Atomic file replacement
            Files.move(tempFile, filePath, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
            return true;
        } catch (IOException e) {
            System.err.println("[ERROR] Failed to persist tickets to " + filePath + ": " + e.getMessage());
            return false;
        }
    }

    /**
     * Formats a Ticket object into an RFC-4180 compliant CSV line.
     */
    private String formatTicketCsv(Ticket t) {
        StringBuilder sb = new StringBuilder();
        appendCsvField(sb, t.getTicketId());
        sb.append(",");
        appendCsvField(sb, t.getTitle());
        sb.append(",");
        appendCsvField(sb, t.getDescription());
        sb.append(",");
        appendCsvField(sb, t.getCategory().name());
        sb.append(",");
        appendCsvField(sb, t.getPriority().name());
        sb.append(",");
        appendCsvField(sb, t.getStatus().name());
        sb.append(",");
        appendCsvField(sb, t.getCreatedBy());
        sb.append(",");
        appendCsvField(sb, t.getAssignedTo());
        sb.append(",");
        appendCsvField(sb, t.getResolutionNotes());
        sb.append(",");
        appendCsvField(sb, t.getCreatedAt().format(Ticket.STORAGE_FORMATTER));
        sb.append(",");
        appendCsvField(sb, t.getUpdatedAt().format(Ticket.STORAGE_FORMATTER));
        return sb.toString();
    }

    /**
     * Escapes and appends a single field according to RFC-4180 rules.
     */
    private void appendCsvField(StringBuilder sb, String value) {
        if (value == null) {
            return;
        }
        boolean mustQuote = value.contains(",") || value.contains("\"") || value.contains("\n") || value.contains("\r");
        if (mustQuote) {
            sb.append("\"");
            sb.append(value.replace("\"", "\"\""));
            sb.append("\"");
        } else {
            sb.append(value);
        }
    }

    /**
     * Standard RFC-4180 compliant CSV reader supporting multiline text and quoted delimiters.
     */
    private List<List<String>> parseCsv(Reader reader) throws IOException {
        List<List<String>> records = new ArrayList<>();
        List<String> currentRecord = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();

        boolean inQuotes = false;
        int prevChar = -1;
        int ch;

        while ((ch = reader.read()) != -1) {
            if (ch == '\"') {
                if (inQuotes) {
                    // Peek if the next character is also a quote (escaped quote "")
                    reader.mark(1);
                    int next = reader.read();
                    if (next == '\"') {
                        currentField.append('\"');
                    } else {
                        inQuotes = false;
                        reader.reset();
                    }
                } else {
                    inQuotes = true;
                }
            } else if (ch == ',' && !inQuotes) {
                currentRecord.add(currentField.toString());
                currentField.setLength(0);
            } else if ((ch == '\n' || ch == '\r') && !inQuotes) {
                if (ch == '\r') {
                    reader.mark(1);
                    int next = reader.read();
                    if (next != '\n') {
                        reader.reset();
                    }
                }
                currentRecord.add(currentField.toString());
                records.add(new ArrayList<>(currentRecord));
                currentRecord.clear();
                currentField.setLength(0);
            } else {
                currentField.append((char) ch);
            }
            prevChar = ch;
        }

        // Add last field and record if present
        if (currentField.length() > 0 || !currentRecord.isEmpty() || prevChar == ',') {
            currentRecord.add(currentField.toString());
            records.add(currentRecord);
        }

        return records;
    }
}
