import os
import sys
from reportlab.lib.pagesizes import A4
from reportlab.lib.units import inch
from reportlab.lib.colors import HexColor
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle, PageBreak, KeepTogether, HRFlowable
)
from reportlab.pdfgen import canvas
import fitz  # PyMuPDF to verify exact page count and layout

class NumberedCanvas(canvas.Canvas):
    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self._saved_page_states = []

    def showPage(self):
        self._saved_page_states.append(dict(self.__dict__))
        self._startPage()

    def save(self):
        num_pages = len(self._saved_page_states)
        for state in self._saved_page_states:
            self.__dict__.update(state)
            self.draw_page_decorations(num_pages)
            super().showPage()
        super().save()

    def draw_page_decorations(self, total_pages):
        self.saveState()
        page_w, page_h = A4

        # Running Header (pages 2 and 3)
        if self._pageNumber > 1:
            self.setFont("Helvetica-Bold", 8)
            self.setFillColor(HexColor("#0F172A"))
            self.drawString(36, page_h - 28, "SHADOWDESK – INTERNAL HELPDESK & SUPPORT TICKET MANAGEMENT SYSTEM")
            self.setFont("Helvetica", 8)
            self.setFillColor(HexColor("#64748B"))
            self.drawRightString(page_w - 36, page_h - 28, "INTERNSHIP REPORT | SHADOWFOX")
            self.setStrokeColor(HexColor("#E2E8F0"))
            self.setLineWidth(0.75)
            self.line(36, page_h - 32, page_w - 36, page_h - 32)

        # Running Footer (all pages)
        self.setStrokeColor(HexColor("#E2E8F0"))
        self.setLineWidth(0.75)
        self.line(36, 32, page_w - 36, 32)

        self.setFont("Helvetica", 8)
        self.setFillColor(HexColor("#64748B"))
        self.drawString(36, 20, "Student: Anurag Acharya | JECRC University (CSE) | Organization: Shadowfox")
        self.drawRightString(page_w - 36, 20, f"Page {self._pageNumber} of {total_pages}")
        self.restoreState()

