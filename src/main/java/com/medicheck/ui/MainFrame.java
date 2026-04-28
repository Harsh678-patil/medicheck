package com.medicheck.ui;

import com.medicheck.config.AppConfig;
import com.medicheck.util.SessionManager;
import com.medicheck.service.AuthService;

import javax.swing.*;
import java.awt.*;

public class MainFrame extends JFrame {

    private static final Color BG = new Color(15, 23, 42);
    private static final Color SIDEBAR_BG = new Color(10, 15, 30);
    private static final Color HEADER_BG = new Color(20, 30, 50);
    private static final Color TEXT = new Color(226, 232, 240);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color INDIGO = new Color(99, 102, 241);
    private static final Color SELECTED_NAV = new Color(30, 27, 75);

    private final AuthService authService = new AuthService();
    private JPanel mainContent;
    private CardLayout cardLayout;
    private String activeNav = "Dashboard";

    public MainFrame() {
        setTitle("MediCheck — AI Medical Management System");
        setSize(1280, 820);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        getContentPane().setBackground(BG);
        initUI();
    }

    private void initUI() {
        setLayout(new BorderLayout());

        // ========================
        // HEADER BAR
        // ========================
        JPanel pnlHeader = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(HEADER_BG);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(31, 41, 60));
                g2.fillRect(0, getHeight() - 1, getWidth(), 1);
                g2.dispose();
            }
        };
        pnlHeader.setPreferredSize(new Dimension(0, 60));
        pnlHeader.setOpaque(false);
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        // Logo + Name
        JPanel pnlLogo = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlLogo.setOpaque(false);
        pnlLogo.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        JLabel lblLogo = new JLabel("⚕");
        lblLogo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 28));
        lblLogo.setForeground(INDIGO);
        JLabel lblName = new JLabel("MediCheck");
        lblName.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblName.setForeground(new Color(226, 232, 240));
        JLabel lblTag = new JLabel("AI Medical System");
        lblTag.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblTag.setForeground(MUTED);
        pnlLogo.add(lblLogo); pnlLogo.add(lblName); pnlLogo.add(lblTag);
        pnlHeader.add(pnlLogo, BorderLayout.WEST);

        // Right user info
        JPanel pnlUser = new JPanel(new FlowLayout(FlowLayout.RIGHT, 14, 0));
        pnlUser.setOpaque(false);
        pnlUser.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        String role = SessionManager.getCurrentRole();
        String username = SessionManager.getCurrentUsername();
        Color roleColor = "Admin".equals(role) ? new Color(239, 68, 68)
                : "Doctor".equals(role) ? new Color(59, 130, 246)
                : new Color(34, 197, 94);

        JLabel lblRolePill = new JLabel(" " + role + " ");
        lblRolePill.setBackground(roleColor);
        lblRolePill.setForeground(Color.WHITE);
        lblRolePill.setFont(new Font("Segoe UI", Font.BOLD, 11));
        lblRolePill.setOpaque(true);
        lblRolePill.setBorder(BorderFactory.createEmptyBorder(3, 7, 3, 7));

        JLabel lblWelcome = new JLabel("👤  " + (username != null ? username : "demo_admin"));
        lblWelcome.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblWelcome.setForeground(TEXT);

        JButton btnLogout = PatientPanel.makeButton("Logout", new Color(239, 68, 68), Color.WHITE);
        btnLogout.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnLogout.addActionListener(e -> logout());

        pnlUser.add(lblRolePill); pnlUser.add(lblWelcome); pnlUser.add(btnLogout);
        pnlHeader.add(pnlUser, BorderLayout.EAST);

        add(pnlHeader, BorderLayout.NORTH);

        // ========================
        // SIDEBAR
        // ========================
        JPanel pnlSidebar = new JPanel();
        pnlSidebar.setLayout(new BoxLayout(pnlSidebar, BoxLayout.Y_AXIS));
        pnlSidebar.setPreferredSize(new Dimension(210, 0));
        pnlSidebar.setBackground(SIDEBAR_BG);
        pnlSidebar.setBorder(BorderFactory.createEmptyBorder(15, 0, 20, 0));

        // ========================
        // CONTENT AREA
        // ========================
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(BG);

        // Section label helper
        Runnable sepDash = () -> addSectionLabel(pnlSidebar, "MAIN");

        // Add views and nav buttons
        DashboardPanel dashboard = new DashboardPanel();
        mainContent.add(dashboard, "Dashboard");
        addNavBtn(pnlSidebar, "📊", "Dashboard", () -> { dashboard.loadData(); showCard("Dashboard"); });

        addSectionLabel(pnlSidebar, "PATIENTS & DOCTORS");

        PatientPanel patientPanel = new PatientPanel();
        mainContent.add(patientPanel, "Patients");
        addNavBtn(pnlSidebar, "👥", "Patients", () -> showCard("Patients"));

        DoctorPanel doctorPanel = new DoctorPanel();
        mainContent.add(doctorPanel, "Doctors");
        addNavBtn(pnlSidebar, "🩺", "Doctors", () -> { doctorPanel.loadData(); showCard("Doctors"); });

        MedicinePanel medicinePanel = new MedicinePanel();
        mainContent.add(medicinePanel, "Medicines");
        addNavBtn(pnlSidebar, "💊", "Medicines", () -> showCard("Medicines"));

        PrescriptionPanel prescriptionPanel = new PrescriptionPanel();
        mainContent.add(prescriptionPanel, "Prescriptions");
        addNavBtn(pnlSidebar, "📋", "Prescriptions", () -> { prescriptionPanel.loadData(); showCard("Prescriptions"); });

        addSectionLabel(pnlSidebar, "BILLING");

        if (SessionManager.canAccess("Admin", "Pharmacist")) {
            POSPanel posPanel = new POSPanel();
            mainContent.add(posPanel, "POS/Billing");
            addNavBtn(pnlSidebar, "🛒", "POS / Billing", () -> showCard("POS/Billing"));
        }

        ReportsPanel reportsPanel = new ReportsPanel();
        mainContent.add(reportsPanel, "Reports");
        addNavBtn(pnlSidebar, "📈", "Reports", () -> showCard("Reports"));

        addSectionLabel(pnlSidebar, "TOOLS");

        AIAssistantPanel aiPanel = new AIAssistantPanel();
        mainContent.add(aiPanel, "AI Assistant");
        addNavBtn(pnlSidebar, "🤖", "AI Assistant", () -> showCard("AI Assistant"));

        if (SessionManager.isAdmin()) {
            addSectionLabel(pnlSidebar, "ADMIN");
            AdminPanel adminPanel = new AdminPanel();
            mainContent.add(adminPanel, "Users");
            addNavBtn(pnlSidebar, "👤", "User Management", () -> showCard("Users"));

            SettingsPanel settingsPanel = new SettingsPanel();
            mainContent.add(settingsPanel, "Settings");
            addNavBtn(pnlSidebar, "⚙️", "Settings", () -> showCard("Settings"));
        }

        pnlSidebar.add(Box.createVerticalGlue());

        // Version footer
        JLabel lblVer = new JLabel("  v1.0.0 · MediCheck", SwingConstants.LEFT);
        lblVer.setForeground(new Color(51, 65, 85));
        lblVer.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblVer.setAlignmentX(Component.LEFT_ALIGNMENT);
        pnlSidebar.add(lblVer);

        add(pnlSidebar, BorderLayout.WEST);
        add(mainContent, BorderLayout.CENTER);
        cardLayout.show(mainContent, "Dashboard");
    }

    private void showCard(String name) {
        activeNav = name;
        cardLayout.show(mainContent, name);
    }

    private void addSectionLabel(JPanel sidebar, String text) {
        sidebar.add(Box.createRigidArea(new Dimension(0, 12)));
        JLabel lbl = new JLabel("  " + text);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 10));
        lbl.setForeground(new Color(71, 85, 105));
        lbl.setMaximumSize(new Dimension(210, 20));
        lbl.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(lbl);
        sidebar.add(Box.createRigidArea(new Dimension(0, 4)));
    }

    private void addNavBtn(JPanel sidebar, String icon, String label, Runnable action) {
        JButton btn = new JButton(icon + "  " + label) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                boolean isActive = activeNav.equals(label) || (label.contains("Billing") && activeNav.equals("POS/Billing"))
                        || (label.contains("Management") && activeNav.equals("Users"));
                if (isActive) {
                    g2.setColor(SELECTED_NAV);
                    g2.fillRoundRect(6, 2, getWidth() - 12, getHeight() - 4, 10, 10);
                    g2.setColor(INDIGO);
                    g2.fillRoundRect(0, 8, 3, getHeight() - 16, 3, 3);
                }
                g2.dispose();
                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        btn.setForeground(new Color(203, 213, 225));
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setMaximumSize(new Dimension(210, 42));
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 10));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override public void mouseEntered(java.awt.event.MouseEvent e) { btn.setForeground(Color.WHITE); }
            @Override public void mouseExited(java.awt.event.MouseEvent e) { btn.setForeground(new Color(203, 213, 225)); }
        });

        btn.addActionListener(e -> {
            action.run();
            sidebar.repaint();
        });

        sidebar.add(btn);
    }

    private void logout() {
        authService.logout();
        dispose();
        new LoginView().setVisible(true);
    }
}
