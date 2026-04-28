package com.medicheck.ui;

import com.medicheck.util.ScannerUtil;

import javax.swing.*;
import java.awt.*;

public class ScannerDialog extends JDialog {

    private final ScannerUtil scannerUtil;
    private String scannedBarcode = null;

    public ScannerDialog(Frame owner) {
        super(owner, "Scan Barcode/QR", true);
        setSize(640, 480);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        scannerUtil = new ScannerUtil();
        
        try {
            JPanel cameraPanel = scannerUtil.createScannerPanel(new Dimension(640, 480));
            add(cameraPanel, BorderLayout.CENTER);
        } catch (Exception e) {
            add(new JLabel("Camera not found or unavailable.", SwingConstants.CENTER), BorderLayout.CENTER);
            return;
        }

        JPanel pnlBottom = new JPanel();
        JButton btnCancel = new JButton("Cancel");
        btnCancel.addActionListener(e -> close());
        pnlBottom.add(btnCancel);
        add(pnlBottom, BorderLayout.SOUTH);

        addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                close();
            }
        });
    }

    public String showDialog() {
        if (scannerUtil != null) {
            scannerUtil.startScanning(result -> {
                scannedBarcode = result;
                close();
            });
        }
        setVisible(true);
        return scannedBarcode;
    }

    private void close() {
        if (scannerUtil != null) {
            scannerUtil.stopScanning();
        }
        dispose();
    }
}
