package com.medicheck.ui;

import com.medicheck.model.Sale;
import com.medicheck.service.ReportService;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import java.util.List;

public class ReportsPanel extends JPanel {

    private static final Color BG = new Color(15, 23, 42);
    private static final Color CARD = new Color(30, 41, 59);
    private static final Color TEXT = new Color(226, 232, 240);
    private static final Color MUTED = new Color(148, 163, 184);
    private static final Color INDIGO = new Color(99, 102, 241);
    private static final Color GREEN = new Color(34, 197, 94);
    private static final Color AMBER = new Color(245, 158, 11);

    private final ReportService reportService = new ReportService();
    private JTable table;
    private DefaultTableModel tableModel;

    public ReportsPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(BG);
        initUI();
        loadDailySales();
    }

    private void initUI() {
        JPanel pnlHeader = new JPanel(new BorderLayout(10, 0));
        pnlHeader.setOpaque(false);
        JLabel lblTitle = new JLabel("📈  Sales Reports");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lblTitle.setForeground(TEXT);
        pnlHeader.add(lblTitle, BorderLayout.WEST);

        JPanel pnlRight = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        pnlRight.setOpaque(false);
        JButton btnToday = PatientPanel.makeButton("Today", INDIGO, Color.WHITE);
        JButton btnMonth = PatientPanel.makeButton("This Month", new Color(5, 150, 105), Color.WHITE);
        JButton btnExport = PatientPanel.makeButton("⬇ Export CSV", CARD, MUTED);
        btnToday.addActionListener(e -> loadDailySales());
        btnMonth.addActionListener(e -> loadMonthlySales());
        btnExport.addActionListener(e -> JOptionPane.showMessageDialog(this, "CSV export requires full DB connection."));
        pnlRight.add(btnToday); pnlRight.add(btnMonth); pnlRight.add(btnExport);
        pnlHeader.add(pnlRight, BorderLayout.EAST);
        add(pnlHeader, BorderLayout.NORTH);

        // Revenue Summary Cards
        JPanel pnlCards = new JPanel(new GridLayout(1, 3, 12, 0));
        pnlCards.setOpaque(false);
        pnlCards.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
        pnlCards.add(revCard("Today's Revenue", "₹" + String.format("%,.2f", MockDataProvider.getTodaySales()), GREEN));
        pnlCards.add(revCard("Monthly Revenue", "₹" + String.format("%,.0f", MockDataProvider.getMonthRevenue()), INDIGO));
        pnlCards.add(revCard("Total Transactions", "1,247", AMBER));

        String[] cols = {"Invoice No", "Patient Name", "Cashier", "Amount (₹)", "Method", "Status", "Date & Time"};
        tableModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(tableModel) {
            @Override public Component prepareRenderer(TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(row % 2 == 0 ? CARD : new Color(39, 52, 72));
                if (isRowSelected(row)) c.setBackground(new Color(55, 48, 163));
                c.setForeground(col == 4 ? new Color(34, 197, 94) : TEXT);
                return c;
            }
        };
        PatientPanel.styleTable(table);

        JPanel center = new JPanel(new BorderLayout(0, 0));
        center.setOpaque(false);
        center.add(pnlCards, BorderLayout.NORTH);
        center.add(PatientPanel.createTableWrapper(table), BorderLayout.CENTER);
        add(center, BorderLayout.CENTER);
    }

    private JPanel revCard(String label, String value, Color color) {
        JPanel c = new JPanel(new BorderLayout(0, 5)) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 14, 14);
                g2.dispose();
            }
        };
        c.setOpaque(false);
        c.setBorder(BorderFactory.createEmptyBorder(16, 20, 16, 20));
        JLabel lbl = new JLabel(label); lbl.setForeground(MUTED); lbl.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JLabel val = new JLabel(value); val.setForeground(color); val.setFont(new Font("Segoe UI", Font.BOLD, 24));
        c.add(lbl, BorderLayout.NORTH); c.add(val, BorderLayout.CENTER);
        return c;
    }

    private void loadDailySales() {
        tableModel.setRowCount(0);
        List<Sale> sales = new ArrayList<>();
        try { sales = reportService.getDailySalesReport(LocalDate.now()); } catch (Exception ignored) {}
        if (sales.isEmpty()) sales = MockDataProvider.getSales();
        for (Sale s : sales) {
            tableModel.addRow(new Object[]{
                s.getInvoiceNo(), s.getPatientName(), s.getCashierName(),
                "₹" + String.format("%,.2f", s.getTotal()),
                s.getPaymentMethod(), "✅ " + s.getPaymentStatus(),
                s.getCreatedAt() != null ? s.getCreatedAt() : "Today"
            });
        }
    }

    private void loadMonthlySales() {
        tableModel.setRowCount(0);
        List<Sale> sales = MockDataProvider.getSales();
        for (Sale s : sales) {
            tableModel.addRow(new Object[]{
                s.getInvoiceNo(), s.getPatientName(), s.getCashierName(),
                "₹" + String.format("%,.2f", s.getTotal()),
                s.getPaymentMethod(), "✅ " + s.getPaymentStatus(),
                s.getCreatedAt()
            });
        }
    }
}
