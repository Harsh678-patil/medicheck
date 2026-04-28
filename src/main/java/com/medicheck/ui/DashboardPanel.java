package com.medicheck.ui;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.math.BigDecimal;
import java.util.Map;
import org.jfree.chart.*;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.category.DefaultCategoryDataset;

public class DashboardPanel extends JPanel {

    private static final Color BG = new Color(15, 23, 42);
    private static final Color CARD_BG = new Color(30, 41, 59);
    private static final Color TEXT_PRIMARY = new Color(226, 232, 240);
    private static final Color TEXT_MUTED = new Color(148, 163, 184);
    private static final Color INDIGO = new Color(99, 102, 241);
    private static final Color TEAL = new Color(20, 184, 166);
    private static final Color RED = new Color(239, 68, 68);
    private static final Color GREEN = new Color(34, 197, 94);
    private static final Color AMBER = new Color(245, 158, 11);

    public DashboardPanel() {
        setLayout(new BorderLayout(15, 15));
        setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        setBackground(BG);
        loadData();
    }

    public void loadData() {
        removeAll();

        // Header
        JLabel lbl = new JLabel("📊  Dashboard Overview");
        lbl.setFont(new Font("Segoe UI", Font.BOLD, 22));
        lbl.setForeground(TEXT_PRIMARY);
        lbl.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(lbl, BorderLayout.NORTH);

        // Cards
        JPanel pnlCards = new JPanel(new GridLayout(1, 4, 15, 0));
        pnlCards.setOpaque(false);

        BigDecimal todaySales = MockDataProvider.getTodaySales();
        BigDecimal monthRev = MockDataProvider.getMonthRevenue();

        pnlCards.add(createCard("👥  Patients", String.valueOf(MockDataProvider.getTotalPatients()), "+12 this month", INDIGO, new Color(79, 70, 229)));
        pnlCards.add(createCard("💊  Medicines", String.valueOf(MockDataProvider.getTotalMedicines()), MockDataProvider.getLowStockCount() + " low stock items", TEAL, new Color(13, 148, 136)));
        pnlCards.add(createCard("⚠  Low Stock", String.valueOf(MockDataProvider.getLowStockCount()), "Action required", RED, new Color(220, 38, 38)));
        pnlCards.add(createCard("💰  Today's Sales", "₹" + String.format("%,.2f", todaySales), "Month: ₹" + String.format("%,.0f", monthRev), GREEN, new Color(22, 163, 74)));

        // Scrollable chart area
        JPanel pnlCharts = new JPanel(new GridLayout(1, 2, 15, 0));
        pnlCharts.setOpaque(false);
        pnlCharts.add(createSalesChart());
        pnlCharts.add(createTopMedicinesChart());

        JPanel pnlCenter = new JPanel(new BorderLayout(0, 15));
        pnlCenter.setOpaque(false);
        pnlCenter.add(pnlCards, BorderLayout.NORTH);
        pnlCenter.add(pnlCharts, BorderLayout.CENTER);

        // Recent activity panel
        pnlCenter.add(createRecentActivity(), BorderLayout.SOUTH);

        add(pnlCenter, BorderLayout.CENTER);
        revalidate(); repaint();
    }

