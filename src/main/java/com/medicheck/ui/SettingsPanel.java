package com.medicheck.ui;

import com.medicheck.config.AppConfig;

import javax.swing.*;
import javax.swing.border.TitledBorder;
import java.awt.*;
import java.awt.event.*;
import java.net.URI;
import java.net.http.*;

public class SettingsPanel extends JPanel {

    private static final Color BG    = new Color(15, 23, 42);
    private static final Color CARD  = new Color(30, 41, 59);
    private static final Color TEXT  = new Color(226, 232, 240);
    private static final Color MUTED = new Color(100, 116, 139);
    private static final Color IND   = new Color(99, 102, 241);
    private static final Color GRN   = new Color(34, 197, 94);
    private static final Color RED   = new Color(239, 68, 68);
    private static final Color AMBER = new Color(245, 158, 11);

    private final AppConfig cfg = AppConfig.getInstance();

    // OpenAI fields
    private JPasswordField txtApiKey;
    private JComboBox<String> cbModel;
    private JToggleButton     btnEnabled;
    private JLabel            lblStatus;

    // General fields
    private JTextField txtCurrency;
    private JTextField txtTaxRate;
    private JTextField txtClinicName;

    public SettingsPanel() {
        setLayout(new BorderLayout(0, 0));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(BG);
        initUI();
    }

