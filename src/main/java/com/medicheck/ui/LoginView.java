package com.medicheck.ui;

import com.medicheck.model.User;
import com.medicheck.service.AuthService;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.RoundRectangle2D;

public class LoginView extends JFrame {

    private static final Color BG_DARK = new Color(10, 13, 28);
    private static final Color INDIGO  = new Color(99, 102, 241);
    private static final Color CARD    = new Color(22, 31, 53);
    private static final Color TEXT    = new Color(226, 232, 240);
    private static final Color MUTED   = new Color(100, 116, 139);
    private static final Color GREEN   = new Color(34, 197, 94);
    private static final Color RED     = new Color(239, 68, 68);

    private JTextField    txtUsername;
    private JPasswordField txtPassword;
    private JButton        btnLogin;
    private JLabel         lblStatus;

    private final AuthService authService = new AuthService();

    public LoginView() {
        setTitle("MediCheck — Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setUndecorated(true);
        setSize(880, 560);
        setLocationRelativeTo(null);
        setShape(new RoundRectangle2D.Double(0, 0, 880, 560, 20, 20));
        initUI();
        enableDragging();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOGIN UI
    // ─────────────────────────────────────────────────────────────────────────
    private void initUI() {
        JPanel root = new JPanel(new GridLayout(1, 2)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(BG_DARK);
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };

        // ── LEFT BRANDING ────────────────────────────────────────────────
        JPanel pnlLeft = new JPanel(new GridBagLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(55, 48, 163), getWidth(), getHeight(), new Color(6, 78, 59)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };

        JPanel colLeft = new JPanel();
        colLeft.setOpaque(false);
        colLeft.setLayout(new BoxLayout(colLeft, BoxLayout.Y_AXIS));
        colLeft.setBorder(BorderFactory.createEmptyBorder(60, 50, 60, 50));

        JLabel icon = new JLabel("⚕");
        icon.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        icon.setForeground(Color.WHITE);
        icon.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("MediCheck");
        title.setFont(new Font("Segoe UI", Font.BOLD, 36));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel sub = new JLabel("<html><center>AI Powered Medical<br>Management System</center></html>");
        sub.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        sub.setForeground(new Color(255, 255, 255, 180));
        sub.setAlignmentX(Component.CENTER_ALIGNMENT);

        JPanel pnlFeatures = new JPanel();
        pnlFeatures.setOpaque(false);
        pnlFeatures.setLayout(new BoxLayout(pnlFeatures, BoxLayout.Y_AXIS));
        pnlFeatures.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
        pnlFeatures.setAlignmentX(Component.CENTER_ALIGNMENT);

        for (String f : new String[]{
            "✅  Patient & Doctor Management",
            "✅  Smart Inventory & Barcode Scan",
            "✅  POS Billing & Invoice Print",
            "✅  AI Medical Assistant",
            "✅  Sales Analytics & Reports"}) {
            JLabel fl = new JLabel(f);
            fl.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            fl.setForeground(new Color(255, 255, 255, 200));
            fl.setAlignmentX(Component.LEFT_ALIGNMENT);
            fl.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));
            pnlFeatures.add(fl);
        }

        colLeft.add(icon);
        colLeft.add(Box.createRigidArea(new Dimension(0, 10)));
        colLeft.add(title);
        colLeft.add(Box.createRigidArea(new Dimension(0, 8)));
        colLeft.add(sub);
        colLeft.add(pnlFeatures);
        pnlLeft.add(colLeft);

        // ── RIGHT LOGIN FORM ─────────────────────────────────────────────
        JPanel pnlRight = new JPanel(new GridBagLayout());
        pnlRight.setBackground(CARD);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        JLabel lblWelcome = new JLabel("Welcome back 👋");
        lblWelcome.setFont(new Font("Segoe UI", Font.BOLD, 26));
        lblWelcome.setForeground(TEXT);

        JLabel lblSub2 = new JLabel("Sign in to your account");
        lblSub2.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblSub2.setForeground(MUTED);

        JLabel lblUser = new JLabel("Username");
        lblUser.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblUser.setForeground(MUTED);

        txtUsername = PatientPanel.makeField("admin");
        txtUsername.setPreferredSize(new Dimension(320, 40));

        JLabel lblPass = new JLabel("Password");
        lblPass.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblPass.setForeground(MUTED);

        txtPassword = new JPasswordField("Admin@123");
        txtPassword.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtPassword.setBackground(new Color(30, 41, 59));
        txtPassword.setForeground(TEXT);
        txtPassword.setCaretColor(TEXT);
        txtPassword.setPreferredSize(new Dimension(320, 40));
        txtPassword.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(71, 85, 105), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)));

        btnLogin = new JButton("Sign In →") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, INDIGO, getWidth(), 0, new Color(55, 48, 163)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnLogin.setForeground(Color.WHITE);
        btnLogin.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnLogin.setPreferredSize(new Dimension(320, 44));
        btnLogin.setContentAreaFilled(false);
        btnLogin.setBorderPainted(false);
        btnLogin.setFocusPainted(false);
        btnLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnLogin.addActionListener(e -> attemptLogin());

        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblStatus.setForeground(RED);

        // Sign Up link
        JPanel pnlSignUp = new JPanel(new FlowLayout(FlowLayout.CENTER, 5, 0));
        pnlSignUp.setOpaque(false);
        JLabel lblNoAcc = new JLabel("Don't have an account?");
        lblNoAcc.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        lblNoAcc.setForeground(MUTED);
        JLabel lblSignUp = new JLabel("Register here");
        lblSignUp.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblSignUp.setForeground(INDIGO);
        lblSignUp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblSignUp.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { showRegisterDialog(); }
            @Override public void mouseEntered(MouseEvent e) { lblSignUp.setForeground(new Color(129, 140, 248)); }
            @Override public void mouseExited(MouseEvent e)  { lblSignUp.setForeground(INDIGO); }
        });
        pnlSignUp.add(lblNoAcc);
        pnlSignUp.add(lblSignUp);

        JLabel lblClose = new JLabel("✕  Close");
        lblClose.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblClose.setForeground(MUTED);
        lblClose.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        lblClose.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { System.exit(0); }
            @Override public void mouseEntered(MouseEvent e) { lblClose.setForeground(RED); }
            @Override public void mouseExited(MouseEvent e)  { lblClose.setForeground(MUTED); }
        });

        gbc.gridy = 0; pnlRight.add(lblWelcome, gbc);
        gbc.gridy = 1; pnlRight.add(lblSub2, gbc);
        gbc.gridy = 2; gbc.insets = new Insets(20, 8, 4, 8); pnlRight.add(lblUser, gbc);
        gbc.gridy = 3; gbc.insets = new Insets(0, 8, 8, 8);  pnlRight.add(txtUsername, gbc);
        gbc.gridy = 4; pnlRight.add(lblPass, gbc);
        gbc.gridy = 5; pnlRight.add(txtPassword, gbc);
        gbc.gridy = 6; gbc.insets = new Insets(14, 8, 4, 8); pnlRight.add(btnLogin, gbc);
        gbc.gridy = 7; gbc.insets = new Insets(0, 8, 2, 8);  pnlRight.add(lblStatus, gbc);
        gbc.gridy = 8; gbc.insets = new Insets(6, 8, 4, 8);  pnlRight.add(pnlSignUp, gbc);
        gbc.gridy = 9; gbc.insets = new Insets(12, 8, 8, 8); pnlRight.add(lblClose, gbc);

        getRootPane().setDefaultButton(btnLogin);
        root.add(pnlLeft);
        root.add(pnlRight);
        setContentPane(root);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOGIN LOGIC
    // ─────────────────────────────────────────────────────────────────────────
    private void attemptLogin() {
        String username = txtUsername.getText().trim();
        String password = new String(txtPassword.getPassword());
        if (username.isEmpty() || password.isEmpty()) {
            lblStatus.setText("⚠  Username and password are required.");
            return;
        }
        lblStatus.setText("Signing in...");
        lblStatus.setForeground(new Color(148, 163, 184));
        btnLogin.setEnabled(false);

        SwingWorker<java.util.Optional<User>, Void> worker = new SwingWorker<>() {
            @Override protected java.util.Optional<User> doInBackground() {
                return authService.login(username, password);
            }
            @Override protected void done() {
                try {
                    java.util.Optional<User> user = get();
                    if (user.isPresent()) {
                        dispose();
                        new MainFrame().setVisible(true);
                    } else {
                        lblStatus.setText("❌  Invalid username or password.");
                        lblStatus.setForeground(RED);
                        txtPassword.setText("");
                    }
                } catch (Exception ex) {
                    lblStatus.setText("❌  " + ex.getMessage());
                    lblStatus.setForeground(RED);
                } finally {
                    btnLogin.setEnabled(true);
                }
            }
        };
        worker.execute();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PREMIUM REGISTRATION DIALOG
    // ─────────────────────────────────────────────────────────────────────────
    private void showRegisterDialog() {
        JDialog dlg = new JDialog(this, "Create Account", true);
        dlg.setUndecorated(true);
        dlg.setSize(700, 700);
        dlg.setLocationRelativeTo(this);
        dlg.setShape(new RoundRectangle2D.Double(0, 0, 700, 700, 18, 18));

        final Color DB = new Color(13, 18, 35);
        final Color DC = new Color(22, 31, 53);
        final Color DS = new Color(30, 41, 59);
        final Color DT = new Color(226, 232, 240);
        final Color DM = new Color(100, 116, 139);
        final Color DI = new Color(99, 102, 241);
        final Color DG = new Color(34, 197, 94);
        final Color DR = new Color(239, 68, 68);
        final Color DA = new Color(245, 158, 11);

        JPanel root = new JPanel(new BorderLayout());
        root.setBackground(DB);

        // ── HEADER ───────────────────────────────────────────────────────
        JPanel pnlHeader = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, new Color(55, 48, 163), getWidth(), 0, new Color(6, 78, 59)));
                g2.fillRect(0, 0, getWidth(), getHeight());
                g2.dispose();
            }
        };
        pnlHeader.setPreferredSize(new Dimension(0, 95));
        pnlHeader.setBorder(BorderFactory.createEmptyBorder(0, 28, 0, 16));

        JPanel pnlHL = new JPanel(new FlowLayout(FlowLayout.LEFT, 14, 22));
        pnlHL.setOpaque(false);
        JLabel icoH = new JLabel("📝");
        icoH.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 32));
        JPanel pnlHT = new JPanel();
        pnlHT.setOpaque(false);
        pnlHT.setLayout(new BoxLayout(pnlHT, BoxLayout.Y_AXIS));
        JLabel hT = new JLabel("Create New Account");
        hT.setFont(new Font("Segoe UI", Font.BOLD, 20));
        hT.setForeground(Color.WHITE);
        JLabel hS = new JLabel("Register a new system user with role & credentials");
        hS.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        hS.setForeground(new Color(255, 255, 255, 170));
        pnlHT.add(hT); pnlHT.add(hS);
        pnlHL.add(icoH); pnlHL.add(pnlHT);
        pnlHeader.add(pnlHL, BorderLayout.WEST);

        JLabel xBtn = new JLabel("  ✕  ");
        xBtn.setFont(new Font("Segoe UI", Font.BOLD, 18));
        xBtn.setForeground(new Color(255, 255, 255, 140));
        xBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        xBtn.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { dlg.dispose(); }
            @Override public void mouseEntered(MouseEvent e) { xBtn.setForeground(Color.WHITE); }
            @Override public void mouseExited(MouseEvent e)  { xBtn.setForeground(new Color(255, 255, 255, 140)); }
        });
        pnlHeader.add(xBtn, BorderLayout.EAST);

        // ── FORM ─────────────────────────────────────────────────────────
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(DB);
        form.setBorder(BorderFactory.createEmptyBorder(20, 28, 10, 28));
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 6, 5, 6);

        // Field creators
        java.util.function.Supplier<JTextField> mf = () -> {
            JTextField f = new JTextField();
            f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            f.setBackground(DS); f.setForeground(DT); f.setCaretColor(DT);
            f.setPreferredSize(new Dimension(0, 38));
            f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(55, 65, 81), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
            return f;
        };
        java.util.function.Supplier<JPasswordField> mpass = () -> {
            JPasswordField f = new JPasswordField();
            f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            f.setBackground(DS); f.setForeground(DT); f.setCaretColor(DT);
            f.setPreferredSize(new Dimension(0, 38));
            f.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(55, 65, 81), 1, true),
                BorderFactory.createEmptyBorder(6, 12, 6, 12)));
            return f;
        };
        java.util.function.Function<String, JLabel> mkL = t -> {
            JLabel l = new JLabel(t);
            l.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            l.setForeground(DM);
            return l;
        };

        JTextField    tFN  = mf.get();
        JTextField    tUN  = mf.get();
        JTextField    tEM  = mf.get();
        JTextField    tPH  = mf.get();
        JTextField    tDes = mf.get();
        JPasswordField tP1 = mpass.get();
        JPasswordField tP2 = mpass.get();

        // Password strength bar
        JPanel pnlStr = new JPanel(new GridLayout(1, 4, 4, 0));
        pnlStr.setOpaque(false);
        JPanel[] sbar = new JPanel[4];
        for (int i = 0; i < 4; i++) {
            sbar[i] = new JPanel();
            sbar[i].setBackground(new Color(51, 65, 85));
            sbar[i].setPreferredSize(new Dimension(0, 5));
            pnlStr.add(sbar[i]);
        }
        JLabel sLbl = new JLabel(" ");
        sLbl.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        sLbl.setForeground(DM);
        tP1.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                String pw = new String(tP1.getPassword());
                int sc = 0;
                if (pw.length() >= 8) sc++;
                if (pw.chars().anyMatch(Character::isUpperCase)) sc++;
                if (pw.chars().anyMatch(Character::isDigit)) sc++;
                if (pw.chars().anyMatch(c -> "!@#$%^&*()-_+=<>?".indexOf(c) >= 0)) sc++;
                Color[] cc = {DR, DA, new Color(234, 179, 8), DG};
                String[] tt = {"Weak", "Fair", "Good", "Strong ✓"};
                for (int i = 0; i < 4; i++) sbar[i].setBackground(i < sc ? cc[sc - 1] : new Color(51, 65, 85));
                sLbl.setText(sc == 0 ? " " : tt[sc - 1]);
                sLbl.setForeground(sc > 0 ? cc[sc - 1] : DM);
            }
        });

        // Role toggle cards
        String[] rN = {"Pharmacist", "Doctor", "Admin"};
        String[] rI = {"💊", "🩺", "👑"};
        String[] rD = {"Billing & Inventory", "Clinical & Patients", "Full System Access"};
        Color[]  rC = {new Color(20, 184, 166), new Color(59, 130, 246), new Color(239, 68, 68)};
        final String[] selRole = {"Pharmacist"};

        JPanel pnlRoles = new JPanel(new GridLayout(1, 3, 10, 0));
        pnlRoles.setOpaque(false);
        ButtonGroup bg = new ButtonGroup();
        for (int idx = 0; idx < 3; idx++) {
            final int fi = idx;
            JToggleButton rtb = new JToggleButton(
                "<html><center>" + rI[fi] + "<br><b>" + rN[fi] + "</b><br><small>" + rD[fi] + "</small></center></html>"
            ) {
                @Override protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g.create();
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    boolean sel = isSelected();
                    g2.setColor(sel ? new Color(rC[fi].getRed(), rC[fi].getGreen(), rC[fi].getBlue(), 45) : DS);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                    if (sel) {
                        g2.setColor(rC[fi]);
                        g2.setStroke(new BasicStroke(2f));
                        g2.drawRoundRect(1, 1, getWidth() - 2, getHeight() - 2, 12, 12);
                    }
                    g2.dispose();
                    super.paintComponent(g);
                }
            };
            rtb.setForeground(DT);
            rtb.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            rtb.setFocusPainted(false); rtb.setBorderPainted(false);
            rtb.setContentAreaFilled(false); rtb.setOpaque(false);
            rtb.setPreferredSize(new Dimension(0, 85));
            rtb.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            rtb.setSelected(idx == 0);
            rtb.addActionListener(ev -> { selRole[0] = rN[fi]; pnlRoles.repaint(); });
            bg.add(rtb);
            pnlRoles.add(rtb);
        }

        // ── LAYOUT ───────────────────────────────────────────────────────
        int r = 0;
        gc.gridy = r; gc.gridx = 0; gc.gridwidth = 1; gc.weightx = 0.5;
        form.add(mkL.apply("Full Name *"), gc);
        gc.gridx = 1; form.add(mkL.apply("Username *"), gc);
        r++;
        gc.gridy = r; gc.gridx = 0; form.add(tFN, gc);
        gc.gridx = 1; form.add(tUN, gc);
        r++;
        gc.gridy = r; gc.gridx = 0; form.add(mkL.apply("Email Address"), gc);
        gc.gridx = 1; form.add(mkL.apply("Phone Number"), gc);
        r++;
        gc.gridy = r; gc.gridx = 0; form.add(tEM, gc);
        gc.gridx = 1; form.add(tPH, gc);
        r++;
        gc.gridy = r; gc.gridx = 0; gc.gridwidth = 2; gc.insets = new Insets(10, 6, 5, 6);
        form.add(mkL.apply("Designation / Title  (e.g. MBBS, Senior Pharmacist, Intern)"), gc);
        r++;
        gc.gridy = r; gc.insets = new Insets(0, 6, 10, 6);
        form.add(tDes, gc);
        r++;
        gc.gridy = r; gc.insets = new Insets(12, 6, 5, 6);
        JLabel roleLbl = new JLabel("Select Role *");
        roleLbl.setFont(new Font("Segoe UI", Font.BOLD, 12)); roleLbl.setForeground(DM);
        form.add(roleLbl, gc);
        r++;
        gc.gridy = r; gc.insets = new Insets(0, 6, 14, 6);
        form.add(pnlRoles, gc);
        r++;
        gc.gridy = r; gc.gridx = 0; gc.gridwidth = 1; gc.weightx = 0.5; gc.insets = new Insets(5, 6, 5, 6);
        form.add(mkL.apply("Password *"), gc);
        gc.gridx = 1; form.add(mkL.apply("Confirm Password *"), gc);
        r++;
        gc.gridy = r; gc.gridx = 0; form.add(tP1, gc);
        gc.gridx = 1; form.add(tP2, gc);
        r++;
        gc.gridy = r; gc.gridx = 0; gc.gridwidth = 2; gc.insets = new Insets(6, 6, 2, 6);
        form.add(pnlStr, gc);
        r++;
        gc.gridy = r; gc.insets = new Insets(2, 6, 2, 6);
        form.add(sLbl, gc);
        r++;
        gc.gridy = r; gc.insets = new Insets(2, 6, 4, 6);
        JLabel rulesL = new JLabel("<html><font color='#475569'>Min 8 chars &middot; uppercase &middot; number &middot; special char (!@#$%)</font></html>");
        rulesL.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        form.add(rulesL, gc);
        r++;
        JLabel errLbl = new JLabel(" ");
        errLbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        errLbl.setForeground(DR);
        gc.gridy = r; gc.insets = new Insets(4, 6, 4, 6);
        form.add(errLbl, gc);

        // ── BUTTON BAR ───────────────────────────────────────────────────
        JPanel pnlBtns = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setColor(DC); g2.fillRect(0, 0, getWidth(), getHeight());
                g2.setColor(new Color(31, 41, 60)); g2.fillRect(0, 0, getWidth(), 1);
                g2.dispose();
            }
        };
        pnlBtns.setOpaque(false);
        pnlBtns.setBorder(BorderFactory.createEmptyBorder(14, 24, 16, 24));

        JLabel tos = new JLabel("<html><font color='#475569'>By registering, user agrees to MediCheck policies.</font></html>");
        tos.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        pnlBtns.add(tos, BorderLayout.WEST);

        JPanel pnlBR = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlBR.setOpaque(false);

        JButton btnC = new JButton("Cancel");
        btnC.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnC.setBackground(DS); btnC.setForeground(DM);
        btnC.setBorderPainted(false); btnC.setFocusPainted(false);
        btnC.setBorder(BorderFactory.createEmptyBorder(10, 22, 10, 22));
        btnC.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnC.addActionListener(e -> dlg.dispose());

        JButton btnR = new JButton("Create Account →") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, DI, getWidth(), 0, new Color(55, 48, 163)));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnR.setFont(new Font("Segoe UI", Font.BOLD, 13));
        btnR.setForeground(Color.WHITE);
        btnR.setContentAreaFilled(false); btnR.setBorderPainted(false); btnR.setFocusPainted(false);
        btnR.setBorder(BorderFactory.createEmptyBorder(10, 26, 10, 26));
        btnR.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        btnR.addActionListener(ev -> {
            String fn  = tFN.getText().trim();
            String un  = tUN.getText().trim();
            String em  = tEM.getText().trim();
            String ph  = tPH.getText().trim();
            String des = tDes.getText().trim();
            String p1  = new String(tP1.getPassword());
            String p2  = new String(tP2.getPassword());
            String role = selRole[0];

            if (fn.isEmpty())     { errLbl.setText("⚠  Full name is required.");            tFN.requestFocus(); return; }
            if (un.isEmpty())     { errLbl.setText("⚠  Username is required.");             tUN.requestFocus(); return; }
            if (un.length() < 3)  { errLbl.setText("⚠  Username must be at least 3 chars."); return; }
            if (p1.isEmpty())     { errLbl.setText("⚠  Password is required.");             tP1.requestFocus(); return; }
            if (!p1.equals(p2))   { errLbl.setText("❌  Passwords do not match.");           tP2.requestFocus(); return; }

            try {
                User u = new User();
                u.setFullName(fn);
                u.setUsername(un);
                u.setEmail(em.isEmpty() ? null : em);
                int rId = "Admin".equals(role) ? 1 : "Doctor".equals(role) ? 2 : 3;
                u.setRoleId(rId);
                authService.register(u, p1);
                dlg.dispose();
                JOptionPane.showMessageDialog(LoginView.this,
                    "✅  Account created successfully!\n\n" +
                    "  Name       : " + fn + "\n" +
                    "  Username   : " + un + "\n" +
                    "  Role       : " + role + (des.isEmpty() ? "" : " · " + des) + "\n" +
                    "  Phone      : " + (ph.isEmpty() ? "—" : ph) + "\n\n" +
                    "They can now sign in with their credentials.",
                    "Registration Successful", JOptionPane.INFORMATION_MESSAGE);
                txtUsername.setText(un);
                txtPassword.setText("");
                txtPassword.requestFocus();
            } catch (Exception ex) {
                errLbl.setText("❌  " + ex.getMessage());
            }
        });

        pnlBR.add(btnC); pnlBR.add(btnR);
        pnlBtns.add(pnlBR, BorderLayout.EAST);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(DB);
        scroll.getViewport().setBackground(DB);
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        root.add(pnlHeader, BorderLayout.NORTH);
        root.add(scroll,    BorderLayout.CENTER);
        root.add(pnlBtns,   BorderLayout.SOUTH);
        dlg.setContentPane(root);
        dlg.setVisible(true);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HELPERS
    // ─────────────────────────────────────────────────────────────────────────
    private void enableDragging() {
        final int[] pos = {0, 0};
        addMouseListener(new MouseAdapter() {
            @Override public void mousePressed(MouseEvent e) { pos[0] = e.getX(); pos[1] = e.getY(); }
        });
        addMouseMotionListener(new MouseMotionAdapter() {
            @Override public void mouseDragged(MouseEvent e) {
                setLocation(getX() + e.getX() - pos[0], getY() + e.getY() - pos[1]);
            }
        });
    }
}