    private JPanel createCard(String title, String value, String sub, Color from, Color to) {
        JPanel card = new JPanel(new BorderLayout(0, 8)) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setPaint(new GradientPaint(0, 0, from, getWidth(), getHeight(), to));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 22, 20, 22));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitle.setForeground(new Color(255, 255, 255, 200));

        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Segoe UI", Font.BOLD, 30));
        lblValue.setForeground(Color.WHITE);

        JLabel lblSub = new JLabel(sub);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(new Color(255, 255, 255, 160));

        card.add(lblTitle, BorderLayout.NORTH);
        card.add(lblValue, BorderLayout.CENTER);
        card.add(lblSub, BorderLayout.SOUTH);
        return card;
    }

    private JPanel createSalesChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        MockDataProvider.getSalesTrend().forEach((d, v) -> dataset.addValue(v, "Sales (₹)", d));

        JFreeChart chart = ChartFactory.createBarChart(
                "Sales Trend - Last 6 Days", "Date", "Amount (₹)", dataset,
                PlotOrientation.VERTICAL, false, true, false);

        chart.setBackgroundPaint(CARD_BG);
        chart.getTitle().setPaint(TEXT_PRIMARY);
        CategoryPlot plot = chart.getCategoryPlot();
        plot.setBackgroundPaint(CARD_BG);
        plot.setRangeGridlinePaint(new Color(55, 65, 81));
        plot.getDomainAxis().setTickLabelPaint(TEXT_MUTED);
        plot.getRangeAxis().setTickLabelPaint(TEXT_MUTED);
        plot.getDomainAxis().setLabelPaint(TEXT_MUTED);
        plot.getRangeAxis().setLabelPaint(TEXT_MUTED);

        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, INDIGO);
        renderer.setBarPainter(new org.jfree.chart.renderer.category.StandardBarPainter());
        renderer.setShadowVisible(false);

        ChartPanel cp = new ChartPanel(chart);
        cp.setBackground(CARD_BG);
        JPanel wrapper = createChartWrapper(cp);
        return wrapper;
    }

    private JPanel createTopMedicinesChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        Map<String, Integer> topMeds = MockDataProvider.getTopMedicines();
        topMeds.forEach(dataset::setValue);

        JFreeChart chart = ChartFactory.createPieChart("Top 5 Medicines Sold", dataset, true, true, false);
        chart.setBackgroundPaint(CARD_BG);
        chart.getTitle().setPaint(TEXT_PRIMARY);
        PiePlot plot = (PiePlot) chart.getPlot();
        plot.setBackgroundPaint(CARD_BG);
        plot.setLabelBackgroundPaint(CARD_BG);
        plot.setLabelOutlinePaint(null);
        plot.setLabelShadowPaint(null);
        plot.setLabelPaint(TEXT_MUTED);
        plot.setOutlineVisible(false);

        Color[] colors = {INDIGO, TEAL, GREEN, AMBER, RED};
        int i = 0;
        for (Object key : dataset.getKeys()) {
            plot.setSectionPaint((Comparable) key, colors[i++ % colors.length]);
        }

        ChartPanel cp = new ChartPanel(chart);
        cp.setBackground(CARD_BG);
        return createChartWrapper(cp);
    }

    private JPanel createChartWrapper(JPanel content) {
        JPanel wrapper = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        wrapper.add(content);
        return wrapper;
    }

    private JPanel createRecentActivity() {
        JPanel panel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_BG);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 16, 16);
                g2.dispose();
            }
        };
        panel.setOpaque(false);
        panel.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JLabel title = new JLabel("📋  Recent Transactions");
        title.setFont(new Font("Segoe UI", Font.BOLD, 14));
        title.setForeground(TEXT_PRIMARY);
        title.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        panel.add(title, BorderLayout.NORTH);

        String[] cols = {"Invoice No", "Patient", "Amount", "Method", "Status", "Time"};
        Object[][] rows = {
            {"INV-2024-010", "Suresh Babu Reddy", "₹1,890.00", "Card", "✅ Paid", "Today, 9:15 AM"},
            {"INV-2024-009", "Deepika Chowdary", "₹345.00", "UPI", "✅ Paid", "Today, 8:45 AM"},
            {"INV-2024-008", "Walk-in Customer", "₹89.00", "Cash", "✅ Paid", "Today, 8:10 AM"},
            {"INV-2024-007", "Lakshmi Narayan Iyer", "₹2,156.00", "Insurance", "✅ Paid", "Yesterday, 5:30 PM"},
            {"INV-2024-006", "Mohammed Farhan Ali", "₹988.00", "Card", "✅ Paid", "Yesterday, 3:00 PM"},
        };
        JTable table = new JTable(rows, cols) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
            @Override public Component prepareRenderer(javax.swing.table.TableCellRenderer r, int row, int col) {
                Component c = super.prepareRenderer(r, row, col);
                c.setBackground(row % 2 == 0 ? CARD_BG : new Color(39, 52, 72));
                c.setForeground(col == 4 ? GREEN : TEXT_PRIMARY);
                return c;
            }
        };
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setBackground(CARD_BG);
        table.setForeground(TEXT_PRIMARY);
        table.getTableHeader().setBackground(new Color(15, 23, 42));
        table.getTableHeader().setForeground(TEXT_MUTED);
        table.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        table.setSelectionBackground(INDIGO);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setPreferredSize(new Dimension(0, 165));
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.setBackground(CARD_BG);
        panel.add(scroll, BorderLayout.CENTER);
        return panel;
    }
}
