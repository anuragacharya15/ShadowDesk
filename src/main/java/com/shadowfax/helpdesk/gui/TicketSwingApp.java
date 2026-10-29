package com.shadowfax.helpdesk.gui;

import com.shadowfax.helpdesk.exception.InvalidTicketOperationException;
import com.shadowfax.helpdesk.exception.TicketNotFoundException;
import com.shadowfax.helpdesk.model.*;
import com.shadowfax.helpdesk.repository.TicketRepository;
import com.shadowfax.helpdesk.service.TicketService;
import com.shadowfax.helpdesk.util.TicketIdGenerator;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * Native Desktop GUI for Shadowfax Internal Helpdesk System built using Java Swing.
 * Zero external dependencies, pure Core Java SE.
 */
public class TicketSwingApp extends JFrame {

    private static final Color SF_ORANGE = new Color(255, 90, 0);
    private static final Color SF_DARK = new Color(15, 23, 42);
    private static final Color SF_BG = new Color(248, 250, 252);
    private static final Color STATUS_OPEN = new Color(2, 132, 199);
    private static final Color STATUS_IN_PROG = new Color(217, 119, 6);
    private static final Color STATUS_RESOLVED = new Color(22, 163, 74);
    private static final Color STATUS_CLOSED = new Color(100, 116, 139);
    private static final Color PRIORITY_CRITICAL = new Color(220, 38, 38);

    private final TicketService ticketService;

    // UI Components
    private JTable ticketTable;
    private DefaultTableModel tableModel;
    private JLabel lblTotal;
    private JLabel lblOpen;
    private JLabel lblProg;
    private JLabel lblRes;
    private JLabel lblCrit;

    private JTextField txtSearch;
    private JComboBox<String> cmbStatus;
    private JComboBox<String> cmbPriority;
    private JComboBox<String> cmbCategory;

