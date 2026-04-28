package com.medicheck.ui;

import com.medicheck.model.Prescription;
import com.medicheck.model.PrescriptionItem;
import com.medicheck.service.PrescriptionService;
import com.medicheck.util.PrintUtil;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PrescriptionPanel extends JPanel {

    private static final Color BG = new Color(15, 23, 42);
    private static final Color CARD = new Color(30, 41, 59);
    private static final Color TEXT = new Color(226, 232, 240);
    private static final Color MUTED = new Color(148, 163, 184);
    private static final Color INDIGO = new Color(99, 102, 241);
    private static final Color GREEN = new Color(34, 197, 94);
    private static final Color AMBER = new Color(245, 158, 11);
    private static final Color TEAL = new Color(20, 184, 166);
    private static final Color DANGER = new Color(239, 68, 68);

    private final PrescriptionService prescriptionService = new PrescriptionService();
    private JTable table;
    private DefaultTableModel tableModel;
    private List<Prescription> allPrescriptions;
    private JTextField txtSearch;

    public PrescriptionPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(BG);
        initUI();
        loadData();
    }

    public void loadData() {
        allPrescriptions = new ArrayList<>();
        try {
            allPrescriptions = prescriptionService.getAllPrescriptions();
        } catch (Exception ignored) {
        }
        if (allPrescriptions.isEmpty()) {
            allPrescriptions = MockDataProvider.getPrescriptions();
        }
        filterTable(txtSearch != null ? txtSearch.getText() : "");
    }

    private void initUI() {
        JPanel pnlHeader = new JPanel(new BorderLayout(10, 0));
        pnlHeader.setOpaque(false);

        JLabel lblTitle = new JLabel("Prescription Management");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT);
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlRight.setOpaque(false);

        txtSearch = PatientPanel.makeField("");
        txtSearch.setPreferredSize(new Dimension(220, 34));
        txtSearch.putClientProperty("JTextField.placeholderText", "Search patient, doctor, disease...");
        txtSearch.addKeyListener(new KeyAdapter() {
            @Override public void keyReleased(KeyEvent e) {
                filterTable(txtSearch.getText());
            }
        });

        JButton btnRefresh = PatientPanel.makeButton("Refresh", INDIGO, Color.WHITE);
        btnRefresh.addActionListener(e -> loadData());
        pnlRight.add(txtSearch);
        pnlRight.add(btnRefresh);
        pnlHeader.add(pnlRight, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        String[] cols = {"#", "Rx Number", "Patient Name", "Doctor", "Diagnosis", "Status", "Date"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return false;
            }
        };

        table = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(row % 2 == 0 ? CARD : new Color(39, 52, 72));
                if (isRowSelected(row)) {
                    c.setBackground(new Color(55, 48, 163));
                }
                if (col == 5) {
                    String s = String.valueOf(tableModel.getValueAt(row, 5));
                    c.setForeground("Active".equals(s) ? GREEN : "Dispensed".equals(s) ? TEAL : DANGER);
                } else {
                    c.setForeground(TEXT);
                }
                return c;
            }
        };
        PatientPanel.styleTable(table);
        table.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2 && table.getSelectedRow() >= 0) {
                    openSelectedPrescription();
                }
            }
        });

        JPanel pnlActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 8));
        pnlActions.setBackground(new Color(23, 32, 49));
        JButton btnOpen = PatientPanel.makeButton("Open Prescription", AMBER, Color.WHITE);
        JButton btnDispense = PatientPanel.makeButton("Mark Dispensed", GREEN, Color.WHITE);
        JButton btnPrint = PatientPanel.makeButton("Print Rx", INDIGO, Color.WHITE);
        btnOpen.addActionListener(e -> openSelectedPrescription());
        btnDispense.addActionListener(e -> markDispensed());
        btnPrint.addActionListener(e -> printSelected());
        pnlActions.add(btnOpen);
        pnlActions.add(btnDispense);
        pnlActions.add(btnPrint);

        JPanel center = new JPanel(new BorderLayout());
        center.setOpaque(false);
        center.add(PatientPanel.createTableWrapper(table), BorderLayout.CENTER);
        center.add(pnlActions, BorderLayout.SOUTH);
        add(center, BorderLayout.CENTER);

        JPanel bar = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 8));
        bar.setBackground(new Color(23, 32, 49));
        bar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(51, 65, 85)));
        statusPill(bar, "Active", GREEN);
        statusPill(bar, "Dispensed", TEAL);
        statusPill(bar, "Expired", DANGER);
        add(bar, BorderLayout.SOUTH);
    }

    private void statusPill(JPanel bar, String text, Color color) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(color);
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 12));
        bar.add(lbl);
    }

    private void filterTable(String kw) {
        tableModel.setRowCount(0);
        String k = kw == null ? "" : kw.trim().toLowerCase();
        allPrescriptions.stream()
                .filter(p -> k.isEmpty()
                        || safeText(p.getPatientName()).toLowerCase().contains(k)
                        || safeText(p.getDoctorName()).toLowerCase().contains(k)
                        || safeText(p.getDisease()).toLowerCase().contains(k)
                        || safeText(p.getPrescriptionNo()).toLowerCase().contains(k))
                .forEach(p -> tableModel.addRow(new Object[]{
                        p.getId(),
                        safeText(p.getPrescriptionNo()),
                        safeText(p.getPatientName()),
                        safeText(p.getDoctorName()),
                        safeText(p.getDisease()),
                        safeText(p.getStatus()),
                        p.getCreatedAt() != null ? p.getCreatedAt().toLocalDate() : "-"
                }));
    }

    private void markDispensed() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a prescription first.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        try {
            prescriptionService.markDispensed(id);
        } catch (Exception ignored) {
        }
        allPrescriptions.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .ifPresent(p -> p.setStatus("Dispensed"));
        filterTable(txtSearch.getText());
    }

    private void printSelected() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a prescription first.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        Prescription prescription = findPrescription(id);
        if (prescription == null) {
            JOptionPane.showMessageDialog(this, "Prescription not found.");
            return;
        }
        try {
            PrintUtil.printPrescription(prescription, loadPrescriptionItems(id, prescription));
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Could not print prescription.\n" + ex.getMessage());
        }
    }

    private void openSelectedPrescription() {
        int row = table.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Please select a prescription first.");
            return;
        }
        int id = (int) tableModel.getValueAt(row, 0);
        Prescription prescription = findPrescription(id);
        if (prescription == null) {
            JOptionPane.showMessageDialog(this, "Prescription not found.");
            return;
        }
        showPrescriptionDialog(prescription, loadPrescriptionItems(id, prescription));
    }

    private Prescription findPrescription(int id) {
        return allPrescriptions.stream()
                .filter(p -> p.getId() == id)
                .findFirst()
                .orElse(null);
    }

    private List<PrescriptionItem> loadPrescriptionItems(int id, Prescription prescription) {
        List<PrescriptionItem> items;
        try {
            items = prescriptionService.getItems(id);
        } catch (Exception ex) {
            items = Collections.emptyList();
        }
        if (items.isEmpty() && prescription.getItems() != null) {
            items = prescription.getItems();
        }
        return items;
    }

    private void showPrescriptionDialog(Prescription prescription, List<PrescriptionItem> items) {
        JDialog dialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Prescription Details", true);
        dialog.setSize(760, 600);
        dialog.setLocationRelativeTo(this);
        dialog.getContentPane().setBackground(BG);
        dialog.setLayout(new BorderLayout(12, 12));

        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(23, 32, 49));
        header.setBorder(BorderFactory.createEmptyBorder(16, 18, 16, 18));

        JLabel title = new JLabel("Prescription " + safeText(prescription.getPrescriptionNo()));
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(TEXT);
        header.add(title, BorderLayout.WEST);

        JLabel status = new JLabel(safeText(prescription.getStatus()));
        status.setFont(new Font("Segoe UI", Font.BOLD, 13));
        status.setForeground("Dispensed".equalsIgnoreCase(prescription.getStatus()) ? TEAL : GREEN);
        header.add(status, BorderLayout.EAST);

        JPanel body = new JPanel(new BorderLayout(0, 12));
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(12, 18, 12, 18));

        JPanel meta = new JPanel(new GridLayout(0, 2, 14, 10));
        meta.setOpaque(false);
        addDetail(meta, "Patient", safeText(prescription.getPatientName()));
        addDetail(meta, "Doctor", safeText(prescription.getDoctorName()));
        addDetail(meta, "Diagnosis", safeText(prescription.getDisease()));
        addDetail(meta, "Date", prescription.getCreatedAt() != null ? prescription.getCreatedAt().toLocalDate().toString() : "-");
        addDetail(meta, "Symptoms", safeText(prescription.getSymptoms()));
        addDetail(meta, "Notes", safeText(prescription.getNotes()));
        body.add(meta, BorderLayout.NORTH);

        String[] itemCols = {"Medicine", "Dosage", "Frequency", "Duration", "Qty", "Notes"};
        DefaultTableModel itemModel = new DefaultTableModel(itemCols, 0) {
            @Override public boolean isCellEditable(int r, int c) {
                return false;
            }
        };
        for (PrescriptionItem item : items) {
            itemModel.addRow(new Object[] {
                    safeText(item.getMedicineName()),
                    safeText(item.getDosage()),
                    safeText(item.getFrequency()),
                    safeText(item.getDuration()),
                    item.getQuantity(),
                    safeText(item.getNotes())
            });
        }

        JTable itemTable = new JTable(itemModel);
        PatientPanel.styleTable(itemTable);
        itemTable.setRowHeight(28);
        body.add(PatientPanel.createTableWrapper(itemTable), BorderLayout.CENTER);

        JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        actions.setBackground(new Color(23, 32, 49));
        JButton btnClose = PatientPanel.makeButton("Close", CARD, TEXT);
        JButton btnPrint = PatientPanel.makeButton("Print Rx", INDIGO, Color.WHITE);
        btnClose.addActionListener(e -> dialog.dispose());
        btnPrint.addActionListener(e -> PrintUtil.printPrescription(prescription, items));
        actions.add(btnClose);
        actions.add(btnPrint);

        dialog.add(header, BorderLayout.NORTH);
        dialog.add(body, BorderLayout.CENTER);
        dialog.add(actions, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private void addDetail(JPanel panel, String label, String value) {
        JPanel box = new JPanel(new BorderLayout(0, 4));
        box.setOpaque(false);

        JLabel lbl = new JLabel(label);
        lbl.setForeground(MUTED);
        lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        JTextArea txt = new JTextArea(value);
        txt.setWrapStyleWord(true);
        txt.setLineWrap(true);
        txt.setEditable(false);
        txt.setOpaque(false);
        txt.setForeground(TEXT);
        txt.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        txt.setBorder(null);

        box.add(lbl, BorderLayout.NORTH);
        box.add(txt, BorderLayout.CENTER);
        panel.add(box);
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "-" : value;
    }
}
