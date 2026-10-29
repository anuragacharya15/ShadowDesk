import sys
import os
from pptx import Presentation
from pptx.util import Inches, Pt
from pptx.dml.color import RGBColor
from pptx.enum.text import PP_ALIGN, MSO_ANCHOR
from pptx.enum.shapes import MSO_SHAPE

def create_deck():
    prs = Presentation()
    # 16:9 widescreen
    prs.slide_width = Inches(13.333)
    prs.slide_height = Inches(7.5)
    blank_layout = prs.slide_layouts[6]

    # Theme Colors
    COLOR_PRIMARY = RGBColor(15, 23, 42)      # #0F172A Deep Navy
    COLOR_SECONDARY = RGBColor(30, 41, 59)   # #1E293B Slate Navy
    COLOR_ACCENT = RGBColor(220, 38, 38)     # #DC2626 Crimson Red
    COLOR_ACCENT_BG = RGBColor(254, 242, 242) # Soft red
    COLOR_BG_LIGHT = RGBColor(248, 250, 252) # #F8FAFC
    COLOR_CARD_BORDER = RGBColor(226, 232, 240)
    COLOR_WHITE = RGBColor(255, 255, 255)
    COLOR_TEXT_MAIN = RGBColor(15, 23, 42)
    COLOR_TEXT_MUTED = RGBColor(100, 116, 139)
    COLOR_TEXT_LIGHT = RGBColor(203, 213, 225)
    COLOR_GREEN = RGBColor(22, 163, 74)
    COLOR_BLUE = RGBColor(2, 132, 199)
    COLOR_AMBER = RGBColor(217, 119, 6)

    def add_header(slide, title_text, category_text="SHADOWDESK"):
        # Header background banner
        header_shape = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0), Inches(0), Inches(13.333), Inches(1.15))
        header_shape.fill.solid()
        header_shape.fill.fore_color.rgb = COLOR_PRIMARY
        header_shape.line.fill.background()

        # Accent red stripe at top of header
        stripe = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0), Inches(0), Inches(13.333), Inches(0.08))
        stripe.fill.solid()
        stripe.fill.fore_color.rgb = COLOR_ACCENT
        stripe.line.fill.background()

        # Header Text Box
        tx_box = slide.shapes.add_textbox(Inches(0.8), Inches(0.18), Inches(11.7), Inches(0.85))
        tf = tx_box.text_frame
        tf.word_wrap = True
        tf.margin_left = tf.margin_top = tf.margin_right = tf.margin_bottom = 0

        p0 = tf.paragraphs[0]
        p0.text = category_text.upper()
        p0.font.size = Pt(10)
        p0.font.bold = True
        p0.font.color.rgb = COLOR_ACCENT

        p1 = tf.add_paragraph()
        p1.text = title_text
        p1.font.size = Pt(22)
        p1.font.bold = True
        p1.font.color.rgb = COLOR_WHITE

        # Footer
        footer_line = slide.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.8), Inches(7.0), Inches(11.733), Inches(0.02))
        footer_line.fill.solid()
        footer_line.fill.fore_color.rgb = COLOR_CARD_BORDER
        footer_line.line.fill.background()

        foot_box = slide.shapes.add_textbox(Inches(0.8), Inches(7.05), Inches(11.733), Inches(0.35))
        ftf = foot_box.text_frame
        ftf.word_wrap = True
        ftf.margin_left = ftf.margin_top = ftf.margin_right = ftf.margin_bottom = 0
        fp = ftf.paragraphs[0]
        fp.text = "ShadowDesk | Internship Project – Shadowfox | Student: Anurag Acharya (JECRC University)"
        fp.font.size = Pt(9)
        fp.font.color.rgb = COLOR_TEXT_MUTED

    # ==========================================
    # SLIDE 1: Title Slide
    # ==========================================
    s1 = prs.slides.add_slide(blank_layout)
    bg1 = s1.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0), Inches(0), Inches(13.333), Inches(7.5))
    bg1.fill.solid()
    bg1.fill.fore_color.rgb = COLOR_PRIMARY
    bg1.line.fill.background()

    # Left accent colored bar
    accent_bar = s1.shapes.add_shape(MSO_SHAPE.RECTANGLE, Inches(0.9), Inches(1.5), Inches(0.15), Inches(4.5))
    accent_bar.fill.solid()
    accent_bar.fill.fore_color.rgb = COLOR_ACCENT
    accent_bar.line.fill.background()

    # Title & Subtitle box
    tbox = s1.shapes.add_textbox(Inches(1.25), Inches(1.5), Inches(7.0), Inches(4.5))
    ttf = tbox.text_frame
    ttf.word_wrap = True

    p = ttf.paragraphs[0]
    p.text = "INTERNSHIP PROJECT PRESENTATION"
    p.font.size = Pt(13)
    p.font.bold = True
    p.font.color.rgb = COLOR_ACCENT
    p.space_after = Pt(12)

    p = ttf.add_paragraph()
    p.text = "ShadowDesk"
    p.font.size = Pt(46)
    p.font.bold = True
    p.font.color.rgb = COLOR_WHITE
    p.space_after = Pt(8)

    p = ttf.add_paragraph()
    p.text = "Internal Helpdesk & Support Ticket Management System"
    p.font.size = Pt(20)
    p.font.color.rgb = RGBColor(226, 232, 240)
    p.space_after = Pt(18)

    p = ttf.add_paragraph()
    p.text = "A modular Core Java application engineered using Object-Oriented Programming, state-machine lifecycle validation, and crash-resilient file persistence."
    p.font.size = Pt(13)
    p.font.color.rgb = COLOR_TEXT_LIGHT
    p.line_spacing = 1.25

    # Right Metadata Card
    card1 = s1.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(8.5), Inches(1.5), Inches(4.0), Inches(4.5))
    card1.fill.solid()
    card1.fill.fore_color.rgb = COLOR_SECONDARY
    card1.line.color.rgb = RGBColor(51, 65, 85)

    cbox = s1.shapes.add_textbox(Inches(8.8), Inches(1.8), Inches(3.4), Inches(4.0))
    ctf = cbox.text_frame
    ctf.word_wrap = True

    cp0 = ctf.paragraphs[0]
    cp0.text = "PROJECT METADATA"
    cp0.font.size = Pt(11)
    cp0.font.bold = True
    cp0.font.color.rgb = COLOR_ACCENT
    cp0.space_after = Pt(16)

    meta_items = [
        ("Organization", "Shadowfox"),
        ("Student Name", "Anurag Acharya"),
        ("College", "JECRC University"),
        ("Department", "Computer Science & Eng. (CSE)"),
        ("Mentor", "[Mentor Name]"),
        ("Internship Duration", "01/10/2026 – 31/10/2026"),
        ("Submission Date", "30/10/2026")
    ]

    for label, val in meta_items:
        mp = ctf.add_paragraph()
        mp.text = f"{label}: "
        mp.font.bold = True
        mp.font.size = Pt(11)
        mp.font.color.rgb = COLOR_WHITE
        # Add value in normal text
        run = mp.add_run()
        run.text = val
        run.font.bold = False
        run.font.color.rgb = RGBColor(226, 232, 240)
        mp.space_after = Pt(6)

    # ==========================================
    # SLIDE 2: Problem Statement & Objectives
    # ==========================================
    s2 = prs.slides.add_slide(blank_layout)
    add_header(s2, "Problem Statement & Project Objectives", "CONTEXT & GOALS")

    # Left Box: Problem Statement
    p_box = s2.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(0.8), Inches(1.4), Inches(5.7), Inches(5.3))
    p_box.fill.solid()
    p_box.fill.fore_color.rgb = COLOR_WHITE
    p_box.line.color.rgb = COLOR_CARD_BORDER

    ptb = s2.shapes.add_textbox(Inches(1.1), Inches(1.6), Inches(5.1), Inches(4.9))
    ptf = ptb.text_frame
    ptf.word_wrap = True

    p = ptf.paragraphs[0]
    p.text = "THE PROBLEM STATEMENT"
    p.font.size = Pt(12)
    p.font.bold = True
    p.font.color.rgb = COLOR_ACCENT
    p.space_after = Pt(10)

    p = ptf.add_paragraph()
    p.text = "Operational Challenges in Internal Support:"
    p.font.size = Pt(15)
    p.font.bold = True
    p.font.color.rgb = COLOR_PRIMARY
    p.space_after = Pt(12)

    problems = [
        ("Unstructured Requests", "Internal support queries raised informally via chat or word-of-mouth lead to lost inquiries and zero audit trail."),
        ("Lack of Priority Visibility", "Urgent technical or logistics blockers get lost among low-priority routine requests without standard classification."),
        ("No Lifecycle Accountability", "Without enforced lifecycle transitions, tickets linger indefinitely without assigned ownership or documented resolutions."),
        ("Data Volatility & Inconsistency", "Ad-hoc tracking lacks persistent storage, making past solutions impossible to reference for repeated incidents.")
    ]

    for title, desc in problems:
        p = ptf.add_paragraph()
        p.text = f"• {title}: "
        p.font.bold = True
        p.font.size = Pt(11)
        p.font.color.rgb = COLOR_PRIMARY
        run = p.add_run()
        run.text = desc
        run.font.bold = False
        run.font.color.rgb = COLOR_TEXT_MUTED
        p.space_after = Pt(8)

    # Right Box: Project Objectives
    o_box = s2.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(6.8), Inches(1.4), Inches(5.7), Inches(5.3))
    o_box.fill.solid()
    o_box.fill.fore_color.rgb = COLOR_WHITE
    o_box.line.color.rgb = COLOR_CARD_BORDER

    otb = s2.shapes.add_textbox(Inches(7.1), Inches(1.6), Inches(5.1), Inches(4.9))
    otf = otb.text_frame
    otf.word_wrap = True

    p = otf.paragraphs[0]
    p.text = "PROJECT OBJECTIVE"
    p.font.size = Pt(12)
    p.font.bold = True
    p.font.color.rgb = COLOR_BLUE
    p.space_after = Pt(10)

    p = otf.add_paragraph()
    p.text = "Purpose & Scope of ShadowDesk:"
    p.font.size = Pt(15)
    p.font.bold = True
    p.font.color.rgb = COLOR_PRIMARY
    p.space_after = Pt(12)

    objectives = [
        ("Automated Issue Tracking", "Provide a reliable, modular Core Java platform to log internal support tickets with unique formatted IDs (SF-1001, SF-1002)."),
        ("Enforce Strict Business Rules", "Implement a formal lifecycle state-machine preventing illegal status moves (e.g., closing an open ticket)."),
        ("Resolution & Audit Logging", "Mandate clear resolution notes before ticket resolution and retain full resolution history upon reopening."),
        ("Dynamic Performance Metrics", "Aggregate real-time metrics across status and priority levels dynamically without hard-coded counters."),
        ("Crash-Safe Persistence", "Persist all ticket states into standard RFC-4180 CSV files with atomic temporary-file replacement.")
    ]

    for title, desc in objectives:
        p = otf.add_paragraph()
        p.text = f"✔ {title}: "
        p.font.bold = True
        p.font.size = Pt(11)
        p.font.color.rgb = COLOR_PRIMARY
        run = p.add_run()
        run.text = desc
        run.font.bold = False
        run.font.color.rgb = COLOR_TEXT_MUTED
        p.space_after = Pt(7)

    # ==========================================
    # SLIDE 3: Key Implemented Features
    # ==========================================
    s3 = prs.slides.add_slide(blank_layout)
    add_header(s3, "Key Implemented Features", "SYSTEM CAPABILITIES")

    features = [
        ("Sequential Unique IDs", "Generates SF-1001, SF-1002 sequentially. Automatically recovers highest ID counter on restart from storage to prevent duplicates.", "SF-1001"),
        ("Category & Priority Enums", "Categorizes tickets under Technical, Delivery Operations, Account Access, Hardware, or Other; prioritized as Low, Medium, High, or Critical.", "CLASSIFY"),
        ("State Machine Lifecycle", "Strictly enforces OPEN -> IN_PROGRESS -> RESOLVED -> CLOSED. Directly blocks illegal status jumps at the business service level.", "WORKFLOW"),
        ("Resolution & Reopen Audit", "Mandatory non-blank notes required on resolution. Reopening a closed/resolved ticket preserves past resolution history with timestamps.", "AUDIT"),
        ("Multi-Criteria Search & Filter", "Provides case-insensitive lookup by Ticket ID, plus instant filtering by Status, Priority, Category, or Requester name.", "SEARCH"),
        ("Dynamic Dashboard & Analytics", "Dynamic calculation of total tickets, open, in-progress, resolved, closed, high-priority, and critical counts directly from memory.", "METRICS"),
        ("Robust Input Validation", "Line-based console input reader handles empty inputs, non-numeric values, and out-of-range choices without application crashes.", "DEFENSIVE"),
        ("Atomic CSV Persistence", "RFC-4180 compliant CSV storage with quoting and escaping. Uses atomic .tmp file replacement to guarantee zero data corruption.", "STORAGE")
    ]

    card_w = Inches(2.78)
    card_h = Inches(2.55)
    positions = [
        (Inches(0.8), Inches(1.4)),
        (Inches(3.78), Inches(1.4)),
        (Inches(6.76), Inches(1.4)),
        (Inches(9.74), Inches(1.4)),
        (Inches(0.8), Inches(4.2)),
        (Inches(3.78), Inches(4.2)),
        (Inches(6.76), Inches(4.2)),
        (Inches(9.74), Inches(4.2))
    ]

    for idx, (title, desc, badge) in enumerate(features):
        x, y = positions[idx]
        card = s3.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, x, y, card_w, card_h)
        card.fill.solid()
        card.fill.fore_color.rgb = COLOR_WHITE
        card.line.color.rgb = COLOR_CARD_BORDER

        # Top badge
        b_box = s3.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, x + Inches(0.18), y + Inches(0.18), Inches(1.2), Inches(0.32))
        b_box.fill.solid()
        b_box.fill.fore_color.rgb = COLOR_PRIMARY if idx % 2 == 0 else COLOR_ACCENT
        b_box.line.fill.background()
        btf = b_box.text_frame
        btf.margin_top = btf.margin_bottom = btf.margin_left = btf.margin_right = 0
        bp = btf.paragraphs[0]
        bp.alignment = PP_ALIGN.CENTER
        bp.text = badge
        bp.font.size = Pt(9)
        bp.font.bold = True
        bp.font.color.rgb = COLOR_WHITE

        # Content
        ctb = s3.shapes.add_textbox(x + Inches(0.18), y + Inches(0.6), card_w - Inches(0.36), card_h - Inches(0.7))
        ctf = ctb.text_frame
        ctf.word_wrap = True

        p = ctf.paragraphs[0]
        p.text = title
        p.font.size = Pt(13)
        p.font.bold = True
        p.font.color.rgb = COLOR_PRIMARY
        p.space_after = Pt(4)

        p = ctf.add_paragraph()
        p.text = desc
        p.font.size = Pt(10)
        p.font.color.rgb = COLOR_TEXT_MUTED
        p.line_spacing = 1.2

    # ==========================================
    # SLIDE 4: Technology Stack & Core Java Concepts
    # ==========================================
    s4 = prs.slides.add_slide(blank_layout)
    add_header(s4, "Technology Stack & Core Java Concepts", "TECHNICAL FOUNDATION")

    tech_cards = [
        ("Core Java & Zero Dependencies",
         "• Java 8+ Compatibility: Built on standard Java SE with no external frameworks or libraries.\n"
         "• java.time API: Uses immutable LocalDateTime and DateTimeFormatter for ISO persistence & UI formatting.\n"
         "• Thread-Safe Concurrency: Utilizes AtomicInteger for race-condition-free ID generation.",
         "PLATFORM"),
        ("Object-Oriented Design (OOP)",
         "• Strong Encapsulation: All entity fields private; state mutations guarded by domain methods (touch, updateDetails).\n"
         "• Layered Architecture: Strict separation into Model, Service, Repository, Utility, and Presentation layers.\n"
         "• Cohesive Classes: Single-responsibility design with focused modules.",
         "OOP PRINCIPLES"),
        ("Collections & Domain Enums",
         "• LinkedHashMap: Guarantees O(1) ticket lookups by ID while preserving chronological creation order.\n"
         "• ArrayList: Used for thread-safe defensive copies during filtering and multi-attribute searches.\n"
         "• Rich Enums: Category, Priority, and Status enums encapsulate labels and reverse-lookup logic.",
         "DATA STRUCTURES"),
        ("Robust I/O & Exception Handling",
         "• Custom Checked Exceptions: TicketNotFoundException and InvalidTicketOperationException for clean error handling.\n"
         "• RFC-4180 CSV Engine: Custom parser supporting quotes, commas, and multiline descriptions.\n"
         "• Atomic Temp-File Swapping: Eliminates risk of corrupted storage during power loss or I/O failure.",
         "I/O & RELIABILITY")
    ]

    card_w = Inches(5.7)
    card_h = Inches(2.55)
    t_positions = [
        (Inches(0.8), Inches(1.4)),
        (Inches(6.8), Inches(1.4)),
        (Inches(0.8), Inches(4.2)),
        (Inches(6.8), Inches(4.2))
    ]

    for idx, (title, points, badge) in enumerate(tech_cards):
        x, y = t_positions[idx]
        card = s4.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, x, y, card_w, card_h)
        card.fill.solid()
        card.fill.fore_color.rgb = COLOR_WHITE
        card.line.color.rgb = COLOR_CARD_BORDER

        # Header tag
        tag = s4.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, x + Inches(0.25), y + Inches(0.2), Inches(1.6), Inches(0.32))
        tag.fill.solid()
        tag.fill.fore_color.rgb = COLOR_PRIMARY
        tag.line.fill.background()
        ttf = tag.text_frame
        ttf.margin_top = ttf.margin_bottom = ttf.margin_left = ttf.margin_right = 0
        tp = ttf.paragraphs[0]
        tp.alignment = PP_ALIGN.CENTER
        tp.text = badge
        tp.font.size = Pt(9)
        tp.font.bold = True
        tp.font.color.rgb = COLOR_WHITE

        # Title & Bullet points
        ctb = s4.shapes.add_textbox(x + Inches(0.25), y + Inches(0.65), card_w - Inches(0.5), card_h - Inches(0.75))
        ctf = ctb.text_frame
        ctf.word_wrap = True

        p = ctf.paragraphs[0]
        p.text = title
        p.font.size = Pt(14)
        p.font.bold = True
        p.font.color.rgb = COLOR_PRIMARY
        p.space_after = Pt(6)

        for line in points.split("\n"):
            if line.strip():
                p = ctf.add_paragraph()
                p.text = line
                p.font.size = Pt(10.5)
                p.font.color.rgb = COLOR_TEXT_MUTED
                p.space_after = Pt(3)

    # ==========================================
    # SLIDE 5: System Architecture
    # ==========================================
    s5 = prs.slides.add_slide(blank_layout)
    add_header(s5, "System Architecture & Layered Design", "MODULAR DESIGN")

    # Left: Architecture Flow Boxes
    flow_x = Inches(0.8)
    flow_w = Inches(4.6)
    box_h = Inches(0.8)

    layers = [
        ("USER INTERFACE LAYER", "Main.java (Console UI) | InputUtil.java", COLOR_PRIMARY),
        ("BUSINESS SERVICE LAYER", "TicketService.java (Workflow, Rules, Metrics)", COLOR_ACCENT),
        ("PERSISTENCE REPOSITORY LAYER", "TicketRepository.java (RFC-4180 CSV Engine)", COLOR_BLUE),
        ("DATA STORAGE LAYER", "data/tickets.csv (Atomic Local File Storage)", COLOR_SECONDARY)
    ]

    for i, (layer_name, details, color) in enumerate(layers):
        y = Inches(1.4 + i * 1.3)
        box = s5.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, flow_x, y, flow_w, box_h)
        box.fill.solid()
        box.fill.fore_color.rgb = color
        box.line.fill.background()

        btf = box.text_frame
        btf.margin_top = btf.margin_bottom = Inches(0.12)
        p0 = btf.paragraphs[0]
        p0.alignment = PP_ALIGN.CENTER
        p0.text = layer_name
        p0.font.bold = True
        p0.font.size = Pt(12)
        p0.font.color.rgb = COLOR_WHITE

        p1 = btf.add_paragraph()
        p1.alignment = PP_ALIGN.CENTER
        p1.text = details
        p1.font.size = Pt(10)
        p1.font.color.rgb = RGBColor(226, 232, 240)

        # Draw down arrow between layers
        if i < len(layers) - 1:
            arrow = s5.shapes.add_shape(MSO_SHAPE.DOWN_ARROW, flow_x + Inches(2.1), y + box_h + Inches(0.08), Inches(0.4), Inches(0.35))
            arrow.fill.solid()
            arrow.fill.fore_color.rgb = COLOR_CARD_BORDER
            arrow.line.fill.background()

    # Right: Responsibilities Detailed Breakdown
    dt_box = s5.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(5.8), Inches(1.4), Inches(6.7), Inches(5.3))
    dt_box.fill.solid()
    dt_box.fill.fore_color.rgb = COLOR_WHITE
    dt_box.line.color.rgb = COLOR_CARD_BORDER

    dtt = s5.shapes.add_textbox(Inches(6.1), Inches(1.6), Inches(6.1), Inches(4.9))
    dtf = dtt.text_frame
    dtf.word_wrap = True

    p = dtf.paragraphs[0]
    p.text = "COMPONENT RESPONSIBILITIES & DECOUPLING"
    p.font.size = Pt(12)
    p.font.bold = True
    p.font.color.rgb = COLOR_ACCENT
    p.space_after = Pt(10)

    responsibilities = [
        ("Presentation Layer (Main.java)", "Coordinates the 12-item menu, processes user commands, and formats table output. Completely decoupled from file reading or storage logic."),
        ("Service Layer (TicketService.java)", "Acts as the business authority. Manages in-memory LinkedHashMap, validates status transitions, prevents duplicate ticket IDs, and computes dynamic statistics."),
        ("Repository Layer (TicketRepository.java)", "Encapsulates file persistence. Parses RFC-4180 CSV, handles multiline descriptions, and writes atomically using temporary files (.tmp) to protect against corruption."),
        ("Domain Entities & Enums (model/*)", "Ticket.java encapsulates fields, audit timestamps (createdAt, updatedAt), and validation. Enums define valid categories, priorities, and statuses."),
        ("Utility Support (util/*)", "InputUtil prevents Scanner newline skipping bugs and handles bad inputs. TicketIdGenerator synchronizes sequential IDs across application restarts.")
    ]

    for title, desc in responsibilities:
        p = dtf.add_paragraph()
        p.text = f"• {title}: "
        p.font.bold = True
        p.font.size = Pt(11)
        p.font.color.rgb = COLOR_PRIMARY
        run = p.add_run()
        run.text = desc
        run.font.bold = False
        run.font.color.rgb = COLOR_TEXT_MUTED
        p.space_after = Pt(8)

    # ==========================================
    # SLIDE 6: Ticket Workflow & Lifecycle
    # ==========================================
    s6 = prs.slides.add_slide(blank_layout)
    add_header(s6, "Ticket Lifecycle & Status Workflow", "STATE MACHINE")

    # Workflow Steps across top
    states = [
        ("1. CREATION", "OPEN", "New issue logged by employee; initial default state.", COLOR_BLUE),
        ("2. TRIAGE", "IN_PROGRESS", "Assigned support agent initiates troubleshooting.", COLOR_AMBER),
        ("3. RESOLUTION", "RESOLVED", "Issue resolved; mandatory resolution notes recorded.", COLOR_GREEN),
        ("4. CLOSURE", "CLOSED", "Terminal closed state; only permitted after resolution.", COLOR_PRIMARY)
    ]

    box_w = Inches(2.68)
    box_h = Inches(1.8)

    for i, (step, state, desc, col) in enumerate(states):
        x = Inches(0.8 + i * 2.98)
        y = Inches(1.5)
        sbox = s6.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, x, y, box_w, box_h)
        sbox.fill.solid()
        sbox.fill.fore_color.rgb = COLOR_WHITE
        sbox.line.color.rgb = col
        sbox.line.width = Pt(2)

        # Header tag
        tag = s6.shapes.add_shape(MSO_SHAPE.RECTANGLE, x, y, box_w, Inches(0.4))
        tag.fill.solid()
        tag.fill.fore_color.rgb = col
        tag.line.fill.background()
        ttf = tag.text_frame
        ttf.margin_top = ttf.margin_bottom = 0
        tp = ttf.paragraphs[0]
        tp.alignment = PP_ALIGN.CENTER
        tp.text = step
        tp.font.size = Pt(10)
        tp.font.bold = True
        tp.font.color.rgb = COLOR_WHITE

        # State text
        stb = s6.shapes.add_textbox(x + Inches(0.15), y + Inches(0.45), box_w - Inches(0.3), box_h - Inches(0.5))
        stf = stb.text_frame
        stf.word_wrap = True
        p0 = stf.paragraphs[0]
        p0.alignment = PP_ALIGN.CENTER
        p0.text = state
        p0.font.size = Pt(17)
        p0.font.bold = True
        p0.font.color.rgb = col
        p0.space_after = Pt(4)

        p1 = stf.add_paragraph()
        p1.alignment = PP_ALIGN.CENTER
        p1.text = desc
        p1.font.size = Pt(9.5)
        p1.font.color.rgb = COLOR_TEXT_MUTED

        # Arrow to next state
        if i < len(states) - 1:
            arr = s6.shapes.add_shape(MSO_SHAPE.RIGHT_ARROW, x + box_w + Inches(0.05), y + Inches(0.75), Inches(0.2), Inches(0.3))
            arr.fill.solid()
            arr.fill.fore_color.rgb = COLOR_CARD_BORDER
            arr.line.fill.background()

    # Bottom Split: Transition Rules & Reopening Policy
    b_left = s6.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(0.8), Inches(3.6), Inches(5.7), Inches(3.1))
    b_left.fill.solid()
    b_left.fill.fore_color.rgb = COLOR_WHITE
    b_left.line.color.rgb = COLOR_CARD_BORDER

    ltb = s6.shapes.add_textbox(Inches(1.0), Inches(3.75), Inches(5.3), Inches(2.8))
    ltf = ltb.text_frame
    ltf.word_wrap = True

    p = ltf.paragraphs[0]
    p.text = "ENFORCED STATUS TRANSITION RULES"
    p.font.size = Pt(12)
    p.font.bold = True
    p.font.color.rgb = COLOR_ACCENT
    p.space_after = Pt(8)

    rules = [
        ("OPEN -> IN_PROGRESS / RESOLVED", "Permitted. Quick issues can be directly resolved with notes."),
        ("IN_PROGRESS -> RESOLVED", "Permitted once resolution actions are documented."),
        ("RESOLVED -> CLOSED", "Strict requirement: Only resolved tickets can be closed."),
        ("ILLEGAL: OPEN / IN_PROGRESS -> CLOSED", "Strictly blocked. Throws InvalidTicketOperationException."),
        ("ILLEGAL: CLOSED -> IN_PROGRESS", "Strictly blocked. A closed ticket must be reopened first.")
    ]

    for title, desc in rules:
        p = ltf.add_paragraph()
        p.text = f"• {title}: "
        p.font.bold = True
        p.font.size = Pt(10)
        p.font.color.rgb = COLOR_PRIMARY
        run = p.add_run()
        run.text = desc
        run.font.bold = False
        run.font.color.rgb = COLOR_TEXT_MUTED
        p.space_after = Pt(4)

    # Bottom Right: Reopening & Audit Policy
    b_right = s6.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(6.8), Inches(3.6), Inches(5.7), Inches(3.1))
    b_right.fill.solid()
    b_right.fill.fore_color.rgb = COLOR_WHITE
    b_right.line.color.rgb = COLOR_CARD_BORDER

    rtb = s6.shapes.add_textbox(Inches(7.0), Inches(3.75), Inches(5.3), Inches(2.8))
    rtf = rtb.text_frame
    rtf.word_wrap = True

    p = rtf.paragraphs[0]
    p.text = "REOPENING POLICY & AUDIT RETENTION"
    p.font.size = Pt(12)
    p.font.bold = True
    p.font.color.rgb = COLOR_BLUE
    p.space_after = Pt(8)

    reopen_points = [
        ("Permitted Reopening", "Both RESOLVED and CLOSED tickets can be reopened by requesters or support agents."),
        ("State Reset", "Reopening immediately transitions the ticket status back to OPEN."),
        ("Preservation of History", "Previous resolution notes are not erased. They are archived and prefixed into the audit trail."),
        ("Optional Reopen Reason", "Users can append an optional reopening reason explaining why further investigation is required."),
        ("Timestamp Tracking", "Updates the updatedAt timestamp while maintaining the original createdAt creation time.")
    ]

    for title, desc in reopen_points:
        p = rtf.add_paragraph()
        p.text = f"✔ {title}: "
        p.font.bold = True
        p.font.size = Pt(10)
        p.font.color.rgb = COLOR_PRIMARY
        run = p.add_run()
        run.text = desc
        run.font.bold = False
        run.font.color.rgb = COLOR_TEXT_MUTED
        p.space_after = Pt(4)

    # ==========================================
    # SLIDE 7: Validation, Testing & Results
    # ==========================================
    s7 = prs.slides.add_slide(blank_layout)
    add_header(s7, "Validation, Testing & Verification", "QUALITY ASSURANCE")

    # Left: Validation Rules
    v_box = s7.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(0.8), Inches(1.4), Inches(5.4), Inches(5.3))
    v_box.fill.solid()
    v_box.fill.fore_color.rgb = COLOR_WHITE
    v_box.line.color.rgb = COLOR_CARD_BORDER

    vtb = s7.shapes.add_textbox(Inches(1.0), Inches(1.6), Inches(5.0), Inches(4.9))
    vtf = vtb.text_frame
    vtf.word_wrap = True

    p = vtf.paragraphs[0]
    p.text = "INPUT VALIDATION & DEFENSIVE CONTROLS"
    p.font.size = Pt(12)
    p.font.bold = True
    p.font.color.rgb = COLOR_ACCENT
    p.space_after = Pt(8)

    val_rules = [
        ("Line-Based Input (InputUtil)", "Exclusively reads full lines via nextLine() to prevent Scanner integer newline skipping bugs."),
        ("Non-Blank Enforcement", "Rejects empty or whitespace-only strings for titles, descriptions, requesters, and resolution notes."),
        ("Resilient Numeric Parsing", "Safely parses menu and option integers using Integer.parseInt() within a try-catch loop without crashing."),
        ("Enum Boundary Checks", "Category and Priority selections validated against numbered menu ranges (1 to 5, 1 to 4)."),
        ("Safe Case-Insensitive Lookup", "Searching by ticket ID (e.g., sf-1001 or SF-1001) catches TicketNotFoundException cleanly.")
    ]

    for title, desc in val_rules:
        p = vtf.add_paragraph()
        p.text = f"• {title}: "
        p.font.bold = True
        p.font.size = Pt(10.5)
        p.font.color.rgb = COLOR_PRIMARY
        run = p.add_run()
        run.text = desc
        run.font.bold = False
        run.font.color.rgb = COLOR_TEXT_MUTED
        p.space_after = Pt(6)

    # Right: Automated Test Results Table
    t_box = s7.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(6.5), Inches(1.4), Inches(6.0), Inches(5.3))
    t_box.fill.solid()
    t_box.fill.fore_color.rgb = COLOR_WHITE
    t_box.line.color.rgb = COLOR_CARD_BORDER

    ttb = s7.shapes.add_textbox(Inches(6.75), Inches(1.6), Inches(5.5), Inches(4.9))
    ttf = ttb.text_frame
    ttf.word_wrap = True

    p = ttf.paragraphs[0]
    p.text = "AUTOMATED TEST SUITE EXECUTION"
    p.font.size = Pt(12)
    p.font.bold = True
    p.font.color.rgb = COLOR_GREEN
    p.space_after = Pt(6)

    p = ttf.add_paragraph()
    p.text = "17/17 Verification Scenarios Passed Successfully"
    p.font.size = Pt(14)
    p.font.bold = True
    p.font.color.rgb = COLOR_PRIMARY
    p.space_after = Pt(10)

    test_samples = [
        ("TEST 1 - 4", "Ticket creation, blank title rejection, invalid priority handling, and unique sequential ID verification."),
        ("TEST 5 - 7", "Case-insensitive ID search, nonexistent ticket safe handling, and atomic field updates with timestamp refresh."),
        ("TEST 8 - 11", "Resolution notes enforcement, rejection of closing OPEN tickets, full lifecycle transition, and invalid status jump rejection."),
        ("TEST 12 - 14", "Reopening closed tickets, multi-attribute filtering, and dynamic dashboard metrics calculation."),
        ("TEST 15 - 17", "Storage persistence across restarts, bad console input resilience, and corruption protection on load failure.")
    ]

    for group, desc in test_samples:
        p = ttf.add_paragraph()
        p.text = f"✔ {group}: "
        p.font.bold = True
        p.font.size = Pt(10)
        p.font.color.rgb = COLOR_GREEN
        run = p.add_run()
        run.text = desc
        run.font.bold = False
        run.font.color.rgb = COLOR_TEXT_MUTED
        p.space_after = Pt(5)

    # Test summary pill
    spill = s7.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(6.75), Inches(6.0), Inches(5.5), Inches(0.48))
    spill.fill.solid()
    spill.fill.fore_color.rgb = RGBColor(240, 253, 244)
    spill.line.color.rgb = COLOR_GREEN
    sptf = spill.text_frame
    sptf.margin_top = sptf.margin_bottom = 0
    spp = sptf.paragraphs[0]
    spp.alignment = PP_ALIGN.CENTER
    spp.text = "Verified via test.bat (TicketSystemTestSuite.java) – 100% Pass Rate"
    spp.font.size = Pt(10.5)
    spp.font.bold = True
    spp.font.color.rgb = COLOR_GREEN

    # ==========================================
    # SLIDE 8: Conclusion & Future Scope
    # ==========================================
    s8 = prs.slides.add_slide(blank_layout)
    add_header(s8, "Conclusion & Future Enhancements", "SUMMARY & OUTLOOK")

    # Left: Conclusion Summary
    c_left = s8.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(0.8), Inches(1.4), Inches(5.7), Inches(4.2))
    c_left.fill.solid()
    c_left.fill.fore_color.rgb = COLOR_WHITE
    c_left.line.color.rgb = COLOR_CARD_BORDER

    cltb = s8.shapes.add_textbox(Inches(1.0), Inches(1.6), Inches(5.3), Inches(3.8))
    cltf = cltb.text_frame
    cltf.word_wrap = True

    p = cltf.paragraphs[0]
    p.text = "INTERNSHIP PROJECT CONCLUSION"
    p.font.size = Pt(12)
    p.font.bold = True
    p.font.color.rgb = COLOR_ACCENT
    p.space_after = Pt(10)

    conclusions = [
        ("Engineering Clean Core Java", "Successfully developed a robust, production-quality helpdesk system using Core Java principles without third-party runtime dependencies."),
        ("Adherence to OOP Best Practices", "Demonstrated rigorous encapsulation, domain state modeling, decoupled modular architecture, and custom domain exceptions."),
        ("Data Safety & Robustness", "Implemented crash-safe RFC-4180 CSV persistence with atomic temporary file swapping and recovery of ticket sequence counters."),
        ("Practical Business Workflows", "Emulated realistic logistics and tech support workflows with state machine validation, audit trails, and live analytics.")
    ]

    for title, desc in conclusions:
        p = cltf.add_paragraph()
        p.text = f"• {title}: "
        p.font.bold = True
        p.font.size = Pt(10.5)
        p.font.color.rgb = COLOR_PRIMARY
        run = p.add_run()
        run.text = desc
        run.font.bold = False
        run.font.color.rgb = COLOR_TEXT_MUTED
        p.space_after = Pt(6)

    # Right: Future Scope (Possibilities)
    c_right = s8.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(6.8), Inches(1.4), Inches(5.7), Inches(4.2))
    c_right.fill.solid()
    c_right.fill.fore_color.rgb = COLOR_WHITE
    c_right.line.color.rgb = COLOR_CARD_BORDER

    crtb = s8.shapes.add_textbox(Inches(7.0), Inches(1.6), Inches(5.3), Inches(3.8))
    crtf = crtb.text_frame
    crtf.word_wrap = True

    p = crtf.paragraphs[0]
    p.text = "FUTURE ENHANCEMENTS (PLANNED SCOPE)"
    p.font.size = Pt(12)
    p.font.bold = True
    p.font.color.rgb = COLOR_BLUE
    p.space_after = Pt(10)

    future_points = [
        ("Relational Database Integration", "Transition from flat CSV storage to enterprise RDBMS (PostgreSQL / MySQL) with transaction management and connection pooling."),
        ("Role-Based Authentication (RBAC)", "Implement secure credential login, distinguishing Requester, Support Agent, and Administrator permission levels."),
        ("Automated Notification Engine", "Integrate automated email / SMS alerts to requesters upon ticket creation, assignment, and resolution."),
        ("SLA Tracking & Escalations", "Introduce Service Level Agreement (SLA) countdown timers that auto-escalate breached tickets to senior leads.")
    ]

    for title, desc in future_points:
        p = crtf.add_paragraph()
        p.text = f"🚀 {title}: "
        p.font.bold = True
        p.font.size = Pt(10.5)
        p.font.color.rgb = COLOR_PRIMARY
        run = p.add_run()
        run.text = desc
        run.font.bold = False
        run.font.color.rgb = COLOR_TEXT_MUTED
        p.space_after = Pt(6)

    # Bottom Banner: Thank You
    thanks_card = s8.shapes.add_shape(MSO_SHAPE.ROUNDED_RECTANGLE, Inches(0.8), Inches(5.8), Inches(11.7), Inches(1.0))
    thanks_card.fill.solid()
    thanks_card.fill.fore_color.rgb = COLOR_PRIMARY
    thanks_card.line.fill.background()

    thtb = s8.shapes.add_textbox(Inches(1.0), Inches(5.9), Inches(11.3), Inches(0.8))
    thtf = thtb.text_frame
    thtf.word_wrap = True
    tp0 = thtf.paragraphs[0]
    tp0.alignment = PP_ALIGN.CENTER
    tp0.text = "Thank You! Questions & Discussion Welcome."
    tp0.font.size = Pt(18)
    tp0.font.bold = True
    tp0.font.color.rgb = COLOR_WHITE

    tp1 = thtf.add_paragraph()
    tp1.alignment = PP_ALIGN.CENTER
    tp1.text = "ShadowDesk – Internal Helpdesk System | Developed by Anurag Acharya (JECRC University, CSE)"
    tp1.font.size = Pt(11)
    tp1.font.color.rgb = RGBColor(226, 232, 240)

    # Save PPTX
    out_dir = "output"
    if not os.path.exists(out_dir):
        os.makedirs(out_dir)
    out_path = os.path.join(out_dir, "ShadowDesk_Project_Presentation.pptx")
    prs.save(out_path)
    print(f"[SUCCESS] Presentation generated: {out_path} ({len(prs.slides)} slides)")

if __name__ == "__main__":
    create_deck()
