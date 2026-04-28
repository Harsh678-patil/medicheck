package com.medicheck.util;

import com.medicheck.config.AppConfig;
import com.medicheck.model.Sale;
import com.medicheck.model.SaleItem;
import com.medicheck.model.Prescription;
import com.medicheck.model.PrescriptionItem;

import javax.swing.*;
import java.awt.*;
import java.awt.print.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Handles print operations for invoices and prescriptions using Java2D printing.
 */
public final class PrintUtil {

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd-MMM-yyyy HH:mm");
    private static final AppConfig config = AppConfig.getInstance();

    private PrintUtil() {}

    /**
     * Print a billing invoice.
     */
    public static void printInvoice(Sale sale, List<SaleItem> items) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Invoice - " + sale.getInvoiceNo());

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;

            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            int y = 20;
            int width = (int) pageFormat.getImageableWidth();

            // Header
            g2.setFont(new Font("Arial", Font.BOLD, 16));
            g2.drawString(config.get("pharmacy_name", "MediCheck Pharmacy"), 10, y);
            y += 18;
            g2.setFont(new Font("Arial", Font.PLAIN, 9));
            g2.drawString(config.get("pharmacy_address", ""), 10, y); y += 12;
            g2.drawString("Phone: " + config.get("pharmacy_phone", "") + "  GSTIN: " + config.get("pharmacy_gstin", ""), 10, y); y += 14;

            // Divider
            g2.drawLine(0, y, width, y); y += 8;

            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString("TAX INVOICE", width / 2 - 35, y); y += 14;

            g2.setFont(new Font("Arial", Font.PLAIN, 9));
            g2.drawString("Invoice No: " + sale.getInvoiceNo(), 10, y);
            g2.drawString("Date: " + (sale.getCreatedAt() != null ? sale.getCreatedAt().format(DATE_FMT) : ""), width / 2, y);
            y += 12;
            g2.drawString("Patient: " + (sale.getPatientName() != null ? sale.getPatientName() : "Walk-in"), 10, y);
            g2.drawString("Cashier: " + SessionManager.getCurrentUsername(), width / 2, y);
            y += 10;
            g2.drawLine(0, y, width, y); y += 8;

            // Column headers
            g2.setFont(new Font("Arial", Font.BOLD, 9));
            g2.drawString("Medicine", 10, y);
            g2.drawString("Qty", width - 150, y);
            g2.drawString("Rate", width - 110, y);
            g2.drawString("Amount", width - 60, y);
            y += 4;
            g2.drawLine(0, y, width, y); y += 8;

            // Items
            g2.setFont(new Font("Arial", Font.PLAIN, 9));
            for (SaleItem item : items) {
                g2.drawString(item.getMedicineName(), 10, y);
                g2.drawString(String.valueOf(item.getQuantity()), width - 150, y);
                g2.drawString(String.format("%.2f", item.getUnitPrice()), width - 110, y);
                g2.drawString(String.format("%.2f", item.getTotalPrice()), width - 60, y);
                y += 11;
            }

            g2.drawLine(0, y, width, y); y += 8;

            // Totals
            g2.setFont(new Font("Arial", Font.PLAIN, 9));
            g2.drawString("Subtotal:", width - 120, y);
            g2.drawString(String.format("%.2f", sale.getSubtotal()), width - 60, y); y += 11;
            g2.drawString("Discount:", width - 120, y);
            g2.drawString(String.format("%.2f", sale.getDiscount()), width - 60, y); y += 11;
            g2.drawString("Tax (" + sale.getTaxRate() + "%):", width - 120, y);
            g2.drawString(String.format("%.2f", sale.getTaxAmount()), width - 60, y); y += 11;

            g2.setFont(new Font("Arial", Font.BOLD, 10));
            g2.drawString("TOTAL:", width - 120, y);
            g2.drawString(config.get("currency_symbol", "₹") + String.format(" %.2f", sale.getTotal()), width - 65, y);
            y += 12;

            g2.setFont(new Font("Arial", Font.PLAIN, 8));
            g2.drawString("Payment: " + sale.getPaymentMethod(), 10, y); y += 10;
            g2.drawLine(0, y, width, y); y += 8;
            g2.drawString("Thank you for choosing " + config.get("pharmacy_name", "MediCheck"), 10, y);

            return Printable.PAGE_EXISTS;
        });

        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(null, "Print failed: " + e.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    /**
     * Print a prescription.
     */
    public static void printPrescription(Prescription prescription, List<PrescriptionItem> items) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setJobName("Prescription - " + prescription.getPrescriptionNo());

        job.setPrintable((graphics, pageFormat, pageIndex) -> {
            if (pageIndex > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) graphics;
            g2.translate(pageFormat.getImageableX(), pageFormat.getImageableY());

            int y = 20;
            int width = (int) pageFormat.getImageableWidth();

            g2.setFont(new Font("Arial", Font.BOLD, 14));
            g2.drawString(config.get("pharmacy_name", "MediCheck Clinic"), 10, y); y += 16;
            g2.setFont(new Font("Arial", Font.PLAIN, 9));
            g2.drawString(config.get("pharmacy_address", ""), 10, y); y += 12;
            g2.drawLine(0, y, width, y); y += 8;

            g2.setFont(new Font("Arial", Font.BOLD, 12));
            g2.drawString("PRESCRIPTION", width / 2 - 40, y); y += 12;

            g2.setFont(new Font("Arial", Font.PLAIN, 9));
            g2.drawString("Rx No: " + prescription.getPrescriptionNo(), 10, y);
            g2.drawString("Date: " + (prescription.getCreatedAt() != null ? prescription.getCreatedAt().format(DATE_FMT) : ""), width / 2, y); y += 11;
            g2.drawString("Patient: " + prescription.getPatientName(), 10, y);
            g2.drawString("Doctor: " + prescription.getDoctorName(), width / 2, y); y += 11;
            g2.drawString("Diagnosis: " + (prescription.getDisease() != null ? prescription.getDisease() : ""), 10, y); y += 10;
            g2.drawLine(0, y, width, y); y += 8;

            g2.setFont(new Font("Arial", Font.BOLD, 9));
            g2.drawString("Medicine", 10, y);
            g2.drawString("Dosage", width - 200, y);
            g2.drawString("Frequency", width - 130, y);
            g2.drawString("Duration", width - 60, y); y += 4;
            g2.drawLine(0, y, width, y); y += 8;

            g2.setFont(new Font("Arial", Font.PLAIN, 9));
            for (PrescriptionItem item : items) {
                g2.drawString(item.getMedicineName(), 10, y);
                g2.drawString(item.getDosage() != null ? item.getDosage() : "", width - 200, y);
                g2.drawString(item.getFrequency() != null ? item.getFrequency() : "", width - 130, y);
                g2.drawString(item.getDuration() != null ? item.getDuration() : "", width - 60, y);
                y += 11;
            }

            g2.drawLine(0, y, width, y); y += 8;
            if (prescription.getNotes() != null && !prescription.getNotes().isEmpty()) {
                g2.drawString("Notes: " + prescription.getNotes(), 10, y); y += 11;
            }
            g2.drawString("Doctor's Signature: ___________________", 10, y); y += 20;
            g2.setFont(new Font("Arial", Font.ITALIC, 8));
            g2.drawString("This prescription is computer generated and valid for 30 days.", 10, y);

            return Printable.PAGE_EXISTS;
        });

        if (job.printDialog()) {
            try {
                job.print();
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(null, "Print failed: " + e.getMessage(), "Print Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
