package com.shadowfax.helpdesk.model;

/**
 * Data holder for dynamically calculated helpdesk metrics.
 */
public class DashboardStats {
    private final int totalTickets;
    private final int openCount;
    private final int inProgressCount;
    private final int resolvedCount;
    private final int closedCount;
    private final int highPriorityCount;
    private final int criticalPriorityCount;

    public DashboardStats(int totalTickets, int openCount, int inProgressCount,
                          int resolvedCount, int closedCount,
                          int highPriorityCount, int criticalPriorityCount) {
        this.totalTickets = totalTickets;
        this.openCount = openCount;
        this.inProgressCount = inProgressCount;
        this.resolvedCount = resolvedCount;
        this.closedCount = closedCount;
        this.highPriorityCount = highPriorityCount;
        this.criticalPriorityCount = criticalPriorityCount;
    }

    public int getTotalTickets() {
        return totalTickets;
    }

    public int getOpenCount() {
        return openCount;
    }

    public int getInProgressCount() {
        return inProgressCount;
    }

    public int getResolvedCount() {
        return resolvedCount;
    }

    public int getClosedCount() {
        return closedCount;
    }

    public int getHighPriorityCount() {
        return highPriorityCount;
    }

    public int getCriticalPriorityCount() {
        return criticalPriorityCount;
    }
}
