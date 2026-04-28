package com.medicheck.util;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.NotFoundException;
import com.google.zxing.Result;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.HybridBinarizer;

import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Utility for barcode/QR scanning via webcam.
 */
public class ScannerUtil {

    private Webcam webcam;
    private WebcamPanel webcamPanel;
    private ExecutorService executor;
    private volatile boolean isScanning = false;

    public ScannerUtil() {
        executor = Executors.newSingleThreadExecutor();
    }

    public WebcamPanel createScannerPanel(Dimension size) {
        webcam = Webcam.getDefault();
        if (webcam == null) {
            throw new RuntimeException("No webcam detected");
        }
        
        webcam.setViewSize(WebcamResolution.VGA.getSize());
        webcamPanel = new WebcamPanel(webcam);
        webcamPanel.setPreferredSize(size);
        webcamPanel.setFPSDisplayed(true);
        webcamPanel.setMirrored(false);
        return webcamPanel;
    }

    public void startScanning(Consumer<String> onResult) {
        if (webcam == null || isScanning) return;
        isScanning = true;
        
        executor.submit(() -> {
            MultiFormatReader reader = new MultiFormatReader();
            while (isScanning && webcam.isOpen()) {
                try {
                    BufferedImage image = webcam.getImage();
                    if (image == null) {
                        Thread.sleep(100);
                        continue;
                    }
                    
                    LuminanceSource source = new BufferedImageLuminanceSource(image);
                    BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
                    
                    try {
                        Result result = reader.decode(bitmap);
                        if (result != null) {
                            String text = result.getText();
                            javax.swing.SwingUtilities.invokeLater(() -> onResult.accept(text));
                            // Pause briefly after a scan
                            Thread.sleep(2000); 
                        }
                    } catch (NotFoundException e) {
                        // Keep attempting
                    }
                    
                    Thread.sleep(100);
                } catch (Exception e) {
                    AppLogger.error("Scanning error", e);
                }
            }
        });
    }

    public void stopScanning() {
        isScanning = false;
        if (webcam != null && webcam.isOpen()) {
            webcam.close();
        }
    }
}
