package com.medicheck.util;

import com.medicheck.config.AppConfig;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Generates unique invoice and prescription numbers.
 * Format: PREFIX-YYYY-NNNNN (e.g., INV-2024-00001)
 */
public final class InvoiceUtil {

    private static final AtomicLong invoiceCounter = new AtomicLong(1);
    private static final AtomicLong prescriptionCounter = new AtomicLong(1);

    private InvoiceUtil() {}

    public static String generateInvoiceNumber() {
        String prefix = AppConfig.getInstance().get("invoice_prefix", "INV");
        String year = String.valueOf(LocalDate.now().getYear());
        long seq = invoiceCounter.getAndIncrement();
        return String.format("%s-%s-%05d", prefix, year, seq);
    }

    public static String generatePrescriptionNumber() {
        String prefix = AppConfig.getInstance().get("prescription_prefix", "RX");
        String year = String.valueOf(LocalDate.now().getYear());
        long seq = prescriptionCounter.getAndIncrement();
        return String.format("%s-%s-%04d", prefix, year, seq);
    }

    /**
     * Set the starting counter (e.g. from DB max) to avoid duplicates on restart.
     */
    public static void initInvoiceCounter(long lastNumber) {
        invoiceCounter.set(lastNumber + 1);
    }

    public static void initPrescriptionCounter(long lastNumber) {
        prescriptionCounter.set(lastNumber + 1);
    }
}