def build_pdf():
    out_dir = "output"
    if not os.path.exists(out_dir):
        os.makedirs(out_dir)
    pdf_path = os.path.join(out_dir, "ShadowDesk_Project_Report.pdf")

    # Margins: 36 pt (0.5 inch) left/right, top 38 pt, bottom 38 pt
    doc = SimpleDocTemplate(
        pdf_path,
        pagesize=A4,
        leftMargin=36,
        rightMargin=36,
        topMargin=38,
        bottomMargin=38
    )

    # Palette
    COLOR_PRIMARY = HexColor("#0F172A")    # Deep Navy
    COLOR_SECONDARY = HexColor("#1E293B")  # Slate Navy
    COLOR_ACCENT = HexColor("#DC2626")     # Crimson Red
    COLOR_TEXT = HexColor("#1E293B")       # Dark Charcoal
    COLOR_MUTED = HexColor("#64748B")      # Slate Muted
    COLOR_BORDER = HexColor("#E2E8F0")     # Light Border
    COLOR_BG_CARD = HexColor("#F8FAFC")    # Card Background

    # Typography Styles
    styles = getSampleStyleSheet()

    title_style = ParagraphStyle(
        'DocTitle',
        fontName='Helvetica-Bold',
        fontSize=18,
        leading=22,
        textColor=COLOR_PRIMARY,
        spaceAfter=3
    )

    subtitle_style = ParagraphStyle(
        'DocSubtitle',
        fontName='Helvetica',
        fontSize=10.5,
        leading=14,
        textColor=COLOR_MUTED,
        spaceAfter=8
    )

    sec_heading_style = ParagraphStyle(
        'SecHeading',
        fontName='Helvetica-Bold',
        fontSize=11.5,
        leading=15,
        textColor=COLOR_PRIMARY,
        spaceBefore=7,
        spaceAfter=4,
        keepWithNext=True
    )

    sub_heading_style = ParagraphStyle(
        'SubHeading',
        fontName='Helvetica-Bold',
        fontSize=9.5,
        leading=13,
        textColor=COLOR_ACCENT,
        spaceBefore=4,
        spaceAfter=2,
        keepWithNext=True
    )

    body_style = ParagraphStyle(
        'BodyTextCustom',
        fontName='Helvetica',
        fontSize=8.8,
        leading=12.2,
        textColor=COLOR_TEXT,
        spaceAfter=4
    )

    bullet_style = ParagraphStyle(
        'BulletCustom',
        fontName='Helvetica',
        fontSize=8.5,
        leading=11.8,
        textColor=COLOR_TEXT,
        leftIndent=12,
        spaceAfter=2.5
    )

    table_cell = ParagraphStyle(
        'TableCell',
        fontName='Helvetica',
        fontSize=8.2,
        leading=11,
        textColor=COLOR_TEXT
    )

    table_cell_bold = ParagraphStyle(
        'TableCellBold',
        fontName='Helvetica-Bold',
        fontSize=8.2,
        leading=11,
        textColor=COLOR_PRIMARY
    )

    table_header = ParagraphStyle(
        'TableHeader',
        fontName='Helvetica-Bold',
        fontSize=8.5,
        leading=11.5,
        textColor=HexColor("#FFFFFF")
    )

    story = []

    # =========================================================================
    # PAGE 1: PROJECT OVERVIEW
    # =========================================================================

    # Title Banner Block
    banner_data = [
        [
            Paragraph("<b>ShadowDesk</b><br/><font size=11 color='#64748B'>Internal Helpdesk & Support Ticket Management System</font>", title_style)
        ]
    ]
    banner_table = Table(banner_data, colWidths=[523.27])
    banner_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), COLOR_BG_CARD),
        ('BOX', (0,0), (-1,-1), 1, COLOR_BORDER),
        ('LINEBEFORE', (0,0), (0,-1), 4, COLOR_ACCENT),
        ('TOPPADDING', (0,0), (-1,-1), 8),
        ('BOTTOMPADDING', (0,0), (-1,-1), 8),
        ('LEFTPADDING', (0,0), (-1,-1), 12),
        ('RIGHTPADDING', (0,0), (-1,-1), 12),
    ]))
    story.append(banner_table)
    story.append(Spacer(1, 6))

    # Internship Metadata Table
    meta_data = [
        [
            Paragraph("<b>Project Title:</b>", table_cell_bold),
            Paragraph("ShadowDesk – Internal Helpdesk & Support Ticket Management System", table_cell),
            Paragraph("<b>Internship Organization:</b>", table_cell_bold),
            Paragraph("Shadowfox", table_cell)
        ],
        [
            Paragraph("<b>Student Name:</b>", table_cell_bold),
            Paragraph("Anurag Acharya", table_cell),
            Paragraph("<b>Mentor Name:</b>", table_cell_bold),
            Paragraph("[Mentor Name]", table_cell)
        ],
        [
            Paragraph("<b>College / Institution:</b>", table_cell_bold),
            Paragraph("JECRC University", table_cell),
            Paragraph("<b>Department:</b>", table_cell_bold),
            Paragraph("Computer Science & Engineering (CSE)", table_cell)
        ],
        [
            Paragraph("<b>Internship Duration:</b>", table_cell_bold),
            Paragraph("01/10/2026 – 31/10/2026", table_cell),
            Paragraph("<b>Submission Date:</b>", table_cell_bold),
            Paragraph("30/10/2026", table_cell)
        ]
    ]
    meta_table = Table(meta_data, colWidths=[105, 160, 110, 148.27])
    meta_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,-1), HexColor("#FFFFFF")),
        ('BOX', (0,0), (-1,-1), 0.75, COLOR_BORDER),
        ('INNERGRID', (0,0), (-1,-1), 0.5, HexColor("#F1F5F9")),
        ('TOPPADDING', (0,0), (-1,-1), 4),
        ('BOTTOMPADDING', (0,0), (-1,-1), 4),
        ('LEFTPADDING', (0,0), (-1,-1), 6),
        ('RIGHTPADDING', (0,0), (-1,-1), 6),
    ]))
    story.append(meta_table)
    story.append(Spacer(1, 8))

    # 1. Abstract
    story.append(Paragraph("1. Abstract", sec_heading_style))
    story.append(Paragraph(
        "<b>ShadowDesk</b> is a robust, modular, and dependency-free internal support ticket management system developed in Core Java (Java 8+ compatible). In enterprise logistics and technology operations, internal support teams frequently face unstructured issue reporting, lack of priority visibility, and lost audit trails. ShadowDesk addresses this challenge by providing a centralized application that allows internal personnel to log support tickets with unique formatted IDs (<code>SF-1001</code>, <code>SF-1002</code>), track issues through an enforced lifecycle state machine (<code>OPEN &rarr; IN_PROGRESS &rarr; RESOLVED &rarr; CLOSED</code>), mandate resolution notes, dynamically compute operational metrics, and persist records using crash-resilient RFC-4180 CSV storage. Engineered strictly adhering to Object-Oriented Programming (OOP) principles and layered architectural separation, the application delivers enterprise-grade reliability without external third-party dependencies.",
        body_style
    ))
    story.append(Spacer(1, 6))

    # 2. Problem Statement
    story.append(Paragraph("2. Problem Statement", sec_heading_style))
    story.append(Paragraph(
        "Modern corporate and logistics environments handle hundreds of daily operational requests—such as device connectivity failures, warehouse scanner breakdowns, driver app authentication blockers, and account permissions. When organizations manage these requests informally across chat channels, emails, or spreadsheets, several critical bottlenecks occur:",
        body_style
    ))
    story.append(Paragraph("• <b>Lack of Structured Intake:</b> Unformatted requests lack mandatory operational attributes (category, urgency, requester contact), causing prolonged diagnostic delays.", bullet_style))
    story.append(Paragraph("• <b>Absence of Priority Differentiation:</b> Critical operational blockers cannot be separated from routine inquiries without systematic categorization.", bullet_style))
    story.append(Paragraph("• <b>Uncontrolled State Transitions:</b> Informal tracking allows tickets to be prematurely marked resolved or closed without documented root causes or fixes.", bullet_style))
    story.append(Paragraph("• <b>Data Loss & Sequence Corruption:</b> Non-persistent or naive text storage frequently suffers from data corruption when user input contains commas, quotes, or line breaks.", bullet_style))
    story.append(Spacer(1, 6))

    # 3. Project Objectives
    story.append(Paragraph("3. Project Objectives", sec_heading_style))
    story.append(Paragraph("The primary objective of the ShadowDesk project is to build an extensible, production-quality Core Java helpdesk solution that guarantees operational integrity:", body_style))
    story.append(Paragraph("1. <b>Automated Identification:</b> Implement thread-safe sequential ticket ID generation (e.g., <code>SF-1001</code>) with sequence recovery across application restarts.", bullet_style))
    story.append(Paragraph("2. <b>Enforced State Workflow:</b> Enforce valid state transitions at the business layer to reject invalid status jumps (e.g., closing an uncompleted ticket).", bullet_style))
    story.append(Paragraph("3. <b>Comprehensive Audit Logging:</b> Enforce mandatory resolution notes upon resolution and preserve historical notes when tickets are reopened.", bullet_style))
    story.append(Paragraph("4. <b>Dynamic Operational Analytics:</b> Provide live metric aggregations (total, open, in-progress, resolved, closed, critical) computed directly from memory.", bullet_style))
    story.append(Paragraph("5. <b>Safe File Persistence:</b> Implement RFC-4180 compliant CSV storage with atomic temporary file swapping to prevent data corruption during unexpected failures.", bullet_style))

    # End of Page 1
    story.append(PageBreak())

    # =========================================================================
    # PAGE 2: IMPLEMENTATION AND WORKFLOW
    # =========================================================================

    # 4. Technology Stack
    story.append(Paragraph("4. Technology Stack & Environment", sec_heading_style))
    story.append(Paragraph(
        "ShadowDesk is implemented strictly in <b>Core Java SE</b> without external third-party frameworks (such as Spring, Hibernate, or external JSON libraries), guaranteeing universal execution on any Java 8+ runtime environment. Key platform components include: "
        "<b>Java SE 8+</b> compatibility, <b>Java Collections Framework</b> (<code>LinkedHashMap</code> for O(1) key lookups with preserved insertion order, <code>ArrayList</code> for defensive copying and filtering), "
        "<b>Java 8 Date/Time API</b> (<code>java.time.LocalDateTime</code> and <code>DateTimeFormatter</code> for immutable ISO persistence and human-readable formatting), "
        "<b>Atomic Concurrency</b> (<code>java.util.concurrent.atomic.AtomicInteger</code> for thread-safe sequential ID generation), and "
        "<b>Java NIO File Channels</b> (<code>java.nio.file.Files</code> with atomic move operations for crash-safe persistence).",
        body_style
    ))
    story.append(Spacer(1, 4))

    # 5. System Design & Layered Architecture
    story.append(Paragraph("5. System Design & Architecture", sec_heading_style))
    story.append(Paragraph(
        "The project follows a disciplined multi-tiered architectural model ensuring single responsibility and high cohesion:",
        body_style
    ))

    arch_data = [
        [
            Paragraph("<b>Architectural Layer</b>", table_header),
            Paragraph("<b>Component / Class</b>", table_header),
            Paragraph("<b>Core Responsibilities & Implementation Details</b>", table_header)
        ],
        [
            Paragraph("<b>Presentation Layer</b>", table_cell_bold),
            Paragraph("<code>Main.java</code><br/><code>InputUtil.java</code>", table_cell),
            Paragraph("Coordinates the 12-item interactive console menu; captures and validates user input; formats data tables; isolates console I/O from domain business logic.", table_cell)
        ],
        [
            Paragraph("<b>Service Layer</b>", table_cell_bold),
            Paragraph("<code>TicketService.java</code>", table_cell),
            Paragraph("Authoritative business engine. Enforces state transitions, manages in-memory <code>LinkedHashMap</code>, validates creation/update constraints, and computes dynamic metrics.", table_cell)
        ],
        [
            Paragraph("<b>Persistence Layer</b>", table_cell_bold),
            Paragraph("<code>TicketRepository.java</code>", table_cell),
            Paragraph("Reads and writes RFC-4180 compliant CSV (<code>data/tickets.csv</code>). Handles quotes, commas, and newlines; writes to temporary files before atomic file swapping.", table_cell)
        ],
        [
            Paragraph("<b>Domain Model</b>", table_cell_bold),
            Paragraph("<code>Ticket.java</code><br/><code>Enums</code>", table_cell),
            Paragraph("Encapsulates ticket attributes, audit timestamps (<code>createdAt</code>, <code>updatedAt</code>), and domain methods. Enums: <code>TicketCategory</code>, <code>TicketPriority</code>, <code>TicketStatus</code>.", table_cell)
        ]
    ]
    arch_table = Table(arch_data, colWidths=[105, 110, 308.27])
    arch_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), COLOR_PRIMARY),
        ('BOX', (0,0), (-1,-1), 0.75, COLOR_BORDER),
        ('INNERGRID', (0,0), (-1,-1), 0.5, HexColor("#F1F5F9")),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [HexColor("#FFFFFF"), COLOR_BG_CARD]),
        ('TOPPADDING', (0,0), (-1,-1), 3.5),
        ('BOTTOMPADDING', (0,0), (-1,-1), 3.5),
        ('LEFTPADDING', (0,0), (-1,-1), 5),
        ('RIGHTPADDING', (0,0), (-1,-1), 5),
    ]))
    story.append(arch_table)
    story.append(Spacer(1, 5))

    # 6. Major Implemented Features
    story.append(Paragraph("6. Major Implemented Features", sec_heading_style))
    story.append(Paragraph("• <b>Sequential ID Management:</b> Automatic generation of readable identifiers (<code>SF-1001</code>, <code>SF-1002</code>). Recovers highest counter from persistent storage on startup to eliminate ID reuse.", bullet_style))
    story.append(Paragraph("• <b>Categorization & Prioritization:</b> Supports five categories (<i>Technical, Delivery Operations, Account Access, Hardware, Other</i>) and four priorities (<i>Low, Medium, High, Critical</i>).", bullet_style))
    story.append(Paragraph("• <b>Multi-Attribute Filtering & Search:</b> Case-insensitive O(1) ID search plus multi-criteria filtering by status, priority, category, or creator name.", bullet_style))
    story.append(Paragraph("• <b>Dynamic Metric Dashboard:</b> Real-time dynamic aggregation of total, open, in-progress, resolved, closed, and high/critical tickets directly from active memory.", bullet_style))
    story.append(Paragraph("• <b>Defensive Console Controls:</b> Line-based scanning avoids Scanner integer buffer bugs; handles non-numeric characters and out-of-range choices gracefully.", bullet_style))
    story.append(Spacer(1, 4))

    # 7. Ticket Workflow & State Machine
    story.append(Paragraph("7. Ticket Lifecycle Workflow", sec_heading_style))
    story.append(Paragraph(
        "Ticket lifecycles follow a strict finite-state machine governed by <code>TicketService.getAvailableTransitions()</code>:",
        body_style
    ))

    flow_data = [
        [
            Paragraph("<b>Current State</b>", table_header),
            Paragraph("<b>Permitted Transitions</b>", table_header),
            Paragraph("<b>Business Constraints & Validation Rules</b>", table_header)
        ],
        [
            Paragraph("<font color='#0284C7'><b>OPEN</b></font>", table_cell),
            Paragraph("&rarr; IN_PROGRESS<br/>&rarr; RESOLVED", table_cell),
            Paragraph("Initial state upon ticket creation. Can transition to IN_PROGRESS when assigned, or directly to RESOLVED if fixed immediately with resolution notes.", table_cell)
        ],
        [
            Paragraph("<font color='#D97706'><b>IN_PROGRESS</b></font>", table_cell),
            Paragraph("&rarr; RESOLVED", table_cell),
            Paragraph("Work actively underway. Can only move to RESOLVED; cannot jump directly to CLOSED.", table_cell)
        ],
        [
            Paragraph("<font color='#16A34A'><b>RESOLVED</b></font>", table_cell),
            Paragraph("&rarr; CLOSED<br/>&rarr; OPEN (Reopen)", table_cell),
            Paragraph("Mandatory non-blank resolution notes required. Can be officially CLOSED by admin/requester or moved back to OPEN if the issue persists.", table_cell)
        ],
        [
            Paragraph("<font color='#64748B'><b>CLOSED</b></font>", table_cell),
            Paragraph("&rarr; OPEN (Reopen)", table_cell),
            Paragraph("Terminal state. Direct moves to IN_PROGRESS or RESOLVED are prohibited. Reopening transitions to OPEN and archives previous resolution notes.", table_cell)
        ]
    ]
    flow_table = Table(flow_data, colWidths=[90, 110, 323.27])
    flow_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), COLOR_SECONDARY),
        ('BOX', (0,0), (-1,-1), 0.75, COLOR_BORDER),
        ('INNERGRID', (0,0), (-1,-1), 0.5, HexColor("#F1F5F9")),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [HexColor("#FFFFFF"), COLOR_BG_CARD]),
        ('TOPPADDING', (0,0), (-1,-1), 3),
        ('BOTTOMPADDING', (0,0), (-1,-1), 3),
        ('LEFTPADDING', (0,0), (-1,-1), 5),
        ('RIGHTPADDING', (0,0), (-1,-1), 5),
    ]))
    story.append(flow_table)
    story.append(Spacer(1, 4))

    # 8. OOP Principles Applied
    story.append(Paragraph("8. Object-Oriented Programming Principles Applied", sec_heading_style))
    story.append(Paragraph(
        "• <b>Encapsulation:</b> All 11 attributes in <code>Ticket.java</code> are <code>private</code>. Modifications occur strictly through domain mutators (<code>touch()</code>, <code>updateDetails()</code>).<br/>"
        "• <b>Custom Domain Exceptions:</b> Checked exceptions (<code>TicketNotFoundException</code>, <code>InvalidTicketOperationException</code>) clearly separate business errors from technical bugs.<br/>"
        "• <b>Decoupled Persistence:</b> Storage logic is wholly isolated in <code>TicketRepository</code>, allowing future migration to SQL databases without altering service or presentation code.",
        body_style
    ))

    # End of Page 2
    story.append(PageBreak())

    # =========================================================================
    # PAGE 3: TESTING, OUTCOMES AND CONCLUSION
    # =========================================================================

    # 9. Validation & Testing
    story.append(Paragraph("9. Validation and Testing", sec_heading_style))
    story.append(Paragraph(
        "The application was tested through an automated, dependency-free test runner (<code>TicketSystemTestSuite.java</code>) executing 17 comprehensive functional, validation, and persistence test cases. All 17 tests passed with zero failures:",
        body_style
    ))

    test_data = [
        [
            Paragraph("<b>#</b>", table_header),
            Paragraph("<b>Test Case Description</b>", table_header),
            Paragraph("<b>Expected Validation / Business Behavior</b>", table_header),
            Paragraph("<b>Result</b>", table_header)
        ],
        [Paragraph("1", table_cell), Paragraph("Valid ticket creation", table_cell), Paragraph("Generates SF-1001 with default OPEN status and valid timestamps", table_cell), Paragraph("<font color='#16A34A'><b>PASS</b></font>", table_cell)],
        [Paragraph("2", table_cell), Paragraph("Blank title rejection", table_cell), Paragraph("Throws IllegalArgumentException on empty or whitespace title", table_cell), Paragraph("<font color='#16A34A'><b>PASS</b></font>", table_cell)],
        [Paragraph("3", table_cell), Paragraph("Invalid priority/category handling", table_cell), Paragraph("Rejects null or unlisted categories and priorities safely", table_cell), Paragraph("<font color='#16A34A'><b>PASS</b></font>", table_cell)],
        [Paragraph("4", table_cell), Paragraph("Unique sequential IDs", table_cell), Paragraph("Generates SF-1001, SF-1002, SF-1003 with zero collisions", table_cell), Paragraph("<font color='#16A34A'><b>PASS</b></font>", table_cell)],
        [Paragraph("5", table_cell), Paragraph("Search existing ticket", table_cell), Paragraph("O(1) retrieval with full detail matching and case-insensitivity", table_cell), Paragraph("<font color='#16A34A'><b>PASS</b></font>", table_cell)],
        [Paragraph("6", table_cell), Paragraph("Search nonexistent ticket", table_cell), Paragraph("Catches TicketNotFoundException gracefully without application crash", table_cell), Paragraph("<font color='#16A34A'><b>PASS</b></font>", table_cell)],
        [Paragraph("7", table_cell), Paragraph("Atomic ticket update", table_cell), Paragraph("Updates fields and refreshes updatedAt timestamp atomically", table_cell), Paragraph("<font color='#16A34A'><b>PASS</b></font>", table_cell)],
        [Paragraph("8", table_cell), Paragraph("Resolution notes enforcement", table_cell), Paragraph("Blocks resolution with blank notes; records resolution text on success", table_cell), Paragraph("<font color='#16A34A'><b>PASS</b></font>", table_cell)],
        [Paragraph("9", table_cell), Paragraph("Reject closing OPEN ticket", table_cell), Paragraph("Throws InvalidTicketOperationException; requires RESOLVED state", table_cell), Paragraph("<font color='#16A34A'><b>PASS</b></font>", table_cell)],
        [Paragraph("10", table_cell), Paragraph("Full lifecycle transition", table_cell), Paragraph("Verifies OPEN &rarr; IN_PROGRESS &rarr; RESOLVED &rarr; CLOSED flow", table_cell), Paragraph("<font color='#16A34A'><b>PASS</b></font>", table_cell)],
        [Paragraph("11", table_cell), Paragraph("Reject invalid transition", table_cell), Paragraph("Blocks direct move from CLOSED to IN_PROGRESS without reopening", table_cell), Paragraph("<font color='#16A34A'><b>PASS</b></font>", table_cell)],
        [Paragraph("12", table_cell), Paragraph("Reopening CLOSED ticket", table_cell), Paragraph("Reverts status to OPEN while archiving prior resolution notes", table_cell), Paragraph("<font color='#16A34A'><b>PASS</b></font>", table_cell)],
        [Paragraph("13", table_cell), Paragraph("Multi-attribute filter", table_cell), Paragraph("Correctly filters by Category, Priority, Status, and Creator", table_cell), Paragraph("<font color='#16A34A'><b>PASS</b></font>", table_cell)],
        [Paragraph("14", table_cell), Paragraph("Dynamic dashboard stats", table_cell), Paragraph("Accurately aggregates live ticket counts across status and priorities", table_cell), Paragraph("<font color='#16A34A'><b>PASS</b></font>", table_cell)],
        [Paragraph("15", table_cell), Paragraph("Persistence across restarts", table_cell), Paragraph("Saves to CSV, reloads tickets, and recovers sequential ID counter", table_cell), Paragraph("<font color='#16A34A'><b>PASS</b></font>", table_cell)],
        [Paragraph("16", table_cell), Paragraph("Console input resilience", table_cell), Paragraph("Handles alphabetic and out-of-range menu inputs without termination", table_cell), Paragraph("<font color='#16A34A'><b>PASS</b></font>", table_cell)],
        [Paragraph("17", table_cell), Paragraph("Storage load failure safety", table_cell), Paragraph("Refuses to overwrite valid CSV storage with empty collection on error", table_cell), Paragraph("<font color='#16A34A'><b>PASS</b></font>", table_cell)]
    ]
    test_table = Table(test_data, colWidths=[20, 140, 318.27, 45])
    test_table.setStyle(TableStyle([
        ('BACKGROUND', (0,0), (-1,0), COLOR_PRIMARY),
        ('BOX', (0,0), (-1,-1), 0.75, COLOR_BORDER),
        ('INNERGRID', (0,0), (-1,-1), 0.5, HexColor("#F1F5F9")),
        ('ROWBACKGROUNDS', (0,1), (-1,-1), [HexColor("#FFFFFF"), COLOR_BG_CARD]),
        ('TOPPADDING', (0,0), (-1,-1), 1.8),
        ('BOTTOMPADDING', (0,0), (-1,-1), 1.8),
        ('LEFTPADDING', (0,0), (-1,-1), 4),
        ('RIGHTPADDING', (0,0), (-1,-1), 4),
    ]))
    story.append(test_table)
    story.append(Spacer(1, 5))

    # 10. Project Outcome
    story.append(Paragraph("10. Project Outcome", sec_heading_style))
    story.append(Paragraph(
        "The project delivered a complete, dependable Core Java internal helpdesk system featuring three distinct interfaces: an <b>Interactive Console CLI</b> (<code>run.bat</code>), a <b>Modern Web UI Dashboard</b> (<code>run-web.bat</code> powered by built-in <code>HttpServer</code> on <code>http://localhost:8080</code>), and a <b>Desktop Swing GUI</b> (<code>run-gui.bat</code>). The application meets all functional requirements with zero external runtime dependencies.",
        body_style
    ))
    story.append(Spacer(1, 4))

    # 11. Limitations & Future Scope
    story.append(Paragraph("11. Limitations and Future Scope", sec_heading_style))
    story.append(Paragraph(
        "• <b>Current Limitations:</b> Data storage relies on local CSV flat files, limiting concurrent multi-machine operations. Authentication operates via predefined employee/agent metadata without password security.<br/>"
        "• <b>Future Scope:</b> Transition to an enterprise relational database (PostgreSQL/MySQL) with connection pooling; implement JWT/RBAC role authentication; integrate automated email/SMS webhook notifications on status changes; and introduce SLA countdown timers with automated escalation.",
        body_style
    ))
    story.append(Spacer(1, 4))

    # 12. Conclusion
    story.append(Paragraph("12. Conclusion", sec_heading_style))
    story.append(Paragraph(
        "The ShadowDesk project successfully fulfills all internship objectives set forth at Shadowfox. It showcases the practical application of Core Java, strong encapsulation, layered decoupling, state-machine validation, and crash-resilient file persistence in solving real-world enterprise operational bottlenecks.",
        body_style
    ))

    # Build PDF using NumberedCanvas
    doc.build(story, canvasmaker=NumberedCanvas)
    print(f"[SUCCESS] PDF generated: {pdf_path}")

    # Verify exact page count using fitz (PyMuPDF)
    doc_fitz = fitz.open(pdf_path)
    total_pages = len(doc_fitz)
    print(f"[VERIFY] Total PDF Pages: {total_pages}")
    doc_fitz.close()
    return total_pages

if __name__ == "__main__":
    pages = build_pdf()
    if pages != 3:
        print(f"[WARNING] Page count is {pages}, expected strictly 3 pages!")
        sys.exit(1)
