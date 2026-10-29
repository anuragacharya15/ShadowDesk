# Shadowfax Internal Helpdesk & Support Ticket Management System

A robust, console-based internal support ticket management system engineered in Core Java (Java 8+ compatible) designed for Shadowfax's operational, technical, and logistics helpdesk teams.

---

## 📌 Project Objective

In high-velocity logistics and delivery operations at Shadowfax, warehouse leads, fleet drivers, and customer support staff encounter issues ranging from app sync delays and delivery address errors to hardware scanner failures and account access requests.

This project provides an intuitive, reliable, and decoupled Core Java solution to:
- Log internal support tickets with unique IDs (`SF-1001`, `SF-1002`, ...).
- Track tickets through a strict business lifecycle: `OPEN` ➔ `IN_PROGRESS` ➔ `RESOLVED` ➔ `CLOSED`.
- Prevent duplicate IDs and persist ID sequence across restarts.
- Safely persist ticket data using standard RFC-4180 compliant CSV storage with atomic replacement.
- Offer dynamic dashboard analytics and multi-attribute search and filtering.
- Prevent application crashes through rigorous input validation and custom exception handling.

---

## 🚀 Key Features

1. **Interactive Console Menu**: Easy-to-navigate numerical menu with resilient input parsing that never crashes on blank lines, characters, or out-of-range numbers.
2. **Sequential Unique ID Generator**: Auto-generates identifiers like `SF-1001`, `SF-1002`. Recovers sequence from saved tickets upon restart to guarantee zero duplicates.
3. **Strict Status Lifecycle**:
   - `OPEN` ➔ `IN_PROGRESS` or `RESOLVED`
   - `IN_PROGRESS` ➔ `RESOLVED`
   - `RESOLVED` ➔ `CLOSED` or `OPEN` (reopen)
   - `CLOSED` ➔ `OPEN` (reopen)
   - Disallows illegal jumps (e.g., closing an open ticket or changing a closed ticket directly to in-progress).
4. **Resolution Notes & Audit History**: Resolving a ticket enforces non-empty resolution notes. Reopening retains previous resolution history with timestamps.
5. **Multi-Attribute Filtering**: Filter active tickets by Status, Priority, Category, or Creator/Requester.
6. **Live Dashboard & Metrics**: Real-time aggregation of total, open, in-progress, resolved, closed, high-priority, and critical-priority tickets.
7. **Crash-Resistant File Persistence**:
   - Uses standard Java I/O (`BufferedReader`/`BufferedWriter`, `java.nio.file.Files`).
   - Complies with RFC-4180 (handles commas, quotes, and multiline text).
   - Atomic saving (writes to `.tmp` file, then atomically renames).
   - Load safety: if a corrupted file is detected during load, the repository refuses to overwrite storage with an empty collection.
8. **Automated Test Suite**: Built-in 17-scenario unit and integration test suite executing without external test runners.

---

## 🛠️ Technology Stack

- **Language**: Java (Core Java SE)
- **Java Version**: Compatible with Java 8, 11, 17, and 21+
- **Build & Dependency Philosophy**: Zero external runtime dependencies. Plain Java & Maven-compatible layout (`pom.xml` included).
- **Storage**: Standard CSV via Java NIO (`java.nio.file`) and Java I/O.
- **Date/Time API**: Java 8 `java.time.LocalDateTime` and `java.time.format.DateTimeFormatter`.
- **Collections Framework**: `LinkedHashMap` (preserves insertion order), `ArrayList`, `Map`, `List`.
- **Concurrency / Thread-Safety**: `AtomicInteger` for ticket sequence tracking.

---

## 📂 Project Folder Structure

```
d:/new/
│
├── pom.xml                               # Standard Maven project configuration
├── run.bat                               # Windows Batch runner (compiles and runs Main)
├── run.ps1                               # PowerShell runner
├── test.bat                              # Batch script to execute the 17-scenario test suite
├── README.md                             # Comprehensive system documentation
│
├── data/
│   └── tickets.csv                       # Persisted ticket records (RFC-4180 CSV)
│
├── bin/                                  # Compiled .class bytecode
│
└── src/
    ├── main/
    │   └── java/
    │       └── com/
    │           └── shadowfax/
    │               └── helpdesk/
    │                   ├── Main.java     # Console UI loop & menu coordination
    │                   │
    │                   ├── model/
    │                   │   ├── Ticket.java            # Core ticket entity
    │                   │   ├── User.java              # User / Agent representation
    │                   │   ├── UserRole.java          # EMPLOYEE, SUPPORT_AGENT
    │                   │   ├── TicketCategory.java    # Categories enum
    │                   │   ├── TicketPriority.java    # Priorities enum
    │                   │   ├── TicketStatus.java      # Statuses enum
    │                   │   └── DashboardStats.java    # Dynamic metrics container
    │                   │
    │                   ├── service/
    │                   │   └── TicketService.java     # Business rules, lifecycle & search
    │                   │
    │                   ├── repository/
    │                   │   └── TicketRepository.java  # CSV file persistence & atomic writes
    │                   │
    │                   ├── util/
    │                   │   ├── InputUtil.java         # Safe scanner input reader
    │                   │   └── TicketIdGenerator.java # Unique SF-1001 ID sequence tracker
    │                   │
    │                   └── exception/
    │                       ├── TicketNotFoundException.java
    │                       └── InvalidTicketOperationException.java
    │
    └── test/
        └── java/
            └── com/
                └── shadowfax/
                    └── helpdesk/
                        └── TicketSystemTestSuite.java # 17 automated verification tests
```

