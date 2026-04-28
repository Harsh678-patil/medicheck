package com.medicheck.ui;

import com.medicheck.model.Medicine;
import com.medicheck.service.MedicineService;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class MedicinePanel extends JPanel {

    private static final Color BG = new Color(15, 23, 42);
    private static final Color CARD = new Color(30, 41, 59);
    private static final Color TEXT = new Color(226, 232, 240);
    private static final Color MUTED = new Color(148, 163, 184);
    private static final Color INDIGO = new Color(99, 102, 241);
    private static final Color GREEN = new Color(34, 197, 94);
    private static final Color RED = new Color(239, 68, 68);
    private static final Color AMBER = new Color(245, 158, 11);

    private final MedicineService medicineService = new MedicineService();
    private JTable table;
    private DefaultTableModel tableModel;
    private JTextField txtSearch;
    private List<Medicine> allMedicines;

    public MedicinePanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(BG);
        initUI();
        loadData();
    }

    private void initUI() {
        // Header
        JPanel pnlHeader = new JPanel(new BorderLayout(10, 0));
        pnlHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("💊  Medicine Inventory");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT);
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlRight.setOpaque(false);
        txtSearch = PatientPanel.makeField("");
        txtSearch.setPreferredSize(new Dimension(220, 34));
        txtSearch.putClientProperty("JTextField.placeholderText", "🔍  Search name, category, barcode...");
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) { filterTable(txtSearch.getText()); }
        });
        JButton btnAdd = PatientPanel.makeButton("+ Add Medicine", GREEN, Color.WHITE);
        btnAdd.addActionListener(e -> showMedicineDialog(null));
        JButton btnScan = PatientPanel.makeButton("📷 Scan", INDIGO, Color.WHITE);
        btnScan.addActionListener(e -> JOptionPane.showMessageDialog(this, "Barcode scanner requires a webcam.\nConnect USB camera to scan."));

        pnlRight.add(txtSearch); pnlRight.add(btnScan); pnlRight.add(btnAdd);
        pnlHeader.add(pnlRight, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        // Summary Cards
        JPanel pnlCards = new JPanel(new GridLayout(1, 4, 12, 0));
        pnlCards.setOpaque(false);
        pnlCards.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        pnlCards.add(miniCard("Total Items", "20", INDIGO));
        pnlCards.add(miniCard("In Stock", "17", GREEN));
        pnlCards.add(miniCard("Low Stock ⚠", "3", AMBER));
        pnlCards.add(miniCard("Expiring Soon", "2", RED));

        // Table
        String[] cols = {"ID", "Medicine Name", "Generic Name", "Category", "Price (₹)", "Stock", "Expiry", "Status"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(row % 2 == 0 ? CARD : new Color(39, 52, 72));
                if (isRowSelected(row)) c.setBackground(new Color(55, 48, 163));
                Object status = tableModel.getValueAt(row, 7);
                if (col == 7) {
                    String s = status != null ? status.toString() : "";
                    if (s.contains("Low")) c.setForeground(AMBER);
                    else if (s.contains("OK")) c.setForeground(GREEN);
                    else if (s.contains("Out")) c.setForeground(RED);
                    else c.setForeground(TEXT);
                } else {
                    c.setForeground(TEXT);
                }
                return c;
            }
        };
        PatientPanel.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(60);

        // Actions
        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        pnlActions.setBackground(new Color(23, 32, 49));
        JButton btnEdit = PatientPanel.makeButton("✏ Edit", INDIGO, Color.WHITE);
        JButton btnRestock = PatientPanel.makeButton("📦 Restock", new Color(5, 150, 105), Color.WHITE);
        JButton btnDelete = PatientPanel.makeButton("🗑 Remove", RED, Color.WHITE);
        btnEdit.addActionListener(e -> editSelected());
        btnRestock.addActionListener(e -> restockSelected());
        btnDelete.addActionListener(e -> deleteSelected());
        pnlActions.add(btnRestock); pnlActions.add(btnEdit); pnlActions.add(btnDelete);

        JPanel tableWrapper = PatientPanel.createTableWrapper(table);
        JPanel centerPanel = new JPanel(new BorderLayout(0, 0));
        centerPanel.setOpaque(false);
        centerPanel.add(pnlCards, BorderLayout.NORTH);
        centerPanel.add(tableWrapper, BorderLayout.CENTER);
        centerPanel.add(pnlActions, BorderLayout.SOUTH);
        add(centerPanel, BorderLayout.CENTER);
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
        c.add(lbl, BorderLayout.NORTH); c.add(val, BorderLayout.CENTER);
        return c;
    }

    private void loadData() {
        allMedicines = new ArrayList<>();
        try { allMedicines = medicineService.getAllMedicines(); } catch (Exception ignored) {}
        if (allMedicines.isEmpty()) allMedicines = MockDataProvider.getMedicines();
        filterTable("");
    }

    private void filterTable(String keyword) {
        tableModel.setRowCount(0);
        String kw = keyword.toLowerCase();
        allMedicines.stream()
            .filter(m -> kw.isEmpty()
                || m.getName().toLowerCase().contains(kw)
                || (m.getCategory() != null && m.getCategory().toLowerCase().contains(kw))
                || (m.getGenericName() != null && m.getGenericName().toLowerCase().contains(kw))
                || (m.getBarcode() != null && m.getBarcode().contains(kw)))
            .forEach(m -> {
                String status = m.getQuantity() == 0 ? "⛔ Out of Stock"
                        : m.getQuantity() <= m.getReorderLevel() ? "⚠ Low Stock" : "✅ OK";
                tableModel.addRow(new Object[]{
                    m.getId(), m.getName(), m.getGenericName(), m.getCategory(),
                    "₹" + m.getPrice(),
                    m.getQuantity(),
                    m.getExpiryDate() != null ? m.getExpiryDate().toString() : "—",
                    status
                });
            });
    }

    private void editSelected() {
        int row = table.getSelectedRow(); if (row < 0) return;
        int id = (int) tableModel.getValueAt(row, 0);
        allMedicines.stream().filter(m -> m.getId() == id).findFirst().ifPresent(this::showMedicineDialog);
    }

    private void restockSelected() {
        int row = table.getSelectedRow(); if (row < 0) return;
        int id = (int) tableModel.getValueAt(row, 0);
        allMedicines.stream().filter(m -> m.getId() == id).findFirst().ifPresent(m -> {
            String qtyStr = JOptionPane.showInputDialog(this, "New stock quantity for " + m.getName() + ":", m.getQuantity());
            if (qtyStr != null) {
                try {
                    m.setQuantity(Integer.parseInt(qtyStr.trim()));
                    try { medicineService.adjustStock(id, m.getQuantity(), "Manual restock"); } catch (Exception ignored) {}
                    filterTable(txtSearch.getText());
                } catch (NumberFormatException ex) { JOptionPane.showMessageDialog(this, "Invalid number."); }
            }
        });
    }

    private void deleteSelected() {
        int row = table.getSelectedRow(); if (row < 0) return;
        int id = (int) tableModel.getValueAt(row, 0);
        String name = (String) tableModel.getValueAt(row, 1);
        if (JOptionPane.showConfirmDialog(this, "Remove " + name + " from inventory?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            allMedicines.removeIf(m -> m.getId() == id);
            filterTable(txtSearch.getText());
        }
    }

    private void showMedicineDialog(Medicine m) {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), m == null ? "Add Medicine" : "Edit Medicine", true);
        dlg.setSize(540, 520);
        dlg.setLocationRelativeTo(this);
        dlg.getContentPane().setBackground(BG);
        dlg.setLayout(new BorderLayout());

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(BG);
        form.setBorder(BorderFactory.createEmptyBorder(20, 25, 10, 25));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(7, 8, 7, 8);
        gbc.fill = GridBagConstraints.HORIZONTAL;

        JTextField txtName = PatientPanel.makeField(m != null ? m.getName() : "");
        JTextField txtGeneric = PatientPanel.makeField(m != null ? (m.getGenericName() != null ? m.getGenericName() : "") : "");
        JTextField txtCategory = PatientPanel.makeField(m != null ? m.getCategory() : "");
        JTextField txtBarcode = PatientPanel.makeField(m != null ? (m.getBarcode() != null ? m.getBarcode() : "") : "");
        JTextField txtPrice = PatientPanel.makeField(m != null ? m.getPrice().toString() : "");
        JTextField txtReorder = PatientPanel.makeField(m != null ? String.valueOf(m.getReorderLevel()) : "10");
        JTextField txtExpiry = PatientPanel.makeField(m != null && m.getExpiryDate() != null ? m.getExpiryDate().toString() : "YYYY-MM-DD");
        JTextField txtMfr = PatientPanel.makeField(m != null ? (m.getManufacturer() != null ? m.getManufacturer() : "") : "");

        Object[][] fields = {
            {"Medicine Name *", txtName}, {"Generic Name", txtGeneric},
            {"Category", txtCategory}, {"Barcode", txtBarcode},
            {"Selling Price (₹) *", txtPrice}, {"Reorder Level", txtReorder},
            {"Expiry Date", txtExpiry}, {"Manufacturer", txtMfr}
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
        JButton btnSave = PatientPanel.makeButton("Save Medicine", GREEN, Color.WHITE);
        JButton btnCancel = PatientPanel.makeButton("Cancel", CARD, MUTED);
        btnSave.addActionListener(e -> {
            try {
                Medicine t = m == null ? new Medicine() : m;
                t.setName(txtName.getText());
                t.setGenericName(txtGeneric.getText());
                t.setCategory(txtCategory.getText());
                t.setBarcode(txtBarcode.getText());
                t.setPrice(new BigDecimal(txtPrice.getText().trim()));
                t.setReorderLevel(Integer.parseInt(txtReorder.getText().trim()));
                String exp = txtExpiry.getText().trim();
                if (!exp.isEmpty() && !exp.equals("YYYY-MM-DD")) t.setExpiryDate(LocalDate.parse(exp));
                t.setManufacturer(txtMfr.getText());
                t.setActive(true);
                if (m == null) { t.setId(allMedicines.size() + 1); t.setQuantity(0); allMedicines.add(t); }
                try { medicineService.save(t); } catch (Exception ignored) {}
                dlg.dispose();
                filterTable(txtSearch.getText());
            } catch (Exception ex) { JOptionPane.showMessageDialog(dlg, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        });
        btnCancel.addActionListener(e -> dlg.dispose());
        pnlBtns.add(btnCancel); pnlBtns.add(btnSave);
        dlg.add(form, BorderLayout.CENTER);
        dlg.add(pnlBtns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}
