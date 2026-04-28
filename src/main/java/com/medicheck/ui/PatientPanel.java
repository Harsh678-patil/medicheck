package com.medicheck.ui;

import com.medicheck.model.Patient;
import com.medicheck.model.Doctor;
import com.medicheck.service.PatientService;
import com.medicheck.service.DoctorService;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class PatientPanel extends JPanel {

    private static final Color BG = new Color(15, 23, 42);
    private static final Color CARD = new Color(30, 41, 59);
    private static final Color TEXT = new Color(226, 232, 240);
    private static final Color MUTED = new Color(148, 163, 184);
    private static final Color INDIGO = new Color(99, 102, 241);
    private static final Color GREEN = new Color(34, 197, 94);
    private static final Color RED = new Color(239, 68, 68);

    private final PatientService patientService = new PatientService();
    private final DoctorService doctorService = new DoctorService();
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private List<Patient> allPatients;

    public PatientPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(BG);
        initUI();
        loadData("");
    }

    private void initUI() {
        // Header row
        JPanel pnlHeader = new JPanel(new BorderLayout(10, 0));
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("👥  Patient Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT);
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlRight.setOpaque(false);

        txtSearch = new JTextField(22);
        txtSearch.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txtSearch.setBackground(CARD);
        txtSearch.setForeground(TEXT);
        txtSearch.setCaretColor(TEXT);
        txtSearch.putClientProperty("JTextField.placeholderText", "🔍  Search patient name, phone...");
        txtSearch.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(71, 85, 105), 1, true),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        ));
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { filterTable(txtSearch.getText()); }
        });

        JButton btnAdd = makeButton("+ Add Patient", GREEN, Color.WHITE);
        btnAdd.addActionListener(e -> showPatientDialog(null));

        pnlRight.add(txtSearch);
        pnlRight.add(btnAdd);
        pnlHeader.add(pnlRight, BorderLayout.EAST);

        add(pnlHeader, BorderLayout.NORTH);

        // Table
        String[] cols = {"#", "Patient Name", "Age", "Gender", "Phone", "Disease / Condition", "Assigned Doctor"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(row % 2 == 0 ? CARD : new Color(39, 52, 72));
                c.setForeground(col == 5 ? new Color(251, 191, 36) : TEXT);
                if (isRowSelected(row)) c.setBackground(new Color(55, 48, 163));
                return c;
            }
        };
        styleTable(table);

        // Action buttons panel below table
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        pnlActions.setBackground(new Color(23, 32, 49));
        JButton btnEdit = makeButton("✏  Edit", INDIGO, Color.WHITE);
        JButton btnDelete = makeButton("🗑  Delete", RED, Color.WHITE);
        btnEdit.addActionListener(e -> editSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        pnlActions.add(btnEdit);
        pnlActions.add(btnDelete);

        JPanel tableWrapper = createTableWrapper(table);
        JPanel pnlCenter = new JPanel(new BorderLayout());
        pnlCenter.setOpaque(false);
        pnlCenter.add(tableWrapper, BorderLayout.CENTER);
        pnlCenter.add(pnlActions, BorderLayout.SOUTH);

        add(pnlCenter, BorderLayout.CENTER);

        // Stats bar
        add(createStatsBar(), BorderLayout.SOUTH);
    }

    private JPanel createStatsBar() {
        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 25, 8));
        bar.setBackground(new Color(23, 32, 49));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(51, 65, 85)));

        addStat(bar, "Total Patients", String.valueOf(MockDataProvider.getTotalPatients()), INDIGO);
        addStat(bar, "Today's Visits", "23", GREEN);
        addStat(bar, "Pending Follow-ups", "7", new Color(245, 158, 11));

        return bar;
    }

    private void addStat(JPanel bar, String label, String value, Color color) {
        JLabel lbl = new JLabel(label + ": ");
        lbl.setForeground(MUTED);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JLabel val = new JLabel(value);
        val.setForeground(color);
        val.setFont(new Font("Segoe UI", Font.BOLD, 13));
        bar.add(lbl); bar.add(val);
    }

    private void loadData(String keyword) {
        allPatients = new ArrayList<>();
        try { allPatients = patientService.getAllPatients(); } catch (Exception ignored) {}
        if (allPatients.isEmpty()) allPatients = MockDataProvider.getPatients();
        filterTable(keyword);
    }

    private void filterTable(String keyword) {
        tableModel.setRowCount(0);
        String kw = keyword.toLowerCase();
        allPatients.stream()
            .filter(p -> kw.isEmpty() || p.getFullName().toLowerCase().contains(kw)
                    || (p.getPhone() != null && p.getPhone().contains(kw))
                    || (p.getDisease() != null && p.getDisease().toLowerCase().contains(kw)))
            .forEach(p -> tableModel.addRow(new Object[]{
                p.getId(), p.getFullName(), p.getAge(), p.getGender(),
                p.getPhone(), p.getDisease(), p.getDoctorName() != null ? p.getDoctorName() : "—"
            }));
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { toast("Please select a patient first."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        allPatients.stream().filter(p -> p.getId() == id).findFirst().ifPresent(this::showPatientDialog);
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        String name = (String) tableModel.getValueAt(row, 1);
        int id = (int) tableModel.getValueAt(row, 0);
        int confirm = JOptionPane.showConfirmDialog(this, "Delete patient: " + name + "?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (confirm == JOptionPane.YES_OPTION) {
            try { patientService.delete(id); } catch (Exception ignored) {}
            allPatients.removeIf(p -> p.getId() == id);
            filterTable(txtSearch.getText());
        }
    }

    private void showPatientDialog(Patient p) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), p == null ? "Add Patient" : "Edit Patient", true);
        dlg.setSize(520, 480);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(BG);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 8, 8, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtName = makeField(p != null ? p.getFullName() : "");
        JTextField txtAge = makeField(p != null ? String.valueOf(p.getAge()) : "");
        JComboBox<String> cbGender = new JComboBox<>(new String[]{"Male", "Female", "Other"});
        if (p != null && p.getGender() != null) cbGender.setSelectedItem(p.getGender());
        styleCombo(cbGender);
        JTextField txtPhone = makeField(p != null ? p.getPhone() : "");
        JTextField txtAddress = makeField(p != null ? p.getAddress() : "");
        JTextField txtDisease = makeField(p != null ? p.getDisease() : "");
        JComboBox<Doctor> cbDoctor = new JComboBox<>();
        styleCombo(cbDoctor);
        cbDoctor.addItem(null);
        try { for (Doctor d : doctorService.getActiveDoctors()) cbDoctor.addItem(d); }
        catch (Exception ignored) { for (Doctor d : MockDataProvider.getDoctors()) cbDoctor.addItem(d); }

        Object[][] fields = {
            {"Full Name *", txtName}, {"Age *", txtAge}, {"Gender", cbGender},
            {"Phone", txtPhone}, {"Address", txtAddress},
            {"Disease / Condition", txtDisease}, {"Assigned Doctor", cbDoctor}
        };

        for (int i = 0; i < fields.length; i++) {
            gbc.gridx = 0; gbc.gridy = i; gbc.weightx = 0.3;
            JLabel lbl = new JLabel((String) fields[i][0]);
            lbl.setForeground(MUTED);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            form.add(lbl, gbc);
            gbc.gridx = 1; gbc.weightx = 0.7;
            form.add((Component) fields[i][1], gbc);
        }

        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        pnlBtns.setBackground(BG);
        JButton btnSave = makeButton(p == null ? "Register Patient" : "Save Changes", GREEN, Color.WHITE);
        JButton btnCancel = makeButton("Cancel", CARD, MUTED);

        JLabel lblErr = new JLabel(" ");
        lblErr.setForeground(RED);
        lblErr.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        // Add error label to form
        gbc.gridx = 0; gbc.gridy = fields.length; gbc.gridwidth = 2; gbc.weightx = 1.0;
        form.add(lblErr, gbc);

        btnSave.addActionListener(e -> {
            String nameVal = txtName.getText().trim();
            String ageVal  = txtAge.getText().trim();
            if (nameVal.isEmpty()) { lblErr.setText("⚠  Patient name is required."); return; }
            if (ageVal.isEmpty())  { lblErr.setText("⚠  Age is required."); return; }

            try {
                Patient t = p == null ? new Patient() : p;
                t.setId(p == null ? 0 : p.getId()); // 0 = new record for DAO insert
                t.setFullName(nameVal);
                t.setAge(Integer.parseInt(ageVal));
                t.setGender((String) cbGender.getSelectedItem());
                t.setPhone(txtPhone.getText().trim());
                t.setAddress(txtAddress.getText().trim());
                t.setDisease(txtDisease.getText().trim());
                Doctor selectedDoc = (Doctor) cbDoctor.getSelectedItem();
                t.setDoctorId(selectedDoc != null ? selectedDoc.getId() : 0);

                // ✅ ACTUALLY SAVE TO MySQL DATABASE
                patientService.save(t);

                dlg.dispose();
                loadData(""); // reload fresh from DB
                JOptionPane.showMessageDialog(null,
                    "✅ Patient \"" + nameVal + "\" " + (p == null ? "registered" : "updated") + " successfully!",
                    "Saved", JOptionPane.INFORMATION_MESSAGE);

            } catch (NumberFormatException ex) {
                lblErr.setText("⚠  Age must be a valid number.");
            } catch (Exception ex) {
                lblErr.setText("❌  " + ex.getMessage());
            }
        });
        btnCancel.addActionListener(e -> dlg.dispose());
        pnlBtns.add(btnCancel);
        pnlBtns.add(btnSave);

        dlg.add(form, BorderLayout.CENTER);
        dlg.add(pnlBtns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }


    // ---- UI Helpers ----
    static JButton makeButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setBackground(bg); btn.setForeground(fg);
        btn.setFont(new Font("Segoe UI", Font.BOLD, 12));
        btn.setFocusPainted(false); btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(BorderFactory.createEmptyBorder(8, 18, 8, 18));
        return btn;
    }

    static JTextField makeField(String text) {
        JTextField f = new JTextField(text);
        f.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        f.setBackground(new Color(30, 41, 59));
        f.setForeground(new Color(226, 232, 240));
        f.setCaretColor(new Color(226, 232, 240));
        f.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(71, 85, 105), 1, true),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        return f;
    }

    static void styleCombo(JComboBox cb) {
        cb.setBackground(new Color(30, 41, 59));
        cb.setForeground(new Color(226, 232, 240));
        cb.setFont(new Font("Segoe UI", Font.PLAIN, 13));
    }

    static void styleTable(JTable table) {
        table.setRowHeight(32);
        table.setShowGrid(false);
        table.setBackground(new Color(30, 41, 59));
        table.setForeground(new Color(226, 232, 240));
        table.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        table.setSelectionBackground(new Color(55, 48, 163));
        table.setIntercellSpacing(new Dimension(0, 2));
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        JTableHeader header = table.getTableHeader();
        header.setBackground(new Color(15, 23, 42));
        header.setForeground(new Color(148, 163, 184));
        header.setFont(new Font("Segoe UI", Font.BOLD, 12));
        header.setPreferredSize(new Dimension(0, 38));
        header.setReorderingAllowed(false);
    }

    static JPanel createTableWrapper(JTable table) {
        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(new Color(30, 41, 59));
        scroll.getViewport().setBackground(new Color(30, 41, 59));
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(new Color(30, 41, 59));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.add(scroll);
        return wrapper;
    }

    private void toast(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Notice", JOptionPane.INFORMATION_MESSAGE);
    }
}