---

## 💡 Core Java Concepts Demonstrated

| Concept | Implementation in this Project |
| :--- | :--- |
| **Object-Oriented Programming (OOP)** | Strong encapsulation (`Ticket` fields are private), explicit constructors, domain methods (`touch()`, `updateDetails()`), separation of concerns across Model-Service-Repository layers. |
| **Enums with Custom Fields & Methods** | `TicketCategory`, `TicketPriority`, `TicketStatus`, and `UserRole` contain display labels and static reverse-lookup methods. |
| **Collections Framework** | `LinkedHashMap<String, Ticket>` for $O(1)$ ID lookups while maintaining creation order; `ArrayList` for defensive copies and filtering. |
| **Modern Date/Time API** | `LocalDateTime` used for immutable timestamps (`createdAt`, `updatedAt`) formatted via `DateTimeFormatter`. |
| **Custom Exception Hierarchy** | Checked exceptions `TicketNotFoundException` and `InvalidTicketOperationException` enforce domain boundaries without generic unhandled exceptions. |
| **File I/O & Streaming** | Character-by-character RFC-4180 CSV parser supporting quoted commas and multi-line descriptions; `BufferedWriter` with atomic file swapping. |
| **Defensive Programming** | Validation of nulls, whitespace trimming, range checks, and preventing empty overwrites on load failure. |

---

## 🖥️ Three User Interfaces (Console, Web Dashboard, and Desktop GUI)

The system provides three distinct ways to interact with the helpdesk:

1. **Interactive Console UI**:
   - Ideal for command-line workflows, SSH sessions, and fast keyboard entry.
   - Run via `run.bat` or `java -cp bin com.shadowfax.helpdesk.Main`.
2. **Modern Web Dashboard (Browser-based)**:
   - Built on Java SE's zero-dependency `HttpServer` with an interactive, responsive HTML5/CSS3/JavaScript UI with official Shadowfax orange and dark navy styling.
   - Features real-time metric cards, live client-side search/filtering, creation modal, resolution modal with validation, and REST API.
   - Run via `run-web.bat` and open `http://localhost:8080` in Chrome/Edge/Firefox.
3. **Native Desktop Window (Java Swing GUI)**:
   - Built on pure Java SE `javax.swing.*` with a native look and feel, multi-column `JTable`, color-coded status badges, filter controls, and modal dialogs.
   - Run via `run-gui.bat` or `java -cp bin com.shadowfax.helpdesk.gui.TicketSwingApp`.

---

## ⚙️ How to Compile and Run

### 1. Launching the Web UI Dashboard (Browser)
- Double-click **`run-web.bat`** (or in PowerShell: `.\run-web.ps1`).
- Open your browser to **`http://localhost:8080`**.

### 2. Launching the Desktop GUI (Java Swing)
- Double-click **`run-gui.bat`** (or in PowerShell: `.\run-gui.ps1`).

### 3. Launching the Console UI
- Double-click **`run.bat`** (or in PowerShell: `.\run.ps1`).

### 4. Running the Automated Test Suite
- Double-click **`test.bat`**.

### Option 3: Opening in Modern IDEs

- **VS Code**: Open folder `d:/new`. The included `pom.xml` will be auto-detected by the Language Support for Java extension. Press `F5` to run or run `Main.java`.
- **IntelliJ IDEA**: Select **File ➔ Open ➔ `d:/new`**. Choose **Open as Project**. IntelliJ will recognize the Maven structure and allow one-click execution.
- **Eclipse / NetBeans**: Select **File ➔ Import ➔ Existing Maven Project**.

---

## 🔄 Ticket Status Workflow

```
       +---------------------------------------------+
       |                                             |
       v                                             |
  +---------+           +---------------+            |
  |  OPEN   | --------> |  IN_PROGRESS  |            |
  +---------+           +---------------+            |
       |                       |                     |
       | (Direct Resolution)   |                     |
       v                       v                     |
  +-------------------------------------+            |
  |              RESOLVED               |            |
  +-------------------------------------+            |
       |                                             |
       | (Formal Closing)                            | (Reopen)
       v                                             |
  +---------+                                        |
  | CLOSED  | ---------------------------------------+
  +---------+
```

### Business Rules:
1. Newly created tickets are always `OPEN`.
2. A ticket can be assigned and moved to `IN_PROGRESS`.
3. Resolving a ticket requires non-empty `resolutionNotes`.
4. Only `RESOLVED` tickets can be moved to `CLOSED`. Attempting to close an `OPEN` or `IN_PROGRESS` ticket throws an `InvalidTicketOperationException`.
5. Tickets in `RESOLVED` or `CLOSED` state can be reopened, moving them back to `OPEN` while preserving historical resolution notes.