    public TicketSwingApp(TicketService ticketService) {
        this.ticketService = ticketService;
        initUI();
        refreshData();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            TicketRepository repository = new TicketRepository();
            TicketIdGenerator idGenerator = new TicketIdGenerator();
            TicketService ticketService = new TicketService(repository, idGenerator);

            TicketSwingApp app = new TicketSwingApp(ticketService);
            app.setVisible(true);
        });
    }

    private void initUI() {
        setTitle("Shadowfax Internal Helpdesk & Ticket Management System");
        setSize(1200, 750);
        setMinimumSize(new Dimension(950, 600));
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout(0, 0));

        // 1. Top Header
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(SF_DARK);
        headerPanel.setBorder(new EmptyBorder(14, 20, 14, 20));

        JPanel brandPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        brandPanel.setOpaque(false);

        JLabel logoBadge = new JLabel(" SF ");
        logoBadge.setOpaque(true);
        logoBadge.setBackground(SF_ORANGE);
        logoBadge.setForeground(Color.WHITE);
        logoBadge.setFont(new Font("SansSerif", Font.BOLD, 16));
        logoBadge.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JLabel titleLabel = new JLabel("SHADOWFAX HELPDESK SYSTEM");
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 17));

        JLabel subLabel = new JLabel(" | Internal Support Operations");
        subLabel.setForeground(new Color(148, 163, 184));
        subLabel.setFont(new Font("SansSerif", Font.PLAIN, 13));

        brandPanel.add(logoBadge);
        brandPanel.add(titleLabel);
        brandPanel.add(subLabel);

        JPanel headerButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        headerButtons.setOpaque(false);

        JButton btnSaveDisk = createStyledButton("💾 Save Data", new Color(51, 65, 85), Color.WHITE);
        btnSaveDisk.addActionListener(e -> {
            boolean ok = ticketService.saveData();
            if (ok) {
                JOptionPane.showMessageDialog(this, "All tickets successfully saved to data/tickets.csv", "Saved", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Failed to save tickets to disk.", "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        JButton btnNewTicket = createStyledButton("➕ New Ticket", SF_ORANGE, Color.WHITE);
        btnNewTicket.addActionListener(e -> openCreateTicketDialog());

        headerButtons.add(btnSaveDisk);
        headerButtons.add(btnNewTicket);

        headerPanel.add(brandPanel, BorderLayout.WEST);
        headerPanel.add(headerButtons, BorderLayout.EAST);
        add(headerPanel, BorderLayout.NORTH);

        // 2. Center Content Area (Stats + Filters + Table)
        JPanel centerPanel = new JPanel(new BorderLayout(0, 12));
        centerPanel.setBackground(SF_BG);
        centerPanel.setBorder(new EmptyBorder(14, 20, 14, 20));

        // Stats Dashboard Strip
        JPanel statsStrip = new JPanel(new GridLayout(1, 5, 12, 0));
        statsStrip.setOpaque(false);

        lblTotal = new JLabel("0", SwingConstants.CENTER);
        lblOpen = new JLabel("0", SwingConstants.CENTER);
        lblProg = new JLabel("0", SwingConstants.CENTER);
        lblRes = new JLabel("0", SwingConstants.CENTER);
        lblCrit = new JLabel("0", SwingConstants.CENTER);

        statsStrip.add(createStatCard("TOTAL TICKETS", lblTotal, SF_DARK));
        statsStrip.add(createStatCard("OPEN", lblOpen, STATUS_OPEN));
        statsStrip.add(createStatCard("IN PROGRESS", lblProg, STATUS_IN_PROG));
        statsStrip.add(createStatCard("RESOLVED", lblRes, STATUS_RESOLVED));
        statsStrip.add(createStatCard("CRITICAL", lblCrit, PRIORITY_CRITICAL));

        // Controls Strip
        JPanel controlsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 8));
        controlsPanel.setBackground(Color.WHITE);
        controlsPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(6, 12, 6, 12)
        ));

        controlsPanel.add(new JLabel("Search:"));
        txtSearch = new JTextField(15);
        txtSearch.putClientProperty("JTextField.placeholderText", "ID, title, requester...");
        txtSearch.addActionListener(e -> filterTable());
        controlsPanel.add(txtSearch);

        controlsPanel.add(new JLabel("Status:"));
        cmbStatus = new JComboBox<>(new String[]{"All", "Open", "In Progress", "Resolved", "Closed"});
        cmbStatus.addActionListener(e -> filterTable());
        controlsPanel.add(cmbStatus);

        controlsPanel.add(new JLabel("Priority:"));
        cmbPriority = new JComboBox<>(new String[]{"All", "Low", "Medium", "High", "Critical"});
        cmbPriority.addActionListener(e -> filterTable());
        controlsPanel.add(cmbPriority);

        controlsPanel.add(new JLabel("Category:"));
        cmbCategory = new JComboBox<>(new String[]{"All", "Technical Support", "Delivery & Logistics Operations", "Account & Access Management", "Hardware & Device Support", "Other / General Inquiries"});
        cmbCategory.addActionListener(e -> filterTable());
        controlsPanel.add(cmbCategory);

        JButton btnFilterReset = new JButton("Reset");
        btnFilterReset.addActionListener(e -> {
            txtSearch.setText("");
            cmbStatus.setSelectedIndex(0);
            cmbPriority.setSelectedIndex(0);
            cmbCategory.setSelectedIndex(0);
            refreshData();
        });
        controlsPanel.add(btnFilterReset);

        JButton btnRefresh = new JButton("🔄 Refresh");
        btnRefresh.addActionListener(e -> refreshData());
        controlsPanel.add(btnRefresh);

        // Combine Stats and Controls in a top container
        JPanel topContainer = new JPanel(new BorderLayout(0, 10));
        topContainer.setOpaque(false);
        topContainer.add(statsStrip, BorderLayout.NORTH);
        topContainer.add(controlsPanel, BorderLayout.SOUTH);

        // 3. Table
        String[] columnNames = {"ID", "Title", "Category", "Priority", "Status", "Requester", "Assignee", "Created Date"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        ticketTable = new JTable(tableModel);
        ticketTable.setRowHeight(32);
        ticketTable.setFont(new Font("SansSerif", Font.PLAIN, 13));
        ticketTable.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));
        ticketTable.getTableHeader().setBackground(new Color(241, 245, 249));
        ticketTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        ticketTable.setShowGrid(true);
        ticketTable.setGridColor(new Color(241, 245, 249));

        // Column widths
        ticketTable.getColumnModel().getColumn(0).setPreferredWidth(80);
        ticketTable.getColumnModel().getColumn(1).setPreferredWidth(260);
        ticketTable.getColumnModel().getColumn(2).setPreferredWidth(170);
        ticketTable.getColumnModel().getColumn(3).setPreferredWidth(90);
        ticketTable.getColumnModel().getColumn(4).setPreferredWidth(100);
        ticketTable.getColumnModel().getColumn(5).setPreferredWidth(130);
        ticketTable.getColumnModel().getColumn(6).setPreferredWidth(130);
        ticketTable.getColumnModel().getColumn(7).setPreferredWidth(130);

        // Custom renderers for status and priority
        ticketTable.getColumnModel().getColumn(4).setCellRenderer(new StatusCellRenderer());
        ticketTable.getColumnModel().getColumn(3).setCellRenderer(new PriorityCellRenderer());

        JScrollPane scrollPane = new JScrollPane(ticketTable);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(226, 232, 240)));

        centerPanel.add(topContainer, BorderLayout.NORTH);
        centerPanel.add(scrollPane, BorderLayout.CENTER);
        add(centerPanel, BorderLayout.CENTER);

        // 4. Bottom Action Bar
        JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        bottomBar.setBackground(Color.WHITE);
        bottomBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(226, 232, 240)));

        JButton btnDetails = new JButton("🔍 View Details");
        btnDetails.addActionListener(e -> viewSelectedTicketDetails());

        JButton btnStart = new JButton("⚡ Start Work");
        btnStart.addActionListener(e -> startWorkOnSelectedTicket());

        JButton btnResolve = createStyledButton("✔ Resolve Ticket", STATUS_RESOLVED, Color.WHITE);
        btnResolve.addActionListener(e -> resolveSelectedTicket());

        JButton btnClose = createStyledButton("🔒 Close Ticket", SF_DARK, Color.WHITE);
        btnClose.addActionListener(e -> closeSelectedTicket());

        JButton btnReopen = new JButton("🔄 Reopen");
        btnReopen.addActionListener(e -> reopenSelectedTicket());

        bottomBar.add(btnDetails);
        bottomBar.add(btnStart);
        bottomBar.add(btnResolve);
        bottomBar.add(btnClose);
        bottomBar.add(btnReopen);

        add(bottomBar, BorderLayout.SOUTH);
    }

    private JPanel createStatCard(String label, JLabel valueLabel, Color accentColor) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(226, 232, 240)),
                new EmptyBorder(10, 14, 10, 14)
        ));

        JLabel title = new JLabel(label);
        title.setFont(new Font("SansSerif", Font.BOLD, 10));
        title.setForeground(new Color(100, 116, 139));

        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        valueLabel.setForeground(accentColor);

        card.add(title, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private JButton createStyledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setBorder(BorderFactory.createEmptyBorder(6, 14, 6, 14));
        return btn;
    }

    // Refresh and Filter
    private void refreshData() {
        DashboardStats stats = ticketService.getDashboardStatistics();
        lblTotal.setText(String.valueOf(stats.getTotalTickets()));
        lblOpen.setText(String.valueOf(stats.getOpenCount()));
        lblProg.setText(String.valueOf(stats.getInProgressCount()));
        lblRes.setText(String.valueOf(stats.getResolvedCount()));
        lblCrit.setText(String.valueOf(stats.getCriticalPriorityCount()));

        filterTable();
    }

    private void filterTable() {
        tableModel.setRowCount(0);

        String query = txtSearch.getText().trim().toLowerCase();
        String selStatus = (String) cmbStatus.getSelectedItem();
        String selPriority = (String) cmbPriority.getSelectedItem();
        String selCategory = (String) cmbCategory.getSelectedItem();

        TicketStatus st = "All".equals(selStatus) ? null : TicketStatus.fromString(selStatus);
        TicketPriority prio = "All".equals(selPriority) ? null : TicketPriority.fromString(selPriority);
        TicketCategory cat = "All".equals(selCategory) ? null : TicketCategory.fromString(selCategory);

        List<Ticket> filtered = ticketService.filterTickets(st, prio, cat, null);

        for (Ticket t : filtered) {
            if (!query.isEmpty()) {
                boolean match = t.getTicketId().toLowerCase().contains(query) ||
                                t.getTitle().toLowerCase().contains(query) ||
                                t.getCreatedBy().toLowerCase().contains(query);
                if (!match) continue;
            }

            tableModel.addRow(new Object[]{
                    t.getTicketId(),
                    t.getTitle(),
                    t.getCategory().getDisplayName(),
                    t.getPriority().getDisplayName(),
                    t.getStatus().getDisplayName(),
                    t.getCreatedBy(),
                    t.getAssignedTo(),
                    t.getFormattedCreatedAt()
            });
        }
    }

    private Ticket getSelectedTicket() {
        int row = ticketTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a ticket from the table first.", "Selection Required", JOptionPane.WARNING_MESSAGE);
            return null;
        }
        String ticketId = (String) tableModel.getValueAt(row, 0);
        try {
            return ticketService.getTicketById(ticketId);
        } catch (TicketNotFoundException e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    // Action Handlers
    private void openCreateTicketDialog() {
        JDialog dialog = new JDialog(this, "Create New Support Ticket", true);
        dialog.setSize(520, 500);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridLayout(6, 2, 10, 12));
        form.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField txtTitle = new JTextField();
        JComboBox<TicketCategory> cbCat = new JComboBox<>(TicketCategory.values());
        JComboBox<TicketPriority> cbPrio = new JComboBox<>(TicketPriority.values());
        cbPrio.setSelectedItem(TicketPriority.MEDIUM);
        JTextField txtCreator = new JTextField();
        JComboBox<String> cbAssignee = new JComboBox<>(new String[]{"Unassigned", "Rahul Sharma", "Priya Patel", "Arun Kumar"});
        JTextArea txtDesc = new JTextArea(4, 20);
        txtDesc.setLineWrap(true);
        txtDesc.setWrapStyleWord(true);

        form.add(new JLabel("Title * :"));
        form.add(txtTitle);
        form.add(new JLabel("Category * :"));
        form.add(cbCat);
        form.add(new JLabel("Priority * :"));
        form.add(cbPrio);
        form.add(new JLabel("Requester Name * :"));
        form.add(txtCreator);
        form.add(new JLabel("Assignee :"));
        form.add(cbAssignee);
        form.add(new JLabel("Description * :"));
        form.add(new JScrollPane(txtDesc));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnSubmit = createStyledButton("Create Ticket", SF_ORANGE, Color.WHITE);
        JButton btnCancel = new JButton("Cancel");

        btnCancel.addActionListener(e -> dialog.dispose());
        btnSubmit.addActionListener(e -> {
            String title = txtTitle.getText().trim();
            String desc = txtDesc.getText().trim();
            String creator = txtCreator.getText().trim();

            if (title.isEmpty() || desc.isEmpty() || creator.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all mandatory fields (Title, Description, Requester).", "Validation Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                Ticket t = ticketService.createTicket(
                        title, desc,
                        (TicketCategory) cbCat.getSelectedItem(),
                        (TicketPriority) cbPrio.getSelectedItem(),
                        creator,
                        (String) cbAssignee.getSelectedItem()
                );
                ticketService.saveData();
                refreshData();
                dialog.dispose();
                JOptionPane.showMessageDialog(this, "Ticket created successfully!\nID: " + t.getTicketId(), "Success", JOptionPane.INFORMATION_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dialog, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        });

        buttonPanel.add(btnCancel);
        buttonPanel.add(btnSubmit);

        dialog.add(form, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void viewSelectedTicketDetails() {
        Ticket t = getSelectedTicket();
        if (t == null) return;

        JDialog dialog = new JDialog(this, "Ticket Details - " + t.getTicketId(), true);
        dialog.setSize(600, 520);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout());

        JTextArea area = new JTextArea();
        area.setEditable(false);
        area.setFont(new Font("Monospaced", Font.PLAIN, 12));
        area.setMargin(new Insets(15, 15, 15, 15));

        StringBuilder sb = new StringBuilder();
        sb.append("=================================================================\n");
        sb.append(String.format(" TICKET ID       : %s%n", t.getTicketId()));
        sb.append(String.format(" TITLE           : %s%n", t.getTitle()));
        sb.append(String.format(" STATUS          : %s%n", t.getStatus().getDisplayName()));
        sb.append(String.format(" PRIORITY        : %s%n", t.getPriority().getDisplayName()));
        sb.append(String.format(" CATEGORY        : %s%n", t.getCategory().getDisplayName()));
        sb.append(String.format(" REQUESTER       : %s%n", t.getCreatedBy()));
        sb.append(String.format(" ASSIGNED TO     : %s%n", t.getAssignedTo()));
        sb.append(String.format(" CREATED AT      : %s%n", t.getFormattedCreatedAt()));
        sb.append(String.format(" LAST UPDATED    : %s%n", t.getFormattedUpdatedAt()));
        sb.append("=================================================================\n");
        sb.append(" DESCRIPTION:\n");
        sb.append(" ").append(t.getDescription()).append("\n\n");
        sb.append(" RESOLUTION / AUDIT NOTES:\n");
        if (t.getResolutionNotes() != null && !t.getResolutionNotes().trim().isEmpty()) {
            sb.append(" ").append(t.getResolutionNotes()).append("\n");
        } else {
            sb.append(" (None recorded yet)\n");
        }
        sb.append("=================================================================\n");

        area.setText(sb.toString());

        JButton btnClose = new JButton("Close");
        btnClose.addActionListener(e -> dialog.dispose());
        JPanel p = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        p.add(btnClose);

        dialog.add(new JScrollPane(area), BorderLayout.CENTER);
        dialog.add(p, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void startWorkOnSelectedTicket() {
        Ticket t = getSelectedTicket();
        if (t == null) return;

        try {
            ticketService.changeStatus(t.getTicketId(), TicketStatus.IN_PROGRESS);
            ticketService.saveData();
            refreshData();
            JOptionPane.showMessageDialog(this, "Ticket " + t.getTicketId() + " moved to IN_PROGRESS.", "Updated", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot Start", JOptionPane.WARNING_MESSAGE);
        }
    }

    private void resolveSelectedTicket() {
        Ticket t = getSelectedTicket();
        if (t == null) return;

        String notes = JOptionPane.showInputDialog(this, "Enter resolution notes for " + t.getTicketId() + ":", "Resolve Ticket", JOptionPane.PLAIN_MESSAGE);
        if (notes == null) return; // Cancelled
        if (notes.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Resolution notes are required to resolve a ticket.", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        try {
            ticketService.resolveTicket(t.getTicketId(), notes.trim());
            ticketService.saveData();
            refreshData();
            JOptionPane.showMessageDialog(this, "Ticket " + t.getTicketId() + " has been RESOLVED.", "Resolved", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot Resolve", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void closeSelectedTicket() {
        Ticket t = getSelectedTicket();
        if (t == null) return;

        int confirm = JOptionPane.showConfirmDialog(this, "Are you sure you want to close ticket " + t.getTicketId() + "?", "Confirm Close", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            ticketService.closeTicket(t.getTicketId());
            ticketService.saveData();
            refreshData();
            JOptionPane.showMessageDialog(this, "Ticket " + t.getTicketId() + " is now officially CLOSED.", "Closed", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot Close", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void reopenSelectedTicket() {
        Ticket t = getSelectedTicket();
        if (t == null) return;

        String reason = JOptionPane.showInputDialog(this, "Reason for reopening " + t.getTicketId() + " (optional):", "Reopen Ticket", JOptionPane.PLAIN_MESSAGE);
        if (reason == null) return;

        try {
            ticketService.reopenTicket(t.getTicketId(), reason);
            ticketService.saveData();
            refreshData();
            JOptionPane.showMessageDialog(this, "Ticket " + t.getTicketId() + " has been reopened to OPEN.", "Reopened", JOptionPane.INFORMATION_MESSAGE);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage(), "Cannot Reopen", JOptionPane.ERROR_MESSAGE);
        }
    }

    // Custom Cell Renderers
    private static class StatusCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String st = String.valueOf(value);
            c.setFont(c.getFont().deriveFont(Font.BOLD));
            if (!isSelected) {
                if ("Open".equalsIgnoreCase(st)) c.setForeground(STATUS_OPEN);
                else if ("In Progress".equalsIgnoreCase(st)) c.setForeground(STATUS_IN_PROG);
                else if ("Resolved".equalsIgnoreCase(st)) c.setForeground(STATUS_RESOLVED);
                else if ("Closed".equalsIgnoreCase(st)) c.setForeground(STATUS_CLOSED);
            }
            return c;
        }
    }

    private static class PriorityCellRenderer extends DefaultTableCellRenderer {
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            JLabel c = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
            String prio = String.valueOf(value);
            if (!isSelected) {
                if ("Critical".equalsIgnoreCase(prio)) {
                    c.setForeground(PRIORITY_CRITICAL);
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else if ("High".equalsIgnoreCase(prio)) {
                    c.setForeground(new Color(194, 65, 12));
                    c.setFont(c.getFont().deriveFont(Font.BOLD));
                } else {
                    c.setForeground(Color.DARK_GRAY);
                }
            }
            return c;
        }
    }
}
