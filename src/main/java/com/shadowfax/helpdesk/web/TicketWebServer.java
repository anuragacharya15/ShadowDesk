package com.shadowfax.helpdesk.web;

import com.shadowfax.helpdesk.exception.InvalidTicketOperationException;
import com.shadowfax.helpdesk.exception.TicketNotFoundException;
import com.shadowfax.helpdesk.model.*;
import com.shadowfax.helpdesk.service.TicketService;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.awt.Desktop;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Embedded, zero-dependency Web Server providing a modern browser-based UI
 * for the Shadowfax Internal Helpdesk System using Java SE's built-in HttpServer.
 */
public class TicketWebServer {

    private final TicketService ticketService;
    private final int port;
    private HttpServer server;

    public TicketWebServer(TicketService ticketService, int port) {
        this.ticketService = ticketService;
        this.port = port;
    }

    public static void main(String[] args) {
        com.shadowfax.helpdesk.repository.TicketRepository repository = new com.shadowfax.helpdesk.repository.TicketRepository();
        com.shadowfax.helpdesk.util.TicketIdGenerator idGenerator = new com.shadowfax.helpdesk.util.TicketIdGenerator();
        TicketService ticketService = new TicketService(repository, idGenerator);

        int port = 8080;
        TicketWebServer webServer = new TicketWebServer(ticketService, port);
        try {
            webServer.start();
            System.out.println("============================================================");
            System.out.println("  SHADOWDESK - WEB DASHBOARD SERVER RUNNING");
            System.out.println("  Access URL: http://localhost:" + port);
            System.out.println("  Press Ctrl+C in this console to stop the server.");
            System.out.println("============================================================");

            // Try to automatically open default browser
            try {
                if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                    Desktop.getDesktop().browse(new URI("http://localhost:" + port));
                }
            } catch (Exception ignored) {}

        } catch (IOException e) {
            System.err.println("[ERROR] Failed to launch web server on port " + port + ": " + e.getMessage());
        }
    }

    public synchronized void start() throws IOException {
        server = HttpServer.create(new InetSocketAddress(port), 0);
        server.createContext("/", new StaticPageHandler());
        server.createContext("/api/tickets", new TicketsApiHandler());
        server.createContext("/api/tickets/resolve", new ResolveTicketHandler());
        server.createContext("/api/tickets/close", new CloseTicketHandler());
        server.createContext("/api/tickets/reopen", new ReopenTicketHandler());
        server.createContext("/api/tickets/status", new ChangeStatusHandler());
        server.createContext("/api/stats", new StatsApiHandler());
        server.createContext("/api/save", new SaveApiHandler());
        server.setExecutor(null); // Default executor
        server.start();
    }

    public synchronized void stop() {
        if (server != null) {
            server.stop(0);
        }
    }

    // -------------------------------------------------------------
    // Handlers
    // -------------------------------------------------------------

    private class StaticPageHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
                return;
            }
            String html = buildDashboardHtml();
            sendResponse(exchange, 200, html, "text/html; charset=UTF-8");
        }
    }

    private class TicketsApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String method = exchange.getRequestMethod();
            if ("GET".equalsIgnoreCase(method)) {
                List<Ticket> tickets = ticketService.getAllTickets();
                StringBuilder json = new StringBuilder("[");
                for (int i = 0; i < tickets.size(); i++) {
                    json.append(ticketToJson(tickets.get(i)));
                    if (i < tickets.size() - 1) json.append(",");
                }
                json.append("]");
                sendResponse(exchange, 200, json.toString(), "application/json; charset=UTF-8");
            } else if ("POST".equalsIgnoreCase(method)) {
                Map<String, String> params = parseRequestBody(exchange);
                String title = params.get("title");
                String description = params.get("description");
                String categoryStr = params.get("category");
                String priorityStr = params.get("priority");
                String createdBy = params.get("createdBy");
                String assignedTo = params.get("assignedTo");

                try {
                    TicketCategory cat = TicketCategory.valueOf(categoryStr);
                    TicketPriority prio = TicketPriority.valueOf(priorityStr);
                    Ticket created = ticketService.createTicket(title, description, cat, prio, createdBy, assignedTo);
                    ticketService.saveData(); // Auto save
                    sendResponse(exchange, 201, "{\"success\":true,\"ticket\":" + ticketToJson(created) + "}", "application/json; charset=UTF-8");
                } catch (Exception e) {
                    sendResponse(exchange, 400, "{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}", "application/json; charset=UTF-8");
                }
            } else {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
            }
        }
    }

    private class ResolveTicketHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
                return;
            }
            Map<String, String> params = parseRequestBody(exchange);
            String ticketId = params.get("ticketId");
            String notes = params.get("resolutionNotes");

            try {
                Ticket t = ticketService.resolveTicket(ticketId, notes);
                ticketService.saveData();
                sendResponse(exchange, 200, "{\"success\":true,\"ticket\":" + ticketToJson(t) + "}", "application/json; charset=UTF-8");
            } catch (TicketNotFoundException | InvalidTicketOperationException | IllegalArgumentException e) {
                sendResponse(exchange, 400, "{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}", "application/json; charset=UTF-8");
            }
        }
    }

    private class CloseTicketHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
                return;
            }
            Map<String, String> params = parseRequestBody(exchange);
            String ticketId = params.get("ticketId");

            try {
                Ticket t = ticketService.closeTicket(ticketId);
                ticketService.saveData();
                sendResponse(exchange, 200, "{\"success\":true,\"ticket\":" + ticketToJson(t) + "}", "application/json; charset=UTF-8");
            } catch (TicketNotFoundException | InvalidTicketOperationException e) {
                sendResponse(exchange, 400, "{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}", "application/json; charset=UTF-8");
            }
        }
    }

    private class ReopenTicketHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
                return;
            }
            Map<String, String> params = parseRequestBody(exchange);
            String ticketId = params.get("ticketId");
            String reason = params.get("reason");

            try {
                Ticket t = ticketService.reopenTicket(ticketId, reason);
                ticketService.saveData();
                sendResponse(exchange, 200, "{\"success\":true,\"ticket\":" + ticketToJson(t) + "}", "application/json; charset=UTF-8");
            } catch (TicketNotFoundException | InvalidTicketOperationException e) {
                sendResponse(exchange, 400, "{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}", "application/json; charset=UTF-8");
            }
        }
    }

    private class ChangeStatusHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
                return;
            }
            Map<String, String> params = parseRequestBody(exchange);
            String ticketId = params.get("ticketId");
            String targetStatus = params.get("status");

            try {
                TicketStatus st = TicketStatus.valueOf(targetStatus);
                Ticket t = ticketService.changeStatus(ticketId, st);
                ticketService.saveData();
                sendResponse(exchange, 200, "{\"success\":true,\"ticket\":" + ticketToJson(t) + "}", "application/json; charset=UTF-8");
            } catch (Exception e) {
                sendResponse(exchange, 400, "{\"success\":false,\"error\":\"" + escapeJson(e.getMessage()) + "\"}", "application/json; charset=UTF-8");
            }
        }
    }

    private class StatsApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"GET".equalsIgnoreCase(exchange.getRequestMethod())) {
                sendResponse(exchange, 405, "Method Not Allowed", "text/plain");
                return;
            }
            DashboardStats s = ticketService.getDashboardStatistics();
            String json = String.format(
                    "{\"total\":%d,\"open\":%d,\"inProgress\":%d,\"resolved\":%d,\"closed\":%d,\"high\":%d,\"critical\":%d}",
                    s.getTotalTickets(), s.getOpenCount(), s.getInProgressCount(),
                    s.getResolvedCount(), s.getClosedCount(), s.getHighPriorityCount(), s.getCriticalPriorityCount()
            );
            sendResponse(exchange, 200, json, "application/json; charset=UTF-8");
        }
    }

    private class SaveApiHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            boolean ok = ticketService.saveData();
            sendResponse(exchange, 200, "{\"success\":" + ok + "}", "application/json; charset=UTF-8");
        }
    }

    // -------------------------------------------------------------
    // Helper Methods
    // -------------------------------------------------------------

    private void sendResponse(HttpExchange exchange, int statusCode, String responseText, String contentType) throws IOException {
        byte[] bytes = responseText.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private Map<String, String> parseRequestBody(HttpExchange exchange) throws IOException {
        Map<String, String> result = new HashMap<>();
        InputStream is = exchange.getRequestBody();
        ByteArrayOutputStream buffer = new ByteArrayOutputStream();
        byte[] data = new byte[1024];
        int nRead;
        while ((nRead = is.read(data, 0, data.length)) != -1) {
            buffer.write(data, 0, nRead);
        }
        buffer.flush();
        String body = new String(buffer.toByteArray(), StandardCharsets.UTF_8).trim();

        if (body.startsWith("{") && body.endsWith("}")) {
            // Primitive JSON parser for simple flat key-value pairs
            String inner = body.substring(1, body.length() - 1);
            String[] pairs = inner.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");
            for (String pair : pairs) {
                String[] kv = pair.split(":", 2);
                if (kv.length == 2) {
                    String k = cleanJsonString(kv[0]);
                    String v = cleanJsonString(kv[1]);
                    result.put(k, v);
                }
            }
        } else {
            // Form URL-encoded
            String[] pairs = body.split("&");
            for (String pair : pairs) {
                String[] kv = pair.split("=", 2);
                if (kv.length == 2) {
                    String k = URLDecoder.decode(kv[0], "UTF-8");
                    String v = URLDecoder.decode(kv[1], "UTF-8");
                    result.put(k, v);
                }
            }
        }
        return result;
    }

    private String cleanJsonString(String val) {
        String trimmed = val.trim();
        if (trimmed.startsWith("\"") && trimmed.endsWith("\"")) {
            trimmed = trimmed.substring(1, trimmed.length() - 1);
        }
        return trimmed.replace("\\\"", "\"").replace("\\\\", "\\");
    }

    private String ticketToJson(Ticket t) {
        return String.format(
                "{\"ticketId\":\"%s\",\"title\":\"%s\",\"description\":\"%s\",\"category\":\"%s\",\"categoryDisplay\":\"%s\",\"priority\":\"%s\",\"priorityDisplay\":\"%s\",\"status\":\"%s\",\"statusDisplay\":\"%s\",\"createdBy\":\"%s\",\"assignedTo\":\"%s\",\"resolutionNotes\":\"%s\",\"createdAt\":\"%s\",\"updatedAt\":\"%s\"}",
                escapeJson(t.getTicketId()),
                escapeJson(t.getTitle()),
                escapeJson(t.getDescription()),
                t.getCategory().name(),
                escapeJson(t.getCategory().getDisplayName()),
                t.getPriority().name(),
                escapeJson(t.getPriority().getDisplayName()),
                t.getStatus().name(),
                escapeJson(t.getStatus().getDisplayName()),
                escapeJson(t.getCreatedBy()),
                escapeJson(t.getAssignedTo()),
                escapeJson(t.getResolutionNotes()),
                escapeJson(t.getFormattedCreatedAt()),
                escapeJson(t.getFormattedUpdatedAt())
        );
    }

    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    /**
     * Builds the complete responsive Shadowfax HTML/CSS/JS dashboard UI.
     */
    private String buildDashboardHtml() {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "  <title>ShadowDesk – Internal Helpdesk & Ticket Management</title>\n" +
                "  <style>\n" +
                "    :root {\n" +
                "      --sf-orange: #FF5A00;\n" +
                "      --sf-orange-hover: #E04E00;\n" +
                "      --sf-dark: #0F172A;\n" +
                "      --sf-navy: #1E293B;\n" +
                "      --sf-bg: #F8FAFC;\n" +
                "      --sf-border: #E2E8F0;\n" +
                "      --sf-text: #1E293B;\n" +
                "      --sf-muted: #64748B;\n" +
                "      --badge-open-bg: #E0F2FE; --badge-open-text: #0369A1;\n" +
                "      --badge-prog-bg: #FEF3C7; --badge-prog-text: #B45309;\n" +
                "      --badge-res-bg: #DCFCE7; --badge-res-text: #15803D;\n" +
                "      --badge-close-bg: #F1F5F9; --badge-close-text: #475569;\n" +
                "      --badge-crit-bg: #FEE2E2; --badge-crit-text: #B91C1C;\n" +
                "      --badge-high-bg: #FFEDD5; --badge-high-text: #C2410C;\n" +
                "    }\n" +
                "    * { box-sizing: border-box; margin: 0; padding: 0; font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; }\n" +
                "    body { background-color: var(--sf-bg); color: var(--sf-text); min-height: 100vh; }\n" +
                "    \n" +
                "    /* Header */\n" +
                "    header {\n" +
                "      background-color: var(--sf-dark);\n" +
                "      color: white;\n" +
                "      padding: 1rem 2rem;\n" +
                "      display: flex;\n" +
                "      align-items: center;\n" +
                "      justify-content: space-between;\n" +
                "      box-shadow: 0 4px 6px -1px rgba(0, 0, 0, 0.1);\n" +
                "    }\n" +
                "    .logo-group { display: flex; align-items: center; gap: 0.75rem; }\n" +
                "    .logo-badge { background: var(--sf-orange); color: white; font-weight: 800; font-size: 1.2rem; padding: 0.35rem 0.7rem; border-radius: 8px; letter-spacing: 0.5px; }\n" +
                "    .logo-title { font-size: 1.25rem; font-weight: 700; letter-spacing: -0.2px; }\n" +
                "    .logo-sub { color: #94A3B8; font-size: 0.85rem; margin-left: 0.5rem; }\n" +
                "    .header-actions { display: flex; gap: 0.75rem; }\n" +
                "    \n" +
                "    /* Buttons */\n" +
                "    .btn { display: inline-flex; align-items: center; gap: 0.4rem; padding: 0.55rem 1.1rem; border-radius: 6px; font-weight: 600; font-size: 0.875rem; cursor: pointer; border: none; transition: all 0.15s ease; }\n" +
                "    .btn-primary { background: var(--sf-orange); color: white; }\n" +
                "    .btn-primary:hover { background: var(--sf-orange-hover); transform: translateY(-1px); }\n" +
                "    .btn-secondary { background: white; color: var(--sf-text); border: 1px solid var(--sf-border); }\n" +
                "    .btn-secondary:hover { background: #F1F5F9; }\n" +
                "    .btn-outline-dark { background: transparent; color: #CBD5E1; border: 1px solid #475569; }\n" +
                "    .btn-outline-dark:hover { background: rgba(255,255,255,0.1); color: white; }\n" +
                "    .btn-sm { padding: 0.35rem 0.7rem; font-size: 0.8rem; border-radius: 4px; }\n" +
                "    .btn-success { background: #16A34A; color: white; }\n" +
                "    .btn-success:hover { background: #15803D; }\n" +
                "    \n" +
                "    /* Container */\n" +
                "    .container { max-width: 1350px; margin: 1.5rem auto; padding: 0 1.5rem; }\n" +
                "    \n" +
                "    /* Stats Grid */\n" +
                "    .stats-grid { display: grid; grid-template-columns: repeat(auto-fit, minmax(180px, 1fr)); gap: 1rem; margin-bottom: 1.5rem; }\n" +
                "    .stat-card { background: white; border: 1px solid var(--sf-border); border-radius: 8px; padding: 1.2rem; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }\n" +
                "    .stat-label { font-size: 0.8rem; text-transform: uppercase; font-weight: 700; color: var(--sf-muted); letter-spacing: 0.5px; margin-bottom: 0.5rem; }\n" +
                "    .stat-value { font-size: 1.8rem; font-weight: 800; color: var(--sf-text); }\n" +
                "    .stat-card.open .stat-value { color: #0284C7; }\n" +
                "    .stat-card.in-prog .stat-value { color: #D97706; }\n" +
                "    .stat-card.resolved .stat-value { color: #16A34A; }\n" +
                "    .stat-card.critical .stat-value { color: #DC2626; }\n" +
                "    \n" +
                "    /* Filter & Controls Bar */\n" +
                "    .controls-card { background: white; border: 1px solid var(--sf-border); border-radius: 8px; padding: 1rem 1.25rem; margin-bottom: 1.25rem; display: flex; flex-wrap: wrap; gap: 1rem; align-items: center; justify-content: space-between; }\n" +
                "    .filters-group { display: flex; flex-wrap: wrap; gap: 0.75rem; align-items: center; }\n" +
                "    .search-box { position: relative; width: 260px; }\n" +
                "    .search-box input { width: 100%; padding: 0.5rem 0.75rem; border-radius: 6px; border: 1px solid var(--sf-border); font-size: 0.875rem; outline: none; }\n" +
                "    .search-box input:focus { border-color: var(--sf-orange); box-shadow: 0 0 0 2px rgba(255,90,0,0.15); }\n" +
                "    select { padding: 0.5rem 0.75rem; border-radius: 6px; border: 1px solid var(--sf-border); font-size: 0.875rem; background: white; outline: none; }\n" +
                "    select:focus { border-color: var(--sf-orange); }\n" +
                "    \n" +
                "    /* Tickets Table */\n" +
                "    .table-card { background: white; border: 1px solid var(--sf-border); border-radius: 8px; overflow: hidden; box-shadow: 0 1px 3px rgba(0,0,0,0.05); }\n" +
                "    table { width: 100%; border-collapse: collapse; text-align: left; font-size: 0.875rem; }\n" +
                "    thead { background: #F1F5F9; border-bottom: 1px solid var(--sf-border); }\n" +
                "    th { padding: 0.85rem 1rem; font-weight: 700; color: #475569; font-size: 0.8rem; text-transform: uppercase; letter-spacing: 0.5px; }\n" +
                "    td { padding: 0.95rem 1rem; border-bottom: 1px solid var(--sf-border); vertical-align: middle; }\n" +
                "    tbody tr:hover { background-color: #F8FAFC; }\n" +
                "    .ticket-id { font-weight: 700; color: var(--sf-orange); font-family: monospace; font-size: 0.95rem; }\n" +
                "    .ticket-title { font-weight: 600; color: var(--sf-text); margin-bottom: 0.2rem; }\n" +
                "    .ticket-desc-preview { font-size: 0.8rem; color: var(--sf-muted); max-width: 320px; white-space: nowrap; overflow: hidden; text-overflow: ellipsis; }\n" +
                "    \n" +
                "    /* Badges */\n" +
                "    .badge { display: inline-block; padding: 0.25rem 0.6rem; border-radius: 9999px; font-size: 0.75rem; font-weight: 700; }\n" +
                "    .badge-OPEN { background: var(--badge-open-bg); color: var(--badge-open-text); }\n" +
                "    .badge-IN_PROGRESS { background: var(--badge-prog-bg); color: var(--badge-prog-text); }\n" +
                "    .badge-RESOLVED { background: var(--badge-res-bg); color: var(--badge-res-text); }\n" +
                "    .badge-CLOSED { background: var(--badge-close-bg); color: var(--badge-close-text); }\n" +
                "    .badge-CRITICAL { background: var(--badge-crit-bg); color: var(--badge-crit-text); }\n" +
                "    .badge-HIGH { background: var(--badge-high-bg); color: var(--badge-high-text); }\n" +
                "    .badge-MEDIUM { background: #FEF9C3; color: #854D0E; }\n" +
                "    .badge-LOW { background: #F1F5F9; color: #475569; }\n" +
                "    \n" +
                "    /* Action Group */\n" +
                "    .action-group { display: flex; gap: 0.4rem; }\n" +
                "    \n" +
                "    /* Modals */\n" +
                "    .modal-backdrop { display: none; position: fixed; top: 0; left: 0; width: 100%; height: 100%; background: rgba(15, 23, 42, 0.6); z-index: 1000; align-items: center; justify-content: center; backdrop-filter: blur(2px); }\n" +
                "    .modal-backdrop.active { display: flex; }\n" +
                "    .modal { background: white; border-radius: 10px; width: 100%; max-width: 600px; max-height: 90vh; overflow-y: auto; padding: 1.75rem; box-shadow: 0 20px 25px -5px rgba(0, 0, 0, 0.1); }\n" +
                "    .modal-header { display: flex; justify-content: space-between; align-items: center; border-bottom: 1px solid var(--sf-border); padding-bottom: 0.85rem; margin-bottom: 1.25rem; }\n" +
                "    .modal-title { font-size: 1.2rem; font-weight: 700; color: var(--sf-dark); }\n" +
                "    .modal-close { background: none; border: none; font-size: 1.5rem; color: var(--sf-muted); cursor: pointer; }\n" +
                "    .form-group { margin-bottom: 1rem; }\n" +
                "    .form-group label { display: block; font-size: 0.85rem; font-weight: 600; color: #334155; margin-bottom: 0.35rem; }\n" +
                "    .form-control { width: 100%; padding: 0.6rem 0.8rem; border-radius: 6px; border: 1px solid var(--sf-border); font-size: 0.9rem; }\n" +
                "    .form-control:focus { outline: none; border-color: var(--sf-orange); }\n" +
                "    textarea.form-control { min-height: 80px; resize: vertical; }\n" +
                "    .form-row { display: grid; grid-template-columns: 1fr 1fr; gap: 1rem; }\n" +
                "    .modal-footer { display: flex; justify-content: flex-end; gap: 0.75rem; border-top: 1px solid var(--sf-border); padding-top: 1rem; margin-top: 1.25rem; }\n" +
                "    \n" +
                "    /* Toast */\n" +
                "    #toast { position: fixed; bottom: 20px; right: 20px; background: #0F172A; color: white; padding: 0.8rem 1.25rem; border-radius: 8px; font-weight: 600; font-size: 0.9rem; z-index: 2000; box-shadow: 0 10px 15px -3px rgba(0,0,0,0.1); display: none; }\n" +
                "    #toast.success { background: #16A34A; }\n" +
                "    #toast.error { background: #DC2626; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "\n" +
                "  <!-- Header -->\n" +
                "  <header>\n" +
                "    <div class=\"logo-group\">\n" +
                "      <div class=\"logo-badge\">SD</div>\n" +
                "      <div>\n" +
                "        <span class=\"logo-title\">SHADOWDESK</span>\n" +
                "        <span class=\"logo-sub\">| Internal Helpdesk System</span>\n" +
                "      </div>\n" +
                "    </div>\n" +
                "    <div class=\"header-actions\">\n" +
                "      <button class=\"btn btn-outline-dark\" onclick=\"saveData()\">💾 Save to Disk</button>\n" +
                "      <button class=\"btn btn-outline-dark\" onclick=\"loadData()\">🔄 Refresh</button>\n" +
                "      <button class=\"btn btn-primary\" onclick=\"openCreateModal()\">➕ Create Ticket</button>\n" +
                "    </div>\n" +
                "  </header>\n" +
                "\n" +
                "  <div class=\"container\">\n" +
                "    <!-- Metrics Dashboard -->\n" +
                "    <div class=\"stats-grid\">\n" +
                "      <div class=\"stat-card\">\n" +
                "        <div class=\"stat-label\">Total Logged</div>\n" +
                "        <div class=\"stat-value\" id=\"statTotal\">0</div>\n" +
                "      </div>\n" +
                "      <div class=\"stat-card open\">\n" +
                "        <div class=\"stat-label\">Open Tickets</div>\n" +
                "        <div class=\"stat-value\" id=\"statOpen\">0</div>\n" +
                "      </div>\n" +
                "      <div class=\"stat-card in-prog\">\n" +
                "        <div class=\"stat-label\">In Progress</div>\n" +
                "        <div class=\"stat-value\" id=\"statProg\">0</div>\n" +
                "      </div>\n" +
                "      <div class=\"stat-card resolved\">\n" +
                "        <div class=\"stat-label\">Resolved</div>\n" +
                "        <div class=\"stat-value\" id=\"statRes\">0</div>\n" +
                "      </div>\n" +
                "      <div class=\"stat-card critical\">\n" +
                "        <div class=\"stat-label\">Critical Priority</div>\n" +
                "        <div class=\"stat-value\" id=\"statCrit\">0</div>\n" +
                "      </div>\n" +
                "    </div>\n" +
                "\n" +
                "    <!-- Filters Bar -->\n" +
                "    <div class=\"controls-card\">\n" +
                "      <div class=\"filters-group\">\n" +
                "        <div class=\"search-box\">\n" +
                "          <input type=\"text\" id=\"searchInput\" placeholder=\"🔍 Search ID, title, creator...\" oninput=\"renderTickets()\">\n" +
                "        </div>\n" +
                "        <select id=\"filterStatus\" onchange=\"renderTickets()\">\n" +
                "          <option value=\"ALL\">All Statuses</option>\n" +
                "          <option value=\"OPEN\">Open</option>\n" +
                "          <option value=\"IN_PROGRESS\">In Progress</option>\n" +
                "          <option value=\"RESOLVED\">Resolved</option>\n" +
                "          <option value=\"CLOSED\">Closed</option>\n" +
                "        </select>\n" +
                "        <select id=\"filterPriority\" onchange=\"renderTickets()\">\n" +
                "          <option value=\"ALL\">All Priorities</option>\n" +
                "          <option value=\"LOW\">Low</option>\n" +
                "          <option value=\"MEDIUM\">Medium</option>\n" +
                "          <option value=\"HIGH\">High</option>\n" +
                "          <option value=\"CRITICAL\">Critical</option>\n" +
                "        </select>\n" +
                "        <select id=\"filterCategory\" onchange=\"renderTickets()\">\n" +
                "          <option value=\"ALL\">All Categories</option>\n" +
                "          <option value=\"TECHNICAL\">Technical Support</option>\n" +
                "          <option value=\"DELIVERY_OPERATIONS\">Delivery & Logistics Operations</option>\n" +
                "          <option value=\"ACCOUNT_ACCESS\">Account & Access</option>\n" +
                "          <option value=\"HARDWARE\">Hardware & Device</option>\n" +
                "          <option value=\"OTHER\">Other / General</option>\n" +
                "        </select>\n" +
                "      </div>\n" +
                "      <div style=\"font-size: 0.85rem; color: var(--sf-muted);\" id=\"ticketCountLabel\">Showing 0 tickets</div>\n" +
                "    </div>\n" +
                "\n" +
                "    <!-- Tickets Table -->\n" +
                "    <div class=\"table-card\">\n" +
                "      <table>\n" +
                "        <thead>\n" +
                "          <tr>\n" +
                "            <th>ID</th>\n" +
                "            <th>Title & Details</th>\n" +
                "            <th>Category</th>\n" +
                "            <th>Priority</th>\n" +
                "            <th>Status</th>\n" +
                "            <th>Requester / Assignee</th>\n" +
                "            <th>Created</th>\n" +
                "            <th>Actions</th>\n" +
                "          </tr>\n" +
                "        </thead>\n" +
                "        <tbody id=\"ticketTableBody\">\n" +
                "          <tr><td colspan=\"8\" style=\"text-align: center; color: var(--sf-muted); padding: 2rem;\">Loading tickets...</td></tr>\n" +
                "        </tbody>\n" +
                "      </table>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "\n" +
                "  <!-- Create Ticket Modal -->\n" +
                "  <div class=\"modal-backdrop\" id=\"createModal\">\n" +
                "    <div class=\"modal\">\n" +
                "      <div class=\"modal-header\">\n" +
                "        <h3 class=\"modal-title\">Create New Support Ticket</h3>\n" +
                "        <button class=\"modal-close\" onclick=\"closeModal('createModal')\">&times;</button>\n" +
                "      </div>\n" +
                "      <form onsubmit=\"submitCreateTicket(event)\">\n" +
                "        <div class=\"form-group\">\n" +
                "          <label>Ticket Title / Summary *</label>\n" +
                "          <input type=\"text\" id=\"createTitle\" class=\"form-control\" placeholder=\"e.g. Warehouse scanner laser failure\" required>\n" +
                "        </div>\n" +
                "        <div class=\"form-row\">\n" +
                "          <div class=\"form-group\">\n" +
                "            <label>Category *</label>\n" +
                "            <select id=\"createCategory\" class=\"form-control\" required>\n" +
                "              <option value=\"TECHNICAL\">Technical Support</option>\n" +
                "              <option value=\"DELIVERY_OPERATIONS\" selected>Delivery & Logistics Operations</option>\n" +
                "              <option value=\"ACCOUNT_ACCESS\">Account & Access Management</option>\n" +
                "              <option value=\"HARDWARE\">Hardware & Device Support</option>\n" +
                "              <option value=\"OTHER\">Other / General</option>\n" +
                "            </select>\n" +
                "          </div>\n" +
                "          <div class=\"form-group\">\n" +
                "            <label>Priority *</label>\n" +
                "            <select id=\"createPriority\" class=\"form-control\" required>\n" +
                "              <option value=\"LOW\">Low</option>\n" +
                "              <option value=\"MEDIUM\" selected>Medium</option>\n" +
                "              <option value=\"HIGH\">High</option>\n" +
                "              <option value=\"CRITICAL\">Critical</option>\n" +
                "            </select>\n" +
                "          </div>\n" +
                "        </div>\n" +
                "        <div class=\"form-row\">\n" +
                "          <div class=\"form-group\">\n" +
                "            <label>Requester Name / Employee ID *</label>\n" +
                "            <input type=\"text\" id=\"createCreator\" class=\"form-control\" placeholder=\"e.g. Rahul (Fleet Lead)\" required>\n" +
                "          </div>\n" +
                "          <div class=\"form-group\">\n" +
                "            <label>Assigned Support Agent</label>\n" +
                "            <select id=\"createAssignee\" class=\"form-control\">\n" +
                "              <option value=\"Unassigned\">Unassigned</option>\n" +
                "              <option value=\"Rahul Sharma\">Rahul Sharma (IT Support)</option>\n" +
                "              <option value=\"Priya Patel\">Priya Patel (Operations Lead)</option>\n" +
                "              <option value=\"Arun Kumar\">Arun Kumar (Logistics Helpdesk)</option>\n" +
                "            </select>\n" +
                "          </div>\n" +
                "        </div>\n" +
                "        <div class=\"form-group\">\n" +
                "          <label>Detailed Description *</label>\n" +
                "          <textarea id=\"createDesc\" class=\"form-control\" placeholder=\"Detailed symptoms, affected hub, steps to reproduce...\" required></textarea>\n" +
                "        </div>\n" +
                "        <div class=\"modal-footer\">\n" +
                "          <button type=\"button\" class=\"btn btn-secondary\" onclick=\"closeModal('createModal')\">Cancel</button>\n" +
                "          <button type=\"submit\" class=\"btn btn-primary\">Create Ticket</button>\n" +
                "        </div>\n" +
                "      </form>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "\n" +
                "  <!-- Detail / Audit Modal -->\n" +
                "  <div class=\"modal-backdrop\" id=\"detailModal\">\n" +
                "    <div class=\"modal\">\n" +
                "      <div class=\"modal-header\">\n" +
                "        <h3 class=\"modal-title\" id=\"detailModalTitle\">Ticket Details</h3>\n" +
                "        <button class=\"modal-close\" onclick=\"closeModal('detailModal')\">&times;</button>\n" +
                "      </div>\n" +
                "      <div id=\"detailModalBody\" style=\"line-height: 1.6; font-size: 0.9rem;\"></div>\n" +
                "      <div class=\"modal-footer\">\n" +
                "        <button class=\"btn btn-secondary\" onclick=\"closeModal('detailModal')\">Close</button>\n" +
                "      </div>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "\n" +
                "  <!-- Resolve Modal -->\n" +
                "  <div class=\"modal-backdrop\" id=\"resolveModal\">\n" +
                "    <div class=\"modal\">\n" +
                "      <div class=\"modal-header\">\n" +
                "        <h3 class=\"modal-title\">Resolve Ticket <span id=\"resolveTicketIdLabel\"></span></h3>\n" +
                "        <button class=\"modal-close\" onclick=\"closeModal('resolveModal')\">&times;</button>\n" +
                "      </div>\n" +
                "      <form onsubmit=\"submitResolveTicket(event)\">\n" +
                "        <input type=\"hidden\" id=\"resolveTicketId\">\n" +
                "        <div class=\"form-group\">\n" +
                "          <label>Resolution Notes * (Required)</label>\n" +
                "          <textarea id=\"resolveNotes\" class=\"form-control\" placeholder=\"Explain resolution, root cause, and verify actions taken...\" required></textarea>\n" +
                "        </div>\n" +
                "        <div class=\"modal-footer\">\n" +
                "          <button type=\"button\" class=\"btn btn-secondary\" onclick=\"closeModal('resolveModal')\">Cancel</button>\n" +
                "          <button type=\"submit\" class=\"btn btn-success\">Confirm Resolution</button>\n" +
                "        </div>\n" +
                "      </form>\n" +
                "    </div>\n" +
                "  </div>\n" +
                "\n" +
                "  <div id=\"toast\"></div>\n" +
                "\n" +
                "  <script>\n" +
                "    let allTickets = [];\n" +
                "\n" +
                "    async function loadData() {\n" +
                "      try {\n" +
                "        const [ticketsRes, statsRes] = await Promise.all([\n" +
                "          fetch('/api/tickets'),\n" +
                "          fetch('/api/stats')\n" +
                "        ]);\n" +
                "        allTickets = await ticketsRes.json();\n" +
                "        const stats = await statsRes.json();\n" +
                "        \n" +
                "        document.getElementById('statTotal').innerText = stats.total;\n" +
                "        document.getElementById('statOpen').innerText = stats.open;\n" +
                "        document.getElementById('statProg').innerText = stats.inProgress;\n" +
                "        document.getElementById('statRes').innerText = stats.resolved;\n" +
                "        document.getElementById('statCrit').innerText = stats.critical;\n" +
                "        renderTickets();\n" +
                "      } catch (e) {\n" +
                "        showToast('Error loading data: ' + e, 'error');\n" +
                "      }\n" +
                "    }\n" +
                "\n" +
                "    function renderTickets() {\n" +
                "      const q = document.getElementById('searchInput').value.toLowerCase().trim();\n" +
                "      const stFilter = document.getElementById('filterStatus').value;\n" +
                "      const prioFilter = document.getElementById('filterPriority').value;\n" +
                "      const catFilter = document.getElementById('filterCategory').value;\n" +
                "\n" +
                "      const filtered = allTickets.filter(t => {\n" +
                "        if (stFilter !== 'ALL' && t.status !== stFilter) return false;\n" +
                "        if (prioFilter !== 'ALL' && t.priority !== prioFilter) return false;\n" +
                "        if (catFilter !== 'ALL' && t.category !== catFilter) return false;\n" +
                "        if (q) {\n" +
                "          const match = t.ticketId.toLowerCase().includes(q) ||\n" +
                "                        t.title.toLowerCase().includes(q) ||\n" +
                "                        t.description.toLowerCase().includes(q) ||\n" +
                "                        t.createdBy.toLowerCase().includes(q);\n" +
                "          if (!match) return false;\n" +
                "        }\n" +
                "        return true;\n" +
                "      });\n" +
                "\n" +
                "      document.getElementById('ticketCountLabel').innerText = `Showing ${filtered.length} of ${allTickets.length} tickets`;\n" +
                "      const tbody = document.getElementById('ticketTableBody');\n" +
                "      if (filtered.length === 0) {\n" +
                "        tbody.innerHTML = '<tr><td colspan=\"8\" style=\"text-align: center; color: var(--sf-muted); padding: 2.5rem;\">No tickets matched the current filter.</td></tr>';\n" +
                "        return;\n" +
                "      }\n" +
                "\n" +
                "      tbody.innerHTML = filtered.map(t => {\n" +
                "        let actionButtons = `<button class=\"btn btn-secondary btn-sm\" onclick=\"viewDetails('${t.ticketId}')\">Details</button>`;\n" +
                "        \n" +
                "        if (t.status === 'OPEN') {\n" +
                "          actionButtons += ` <button class=\"btn btn-secondary btn-sm\" onclick=\"changeStatus('${t.ticketId}', 'IN_PROGRESS')\">Start</button>`;\n" +
                "          actionButtons += ` <button class=\"btn btn-success btn-sm\" onclick=\"promptResolve('${t.ticketId}')\">Resolve</button>`;\n" +
                "        } else if (t.status === 'IN_PROGRESS') {\n" +
                "          actionButtons += ` <button class=\"btn btn-success btn-sm\" onclick=\"promptResolve('${t.ticketId}')\">Resolve</button>`;\n" +
                "        } else if (t.status === 'RESOLVED') {\n" +
                "          actionButtons += ` <button class=\"btn btn-primary btn-sm\" onclick=\"closeTicket('${t.ticketId}')\">Close</button>`;\n" +
                "          actionButtons += ` <button class=\"btn btn-secondary btn-sm\" onclick=\"reopenTicket('${t.ticketId}')\">Reopen</button>`;\n" +
                "        } else if (t.status === 'CLOSED') {\n" +
                "          actionButtons += ` <button class=\"btn btn-secondary btn-sm\" onclick=\"reopenTicket('${t.ticketId}')\">Reopen</button>`;\n" +
                "        }\n" +
                "\n" +
                "        return `<tr>\n" +
                "          <td class=\"ticket-id\">${t.ticketId}</td>\n" +
                "          <td>\n" +
                "            <div class=\"ticket-title\">${escapeHtml(t.title)}</div>\n" +
                "            <div class=\"ticket-desc-preview\">${escapeHtml(t.description)}</div>\n" +
                "          </td>\n" +
                "          <td>${escapeHtml(t.categoryDisplay)}</td>\n" +
                "          <td><span class=\"badge badge-${t.priority}\">${t.priorityDisplay}</span></td>\n" +
                "          <td><span class=\"badge badge-${t.status}\">${t.statusDisplay}</span></td>\n" +
                "          <td>\n" +
                "            <div><strong>Req:</strong> ${escapeHtml(t.createdBy)}</div>\n" +
                "            <div style=\"color: var(--sf-muted); font-size: 0.8rem;\"><strong>Assigned:</strong> ${escapeHtml(t.assignedTo)}</div>\n" +
                "          </td>\n" +
                "          <td style=\"font-size: 0.8rem; color: var(--sf-muted);\">${t.createdAt}</td>\n" +
                "          <td><div class=\"action-group\">${actionButtons}</div></td>\n" +
                "        </tr>`;\n" +
                "      }).join('');\n" +
                "    }\n" +
                "\n" +
                "    function openCreateModal() { document.getElementById('createModal').classList.add('active'); }\n" +
                "    function closeModal(id) { document.getElementById(id).classList.remove('active'); }\n" +
                "\n" +
                "    async function submitCreateTicket(e) {\n" +
                "      e.preventDefault();\n" +
                "      const payload = {\n" +
                "        title: document.getElementById('createTitle').value,\n" +
                "        category: document.getElementById('createCategory').value,\n" +
                "        priority: document.getElementById('createPriority').value,\n" +
                "        createdBy: document.getElementById('createCreator').value,\n" +
                "        assignedTo: document.getElementById('createAssignee').value,\n" +
                "        description: document.getElementById('createDesc').value\n" +
                "      };\n" +
                "      try {\n" +
                "        const res = await fetch('/api/tickets', {\n" +
                "          method: 'POST',\n" +
                "          headers: { 'Content-Type': 'application/json' },\n" +
                "          body: JSON.stringify(payload)\n" +
                "        });\n" +
                "        const data = await res.json();\n" +
                "        if (data.success) {\n" +
                "          showToast(`Created ticket ${data.ticket.ticketId}`, 'success');\n" +
                "          closeModal('createModal');\n" +
                "          e.target.reset();\n" +
                "          loadData();\n" +
                "        } else {\n" +
                "          showToast(data.error, 'error');\n" +
                "        }\n" +
                "      } catch (err) {\n" +
                "        showToast('Submission error: ' + err, 'error');\n" +
                "      }\n" +
                "    }\n" +
                "\n" +
                "    function promptResolve(ticketId) {\n" +
                "      document.getElementById('resolveTicketId').value = ticketId;\n" +
                "      document.getElementById('resolveTicketIdLabel').innerText = ticketId;\n" +
                "      document.getElementById('resolveNotes').value = '';\n" +
                "      document.getElementById('resolveModal').classList.add('active');\n" +
                "    }\n" +
                "\n" +
                "    async function submitResolveTicket(e) {\n" +
                "      e.preventDefault();\n" +
                "      const ticketId = document.getElementById('resolveTicketId').value;\n" +
                "      const notes = document.getElementById('resolveNotes').value;\n" +
                "      try {\n" +
                "        const res = await fetch('/api/tickets/resolve', {\n" +
                "          method: 'POST',\n" +
                "          headers: { 'Content-Type': 'application/json' },\n" +
                "          body: JSON.stringify({ ticketId, resolutionNotes: notes })\n" +
                "        });\n" +
                "        const data = await res.json();\n" +
                "        if (data.success) {\n" +
                "          showToast(`Ticket ${ticketId} resolved`, 'success');\n" +
                "          closeModal('resolveModal');\n" +
                "          loadData();\n" +
                "        } else {\n" +
                "          showToast(data.error, 'error');\n" +
                "        }\n" +
                "      } catch (err) {\n" +
                "        showToast('Error resolving: ' + err, 'error');\n" +
                "      }\n" +
                "    }\n" +
                "\n" +
                "    async function closeTicket(ticketId) {\n" +
                "      if (!confirm(`Are you sure you want to CLOSE ticket ${ticketId}?`)) return;\n" +
                "      try {\n" +
                "        const res = await fetch('/api/tickets/close', {\n" +
                "          method: 'POST',\n" +
                "          headers: { 'Content-Type': 'application/json' },\n" +
                "          body: JSON.stringify({ ticketId })\n" +
                "        });\n" +
                "        const data = await res.json();\n" +
                "        if (data.success) {\n" +
                "          showToast(`Ticket ${ticketId} officially closed`, 'success');\n" +
                "          loadData();\n" +
                "        } else {\n" +
                "          showToast(data.error, 'error');\n" +
                "        }\n" +
                "      } catch (err) {\n" +
                "        showToast('Error closing: ' + err, 'error');\n" +
                "      }\n" +
                "    }\n" +
                "\n" +
                "    async function reopenTicket(ticketId) {\n" +
                "      const reason = prompt(`Reason for reopening ticket ${ticketId}:`, 'User requested further review');\n" +
                "      if (reason === null) return;\n" +
                "      try {\n" +
                "        const res = await fetch('/api/tickets/reopen', {\n" +
                "          method: 'POST',\n" +
                "          headers: { 'Content-Type': 'application/json' },\n" +
                "          body: JSON.stringify({ ticketId, reason })\n" +
                "        });\n" +
                "        const data = await res.json();\n" +
                "        if (data.success) {\n" +
                "          showToast(`Ticket ${ticketId} reopened to OPEN`, 'success');\n" +
                "          loadData();\n" +
                "        } else {\n" +
                "          showToast(data.error, 'error');\n" +
                "        }\n" +
                "      } catch (err) {\n" +
                "        showToast('Error reopening: ' + err, 'error');\n" +
                "      }\n" +
                "    }\n" +
                "\n" +
                "    async function changeStatus(ticketId, status) {\n" +
                "      try {\n" +
                "        const res = await fetch('/api/tickets/status', {\n" +
                "          method: 'POST',\n" +
                "          headers: { 'Content-Type': 'application/json' },\n" +
                "          body: JSON.stringify({ ticketId, status })\n" +
                "        });\n" +
                "        const data = await res.json();\n" +
                "        if (data.success) {\n" +
                "          showToast(`Status changed to ${status}`, 'success');\n" +
                "          loadData();\n" +
                "        } else {\n" +
                "          showToast(data.error, 'error');\n" +
                "        }\n" +
                "      } catch (err) {\n" +
                "        showToast('Error changing status: ' + err, 'error');\n" +
                "      }\n" +
                "    }\n" +
                "\n" +
                "    function viewDetails(ticketId) {\n" +
                "      const t = allTickets.find(item => item.ticketId === ticketId);\n" +
                "      if (!t) return;\n" +
                "      document.getElementById('detailModalTitle').innerText = `${t.ticketId} - ${t.title}`;\n" +
                "      document.getElementById('detailModalBody').innerHTML = `\n" +
                "        <div style=\"display:grid; grid-template-columns:1fr 1fr; gap:0.5rem; margin-bottom:1rem; background:#F8FAFC; padding:0.75rem; border-radius:6px; border:1px solid #E2E8F0;\">\n" +
                "          <div><strong>Status:</strong> <span class=\"badge badge-${t.status}\">${t.statusDisplay}</span></div>\n" +
                "          <div><strong>Priority:</strong> <span class=\"badge badge-${t.priority}\">${t.priorityDisplay}</span></div>\n" +
                "          <div><strong>Category:</strong> ${escapeHtml(t.categoryDisplay)}</div>\n" +
                "          <div><strong>Requester:</strong> ${escapeHtml(t.createdBy)}</div>\n" +
                "          <div><strong>Assigned To:</strong> ${escapeHtml(t.assignedTo)}</div>\n" +
                "          <div><strong>Created At:</strong> ${t.createdAt}</div>\n" +
                "          <div style=\"grid-column:1/-1\"><strong>Last Updated:</strong> ${t.updatedAt}</div>\n" +
                "        </div>\n" +
                "        <div style=\"margin-bottom:1rem;\">\n" +
                "          <strong>Description:</strong>\n" +
                "          <p style=\"margin-top:0.35rem; color:#334155; white-space:pre-wrap; background:#FFF; border:1px solid #E2E8F0; padding:0.6rem; border-radius:6px;\">${escapeHtml(t.description)}</p>\n" +
                "        </div>\n" +
                "        <div>\n" +
                "          <strong>Resolution / Audit Notes:</strong>\n" +
                "          <p style=\"margin-top:0.35rem; color:#334155; white-space:pre-wrap; background:#FFF; border:1px solid #E2E8F0; padding:0.6rem; border-radius:6px;\">${t.resolutionNotes ? escapeHtml(t.resolutionNotes) : '<em>(None recorded yet)</em>'}</p>\n" +
                "        </div>\n" +
                "      `;\n" +
                "      document.getElementById('detailModal').classList.add('active');\n" +
                "    }\n" +
                "\n" +
                "    async function saveData() {\n" +
                "      try {\n" +
                "        const res = await fetch('/api/save', { method: 'POST' });\n" +
                "        const data = await res.json();\n" +
                "        if (data.success) showToast('Data successfully saved to tickets.csv', 'success');\n" +
                "        else showToast('Failed to save data', 'error');\n" +
                "      } catch (err) {\n" +
                "        showToast('Save error: ' + err, 'error');\n" +
                "      }\n" +
                "    }\n" +
                "\n" +
                "    function showToast(msg, type='info') {\n" +
                "      const el = document.getElementById('toast');\n" +
                "      el.innerText = msg;\n" +
                "      el.className = type;\n" +
                "      el.style.display = 'block';\n" +
                "      setTimeout(() => el.style.display = 'none', 3500);\n" +
                "    }\n" +
                "\n" +
                "    function escapeHtml(s) {\n" +
                "      if (!s) return '';\n" +
                "      return s.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;').replace(/\"/g, '&quot;');\n" +
                "    }\n" +
                "\n" +
                "    // Initial load and periodic poll every 5s\n" +
                "    loadData();\n" +
                "    setInterval(loadData, 5000);\n" +
                "  </script>\n" +
                "</body>\n" +
                "</html>";
    }
}