---

## 📋 Input Validation Rules

| Input Field | Rule | Handling on Violation |
| :--- | :--- | :--- |
| **Menu Options** | Numbers between 0 and 11 | Displays friendly error; prompts again without terminating |
| **Ticket Title** | Must not be empty or whitespace | Rejected with prompt to enter valid title |
| **Description** | Must not be empty or whitespace | Rejected with prompt to enter valid description |
| **Category & Priority** | Must be selected from 1..N options | Displays numbered menu; rejects out-of-range choices |
| **Creator Name** | Cannot be empty | Loops until non-blank string is provided |
| **Resolution Notes** | Required when resolving | Mandatory input; rejects empty notes |
| **Ticket ID Lookup** | Case-insensitive (`sf-1001` or `SF-1001`) | Throws `TicketNotFoundException`; friendly message |

---

## 🧪 Testing and Verification

The project includes an automated test runner (`TicketSystemTestSuite.java`) verifying 17 real-world scenarios:

| # | Test Scenario | Expected Outcome | Status |
| :---: | :--- | :--- | :---: |
| **1** | Create a valid ticket | Generates `SF-1001` with `OPEN` status | **PASSED** |
| **2** | Create ticket with blank title | Throws `IllegalArgumentException` | **PASSED** |
| **3** | Invalid priority or category | Rejected by service layer / enum parser | **PASSED** |
| **4** | Multiple tickets created | Unique IDs generated (`SF-1001`, `SF-1002`, `SF-1003`) | **PASSED** |
| **5** | Search for existing ticket | Displays full details; case-insensitive | **PASSED** |
| **6** | Search for nonexistent ticket | Catches `TicketNotFoundException` without crash | **PASSED** |
| **7** | Update ticket title/priority | Changes reflect immediately with updated timestamp | **PASSED** |
| **8** | Resolve ticket | Enforces resolution notes; updates status to `RESOLVED` | **PASSED** |
| **9** | Close an `OPEN` ticket | Rejected with informative explanation | **PASSED** |
| **10** | Complete lifecycle | `OPEN` ➔ `IN_PROGRESS` ➔ `RESOLVED` ➔ `CLOSED` verified | **PASSED** |
| **11** | Invalid transition (`CLOSED` ➔ `IN_PROGRESS`) | Rejected by state machine validation | **PASSED** |
| **12** | Reopen a `CLOSED` ticket | Status transitions to `OPEN`; past notes archived | **PASSED** |
| **13** | Filter tickets | Matches exact category, priority, status, creator | **PASSED** |
| **14** | Dynamic dashboard statistics | Accurately aggregates counts from ticket collection | **PASSED** |
| **15** | Persistence across restarts | Saves to CSV, reloads on restart, sequence resumes | **PASSED** |
| **16** | Resilient console input | Letters and negative numbers reprompt cleanly | **PASSED** |
| **17** | Storage load failure safety | Refuses to overwrite storage with empty set on load error | **PASSED** |

---

## 🖥️ Sample Console Session

```
=================================================
 Welcome to Shadowfax Internal Helpdesk System  
=================================================

=============================================
    SHADOWFAX INTERNAL HELPDESK SYSTEM       
=============================================
 1. Create New Ticket
 2. View All Tickets
 3. Search Ticket by ID
 4. Update Ticket Details
 5. Change Ticket Status
 6. Resolve Ticket
 7. Close Ticket
 8. Reopen Ticket
 9. Filter Tickets
 10. Dashboard / Statistics
 11. Save Data
 0. Exit
=============================================
Select an option (0-11): 1

--- [CREATE NEW SUPPORT TICKET] ---
Enter ticket title/summary: Warehouse Barcode Scanner Offline
Enter detailed description: Zebra TC26 scanner at Koramangala Hub fails to sync package barcodes.
Select Ticket Category:
  1. Technical Support
  2. Delivery & Logistics Operations
  3. Account & Access Management
  4. Hardware & Device Support
  5. Other / General Inquiries
Select category (1-5): 4
Select Ticket Priority:
  1. Low
  2. Medium
  3. High
  4. Critical
Select priority (1-4): 3
Enter requester name / Employee ID: Hub Lead Rajesh
Assign to Support Agent:
  1. Unassigned
  2. Rahul Sharma (IT Support)
  3. Priya Patel (Operations Lead)
  4. Arun Kumar (Logistics Helpdesk)
  5. Custom Agent Name
Select assignee option (1-5): 2

[SUCCESS] Ticket created successfully!
Generated Ticket ID : SF-1001
Status              : Open
Priority            : High
Category            : Hardware & Device Support
```

---

## ⚠️ Current Scope & Limitations

1. **Storage Backend**: Stored locally in RFC-4180 CSV (`data/tickets.csv`). In production, this would be replaced with a relational database (PostgreSQL / MySQL).
2. **Authentication**: Uses role-based metadata without password authentication or encryption, as appropriate for a Core Java console internship project.
3. **Multi-User Concurrency**: Runs as a single console process. Thread-safe atomic sequences and synchronized service methods are implemented, but remote concurrent users would use a client-server or REST architecture.
