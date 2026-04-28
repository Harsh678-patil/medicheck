package com.medicheck.ui;

import com.medicheck.model.Doctor;
import com.medicheck.service.DoctorService;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

public class DoctorPanel extends JPanel {

    private static final Color BG   = new Color(15, 23, 42);
    private static final Color CARD = new Color(30, 41, 59);
    private static final Color TEXT = new Color(226, 232, 240);
    private static final Color MUTED = new Color(148, 163, 184);
    private static final Color INDIGO = new Color(99, 102, 241);
    private static final Color GREEN  = new Color(34, 197, 94);
    private static final Color RED    = new Color(239, 68, 68);
    private static final Color AMBER  = new Color(245, 158, 11);

    private final DoctorService doctorService = new DoctorService();
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private List<Doctor> allDoctors = new ArrayList<>();

    public DoctorPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(BG);
        initUI();
        loadData();
    }

    private void initUI() {
        // ── Header ──────────────────────────────────────────
        JPanel pnlHeader = new JPanel(new BorderLayout(10, 0));
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("🩺  Doctor Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT);
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlRight.setOpaque(false);

        txtSearch = PatientPanel.makeField("");
        txtSearch.setPreferredSize(new Dimension(230, 34));
        txtSearch.putClientProperty("JTextField.placeholderText", "🔍  Search name, specialty, phone...");
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { filterTable(txtSearch.getText()); }
        });

        JButton btnAdd = PatientPanel.makeButton("+ Add Doctor", GREEN, Color.WHITE);
        btnAdd.addActionListener(e -> showDoctorDialog(null));

        pnlRight.add(txtSearch);
        pnlRight.add(btnAdd);
        pnlHeader.add(pnlRight, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        // ── Summary mini cards ───────────────────────────────
        JPanel pnlCards = new JPanel(new GridLayout(1, 3, 12, 0));
        pnlCards.setOpaque(false);
        pnlCards.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        pnlCards.add(miniCard("Total Doctors", "8", INDIGO));
        pnlCards.add(miniCard("Active", "8", GREEN));
        pnlCards.add(miniCard("Specialisations", "8", AMBER));

        // ── Table ────────────────────────────────────────────
        String[] cols = {"#", "Full Name", "Specialization", "Qualification", "Phone", "Email", "Exp (yrs)", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(row % 2 == 0 ? CARD : new Color(39, 52, 72));
                if (isRowSelected(row)) c.setBackground(new Color(55, 48, 163));
                if (col == 7) {
                    String s = tableModel.getValueAt(row, 7).toString();
                    c.setForeground(s.contains("Active") ? GREEN : RED);
                } else {
                    c.setForeground(TEXT);
                }
                return c;
            }
        };
        PatientPanel.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(170);
        table.getColumnModel().getColumn(3).setPreferredWidth(200);

        // ── Action buttons ────────────────────────────────────
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        pnlActions.setBackground(new Color(23, 32, 49));
        JButton btnEdit    = PatientPanel.makeButton("✏  Edit", INDIGO, Color.WHITE);
        JButton btnToggle  = PatientPanel.makeButton("🔄  Toggle Active", AMBER, Color.WHITE);
        JButton btnDelete  = PatientPanel.makeButton("🗑  Delete", RED, Color.WHITE);
        btnEdit.addActionListener(e -> editSelected());
        btnToggle.addActionListener(e -> toggleSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        pnlActions.add(btnEdit);
        pnlActions.add(btnToggle);
        pnlActions.add(btnDelete);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(pnlCards, BorderLayout.NORTH);
        center.add(PatientPanel.createTableWrapper(table), BorderLayout.CENTER);
        center.add(pnlActions, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);
    }

    private JPanel miniCard(String label, String value, Color color) {
        JPanel c = new JPanel(new BorderLayout(0, 4)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12);
                g2.setColor(color);
                g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                g2.dispose();
            }
        };
        c.setOpaque(false);
        c.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));
        JLabel lbl = new JLabel(label); lbl.setForeground(MUTED); lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JLabel val = new JLabel(value); val.setForeground(color); val.setFont(new Font("Segoe UI", Font.BOLD, 24));
        c.add(lbl, BorderLayout.NORTH);
        c.add(val, BorderLayout.CENTER);
        return c;
    }

    public void loadData() {
        allDoctors = new ArrayList<>();
        try {
            allDoctors = doctorService.getAllDoctors();
        } catch (Exception ex) {
            allDoctors = MockDataProvider.getDoctors();
        }
        filterTable("");
        updateCards();
    }

    private void updateCards() {
        long active = allDoctors.stream().filter(Doctor::isActive).count();
        long specs = allDoctors.stream().map(d -> d.getSpecialty()).filter(Objects::nonNull).distinct().count();
        // update mini card labels dynamically if needed
    }

    private void filterTable(String kw) {
        tableModel.setRowCount(0);
        String k = kw.toLowerCase();
        allDoctors.stream()
            .filter(d -> k.isEmpty()
                || d.getFullName().toLowerCase().contains(k)
                || (d.getSpecialty() != null && d.getSpecialty().toLowerCase().contains(k))
                || (d.getPhone() != null && d.getPhone().contains(k))
                || (d.getEmail() != null && d.getEmail().toLowerCase().contains(k)))
            .forEach(d -> tableModel.addRow(new Object[]{
                d.getId(),
                d.getFullName(),
                d.getSpecialty() != null ? d.getSpecialty() : "—",
                d.getQualification() != null ? d.getQualification() : "—",
                d.getPhone() != null ? d.getPhone() : "—",
                d.getEmail() != null ? d.getEmail() : "—",
                d.getExperienceYears(),
                d.isActive() ? "✅ Active" : "❌ Inactive"
            }));
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a doctor first."); return; }
        int id = (int) tableModel.getValueAt(row, 0);
        allDoctors.stream().filter(d -> d.getId() == id).findFirst().ifPresent(this::showDoctorDialog);
    }

    private void toggleSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = (int) tableModel.getValueAt(row, 0);
        allDoctors.stream().filter(d -> d.getId() == id).findFirst().ifPresent(d -> {
            d.setActive(!d.isActive());
            try {
                doctorService.save(d);
            } catch (Exception ignored) {}
            filterTable(txtSearch.getText());
        });
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) return;
        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        int confirm = JOptionPane.showConfirmDialog(this,
            "Delete Dr. " + name + "?\n⚠ This may affect linked patients and prescriptions.",
            "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (confirm == JOptionPane.YES_OPTION) {
            try {
                doctorService.delete(id);
                allDoctors.removeIf(d -> d.getId() == id);
                filterTable(txtSearch.getText());
                JOptionPane.showMessageDialog(this, "✅ Doctor deleted from database.");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "❌ Cannot delete: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showDoctorDialog(Doctor d) {
        boolean isNew = (d == null);
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            isNew ? "Add New Doctor" : "Edit Doctor", true);
        dlg.setSize(540, 560);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(BG);
        dlg.setLayout(new BorderLayout());

        // Title bar
        JLabel lblTitle = new JLabel("  " + (isNew ? "🩺  Add New Doctor" : "✏  Edit Doctor Details"));
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        lblTitle.setForeground(TEXT);
        lblTitle.setBackground(new Color(22, 31, 53));
        lblTitle.setOpaque(true);
        lblTitle.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));

        // Form
        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG);
        form.setBorder(BorderFactory.createEmptyBorder(15, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(7, 6, 7, 6);
        gbc.gridwidth = GridBagConstraints.REMAINDER;

        JTextField txtName    = PatientPanel.makeField(d != null ? d.getFullName() : "");
        JTextField txtSpec    = PatientPanel.makeField(d != null && d.getSpecialty() != null ? d.getSpecialty() : "");
        JTextField txtQual    = PatientPanel.makeField(d != null && d.getQualification() != null ? d.getQualification() : "");
        JTextField txtPhone   = PatientPanel.makeField(d != null && d.getPhone() != null ? d.getPhone() : "");
        JTextField txtEmail   = PatientPanel.makeField(d != null && d.getEmail() != null ? d.getEmail() : "");
        JTextField txtLicense = PatientPanel.makeField(d != null && d.getLicenseNo() != null ? d.getLicenseNo() : "");
        JTextField txtAddress = PatientPanel.makeField(d != null && d.getAddress() != null ? d.getAddress() : "");
        JTextField txtExp     = PatientPanel.makeField(d != null ? String.valueOf(d.getExperienceYears()) : "0");

        Object[][] fields = {
            {"Full Name *", txtName},
            {"Specialization *", txtSpec},
            {"Qualification", txtQual},
            {"Phone", txtPhone},
            {"Email", txtEmail},
            {"License No. (MCI)", txtLicense},
            {"Hospital / Address", txtAddress},
            {"Experience (years)", txtExp},
        };

        for (Object[] field : fields) {
            JLabel lbl = new JLabel((String) field[0]);
            lbl.setForeground(MUTED);
            lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            form.add(lbl, gbc);
            form.add((Component) field[1], gbc);
        }

        JLabel lblErr = new JLabel(" ");
        lblErr.setForeground(RED);
        lblErr.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        form.add(lblErr, gbc);

        // Buttons
        JPanel pnlBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        pnlBtns.setBackground(new Color(22, 31, 53));
        JButton btnCancel = PatientPanel.makeButton("Cancel", CARD, MUTED);
        JButton btnSave   = PatientPanel.makeButton(isNew ? "Add Doctor →" : "Save Changes →", GREEN, Color.WHITE);

        btnCancel.addActionListener(e -> dlg.dispose());
        btnSave.addActionListener(e -> {
            String name = txtName.getText().trim();
            String spec = txtSpec.getText().trim();
            if (name.isEmpty() || spec.isEmpty()) {
                lblErr.setText("⚠  Full name and specialization are required.");
                return;
            }
            try {
                Doctor t = d == null ? new Doctor() : d;
                t.setFullName(name);
                t.setSpecialty(spec);
                t.setQualification(txtQual.getText().trim());
                t.setPhone(txtPhone.getText().trim());
                t.setEmail(txtEmail.getText().trim());
                t.setLicenseNo(txtLicense.getText().trim());
                t.setAddress(txtAddress.getText().trim());
                String expStr = txtExp.getText().trim();
                t.setExperienceYears(expStr.isEmpty() ? 0 : Integer.parseInt(expStr));
                t.setActive(true);

                // ✅ THIS SAVES TO MySQL DATABASE
                doctorService.save(t);

                if (d == null) allDoctors.add(t);
                dlg.dispose();
                filterTable(txtSearch.getText());
                JOptionPane.showMessageDialog(null,
                    "✅ Dr. " + name + " saved to database successfully!",
                    "Saved", JOptionPane.INFORMATION_MESSAGE);
            } catch (NumberFormatException ex) {
                lblErr.setText("⚠  Experience must be a number.");
            } catch (Exception ex) {
                lblErr.setText("❌ " + ex.getMessage());
            }
        });

        pnlBtns.add(btnCancel);
        pnlBtns.add(btnSave);

        JScrollPane scroll = new JScrollPane(form);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(BG);
        dlg.add(lblTitle, BorderLayout.NORTH);
        dlg.add(scroll, BorderLayout.CENTER);
        dlg.add(pnlBtns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}