    private void initUI() {
        JLabel lblTitle = new JLabel("⚙️  Application Settings");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 18, 0));
        add(lblTitle, BorderLayout.NORTH);

        JPanel pnlContent = new JPanel();
        pnlContent.setLayout(new BoxLayout(pnlContent, BoxLayout.Y_AXIS));
        pnlContent.setOpaque(false);

        pnlContent.add(buildGeneralCard());
        pnlContent.add(Box.createRigidArea(new Dimension(0, 18)));
        pnlContent.add(buildOpenAICard());
        pnlContent.add(Box.createRigidArea(new Dimension(0, 18)));
        pnlContent.add(buildSaveBar());

        JScrollPane scroll = new JScrollPane(pnlContent);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(BG);
        scroll.getViewport().setBackground(BG);
        scroll.getVerticalScrollBar().setUnitIncrement(12);
        add(scroll, BorderLayout.CENTER);

        loadCurrentValues();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GENERAL SETTINGS CARD
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildGeneralCard() {
        JPanel card = makeCard("🏥  General Settings");
        card.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(7, 12, 7, 12);

        txtClinicName = makeField();
        txtCurrency   = makeField();
        txtTaxRate    = makeField();

        Object[][] rows = {
            {"Clinic / Hospital Name", txtClinicName},
            {"Currency Symbol (e.g. ₹)", txtCurrency},
            {"Default GST / Tax Rate (%)", txtTaxRate}
        };

        int r = 0;
        for (Object[] row : rows) {
            gc.gridy = r; gc.gridx = 0; gc.weightx = 0.35;
            JLabel lbl = makeLabel((String) row[0]);
            card.add(lbl, gc);
            gc.gridx = 1; gc.weightx = 0.65;
            card.add((Component) row[1], gc);
            r++;
        }
        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // OPENAI SETTINGS CARD
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildOpenAICard() {
        JPanel card = makeCard("🤖  AI API — OpenAI or Google Gemini (Free)");
        card.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(8, 12, 8, 12);

        // Info banner
        gc.gridy = 0; gc.gridx = 0; gc.gridwidth = 2; gc.weightx = 1.0;
        JLabel info = new JLabel(
            "<html><font color='#64748b'>"
            + "Supports <b>Google Gemini (FREE)</b> — key starts with <b>AIza...</b>  "
            + "| or <b>OpenAI</b> key starts with <b>sk-...</b><br>"
            + "Get free Gemini key: <b>aistudio.google.com/apikey</b>"
            + "</font></html>");
        info.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        card.add(info, gc);

        // Enable toggle
        gc.gridy = 1; gc.gridwidth = 1; gc.weightx = 0.35;
        card.add(makeLabel("Enable OpenAI "), gc);
        gc.gridx = 1; gc.weightx = 0.65;
        btnEnabled = new JToggleButton("● Disabled") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(isSelected() ? new Color(22, 101, 52) : new Color(39, 52, 72));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 8, 8);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        btnEnabled.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btnEnabled.setForeground(MUTED);
        btnEnabled.setContentAreaFilled(false);
        btnEnabled.setBorderPainted(false);
        btnEnabled.setFocusPainted(false);
        btnEnabled.setBorder(BorderFactory.createEmptyBorder(8, 14, 8, 14));
        btnEnabled.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btnEnabled.addActionListener(e -> {
            if (btnEnabled.isSelected()) {
                btnEnabled.setText("● Enabled");
                btnEnabled.setForeground(GRN);
            } else {
                btnEnabled.setText("● Disabled");
                btnEnabled.setForeground(MUTED);
            }
            btnEnabled.repaint();
        });
        card.add(btnEnabled, gc);

        // API Key
        gc.gridy = 2; gc.gridx = 0; gc.weightx = 0.35;
        card.add(makeLabel("API Key *"), gc);
        gc.gridx = 1; gc.weightx = 0.65;
        txtApiKey = new JPasswordField();
        txtApiKey.setFont(new Font("Segoe UI Mono", Font.PLAIN, 13));
        txtApiKey.setBackground(CARD);
        txtApiKey.setForeground(TEXT);
        txtApiKey.setCaretColor(TEXT);
        txtApiKey.setEchoChar('•');
        txtApiKey.setPreferredSize(new Dimension(0, 38));
        txtApiKey.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(55, 65, 81), 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)));
        card.add(txtApiKey, gc);

        // Show/Hide key toggle
        gc.gridy = 3; gc.gridx = 1;
        JCheckBox chkShow = new JCheckBox("Show API key");
        chkShow.setOpaque(false);
        chkShow.setForeground(MUTED);
        chkShow.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        chkShow.addActionListener(e ->
            txtApiKey.setEchoChar(chkShow.isSelected() ? (char) 0 : '•'));
        card.add(chkShow, gc);

        // Model
        gc.gridy = 4; gc.gridx = 0; gc.weightx = 0.35;
        card.add(makeLabel("AI Model"), gc);
        gc.gridx = 1; gc.weightx = 0.65;
        cbModel = new JComboBox<>(new String[]{
            "gpt-3.5-turbo", "gpt-4o-mini", "gpt-4o", "gpt-4-turbo"
        });
        cbModel.setBackground(CARD);
        cbModel.setForeground(TEXT);
        cbModel.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cbModel.setPreferredSize(new Dimension(0, 36));
        card.add(cbModel, gc);

        // Test button + status
        gc.gridy = 5; gc.gridx = 0; gc.weightx = 0.35;
        card.add(makeLabel("Connection Test"), gc);
        gc.gridx = 1;
        JPanel pnlTest = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        pnlTest.setOpaque(false);
        JButton btnTest = PatientPanel.makeButton("🔌 Test Connection", new Color(30, 58, 138), new Color(147, 197, 253));
        btnTest.addActionListener(e -> testOpenAIConnection());
        lblStatus = new JLabel(" ");
        lblStatus.setFont(new Font("Segoe UI", Font.BOLD, 12));
        pnlTest.add(btnTest);
        pnlTest.add(lblStatus);
        card.add(pnlTest, gc);

        // Key hint
        gc.gridy = 6; gc.gridx = 0; gc.gridwidth = 2;
        JLabel hint = new JLabel(
            "<html><font color='#475569'>🔑 Get your API key from <u>platform.openai.com/api-keys</u>. " +
            "Starts with <b>sk-...</b>  (keep it private!)</font></html>");
        hint.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        card.add(hint, gc);

        return card;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // SAVE BAR
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel buildSaveBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 8));
        bar.setOpaque(false);
        JButton btnReset = PatientPanel.makeButton("↺ Reset Defaults", CARD, MUTED);
        JButton btnSave  = PatientPanel.makeButton("💾  Save All Settings", IND, Color.WHITE);

        btnReset.addActionListener(e -> loadCurrentValues());
        btnSave.addActionListener(e -> saveAll());

        bar.add(btnReset);
        bar.add(btnSave);
        return bar;
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOAD / SAVE
    // ─────────────────────────────────────────────────────────────────────────
    private void loadCurrentValues() {
        txtClinicName.setText(cfg.get("app.clinic.name", "MediCheck Pharmacy"));
        txtCurrency.setText(cfg.get("currency_symbol", "₹"));
        txtTaxRate.setText(cfg.get("tax_rate", "18.0"));

        String key = cfg.get("openai.api.key");
        txtApiKey.setText(key);
        boolean enabled = cfg.getBoolean("openai.enabled", false);
        btnEnabled.setSelected(enabled);
        btnEnabled.setText(enabled ? "● Enabled" : "● Disabled");
        btnEnabled.setForeground(enabled ? GRN : MUTED);
        btnEnabled.repaint();

        String model = cfg.get("openai.model", "gpt-3.5-turbo");
        for (int i = 0; i < cbModel.getItemCount(); i++) {
            if (cbModel.getItemAt(i).equals(model)) { cbModel.setSelectedIndex(i); break; }
        }
    }

    private void saveAll() {
        String key = new String(txtApiKey.getPassword()).trim();

        cfg.set("app.clinic.name",  txtClinicName.getText().trim());
        cfg.set("currency_symbol",  txtCurrency.getText().trim());
        cfg.set("tax_rate",         txtTaxRate.getText().trim());
        cfg.set("openai.api.key",   key);
        cfg.set("openai.enabled",   String.valueOf(btnEnabled.isSelected()));
        cfg.set("openai.model",     (String) cbModel.getSelectedItem());
        cfg.save();

        JOptionPane.showMessageDialog(this,
            "✅  Settings saved successfully!\n\n" +
            "  OpenAI: " + (btnEnabled.isSelected() ? "Enabled (" + cbModel.getSelectedItem() + ")" : "Disabled") + "\n" +
            "  Currency: " + txtCurrency.getText().trim() + "\n" +
            "  Tax Rate: " + txtTaxRate.getText().trim() + "%",
            "Settings Saved", JOptionPane.INFORMATION_MESSAGE);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // TEST OpenAI CONNECTION
    // ─────────────────────────────────────────────────────────────────────────
    private void testOpenAIConnection() {
        String key = new String(txtApiKey.getPassword()).trim();
        if (key.isEmpty()) {
            lblStatus.setText("\u26A0 Enter API key first");
            lblStatus.setForeground(AMBER);
            return;
        }
        lblStatus.setText("\u23F3 Testing...");
        lblStatus.setForeground(MUTED);

        boolean isGemini = key.startsWith("AIza") || key.startsWith("AQ.");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override protected String doInBackground() {
                try {
                    java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
                    java.net.http.HttpResponse<String> resp;

                    if (isGemini) {
                        // Gemini test
                        String body = "{\"contents\":[{\"parts\":[{\"text\":\"Say OK\"}]}]}";
                        String url  = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + key;
                        java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                            .uri(java.net.URI.create(url))
                            .header("Content-Type", "application/json")
                            .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body)).build();
                        resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
                    } else {
                        // OpenAI test
                        String body = "{\"model\":\"gpt-3.5-turbo\",\"messages\":[{\"role\":\"user\",\"content\":\"Say OK\"}],\"max_tokens\":5}";
                        java.net.http.HttpRequest req = java.net.http.HttpRequest.newBuilder()
                            .uri(java.net.URI.create("https://api.openai.com/v1/chat/completions"))
                            .header("Content-Type", "application/json")
                            .header("Authorization", "Bearer " + key)
                            .POST(java.net.http.HttpRequest.BodyPublishers.ofString(body)).build();
                        resp = client.send(req, java.net.http.HttpResponse.BodyHandlers.ofString());
                    }

                    if (resp.statusCode() == 200) return "OK";
                    if (resp.statusCode() == 401 || resp.statusCode() == 403) return "INVALID_KEY";
                    return "ERROR_" + resp.statusCode();
                } catch (Exception ex) {
                    return "NET_ERROR";
                }
            }
            @Override protected void done() {
                try {
                    String res = get();
                    String provider = isGemini ? "Gemini" : "OpenAI";
                    if ("OK".equals(res)) {
                        lblStatus.setText("\u2705 " + provider + " connected!");
                        lblStatus.setForeground(GRN);
                    } else if ("INVALID_KEY".equals(res)) {
                        lblStatus.setText("\u274C Invalid " + provider + " key");
                        lblStatus.setForeground(RED);
                    } else if (res.startsWith("ERROR_429")) {
                        lblStatus.setText("\u26A0 No credits (add billing)");
                        lblStatus.setForeground(AMBER);
                    } else if ("NET_ERROR".equals(res)) {
                        lblStatus.setText("\u26A0 Network error \u2014 check internet");
                        lblStatus.setForeground(AMBER);
                    } else {
                        lblStatus.setText("\u274C Error: " + res);
                        lblStatus.setForeground(RED);
                    }
                } catch (Exception e) {
                    lblStatus.setText("\u274C Test failed");
                    lblStatus.setForeground(RED);
                }
            }
        };
        worker.execute();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // UI HELPERS
    // ─────────────────────────────────────────────────────────────────────────
    private JPanel makeCard(String title) {
        JPanel card = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setAlignmentX(Component.LEFT_ALIGNMENT);
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 600));

        TitledBorder tb = BorderFactory.createTitledBorder(
            BorderFactory.createEmptyBorder(), title);
        tb.setTitleFont(new Font("Segoe UI", Font.BOLD, 13));
        tb.setTitleColor(TEXT);
        card.setBorder(BorderFactory.createCompoundBorder(
            tb,
            BorderFactory.createEmptyBorder(6, 10, 14, 10)
        ));
        return card;
    }

    private JTextField makeField() {
        JTextField f = new JTextField();
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBackground(new Color(22, 31, 53));
        f.setForeground(TEXT);
        f.setCaretColor(TEXT);
        f.setPreferredSize(new Dimension(0, 36));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(55, 65, 81), 1, true),
            BorderFactory.createEmptyBorder(5, 12, 5, 12)));
        return f;
    }

    private JLabel makeLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        l.setForeground(MUTED);
        return l;
    }
}
