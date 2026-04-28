package com.medicheck.ui;

import com.medicheck.service.AIAssistantService;
import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class AIAssistantPanel extends JPanel {

    private final AIAssistantService aiService = new AIAssistantService();
    private JTextPane txtChat;
    private JTextField txtInput;

    public AIAssistantPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        initUI();
    }

    private void initUI() {
        // Header
        JPanel pnlTop = new JPanel(new BorderLayout());
        JLabel lblTitle = new JLabel("🤖 MediCheck AI Medical Assistant");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        lblTitle.setForeground(new Color(99, 102, 241));
        
        JLabel lblDesc = new JLabel("Ask about diseases, symptoms, or medicines. Medical advice generated is for reference only.");
        lblDesc.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        
        pnlTop.add(lblTitle, BorderLayout.NORTH);
        pnlTop.add(lblDesc, BorderLayout.SOUTH);
        pnlTop.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        add(pnlTop, BorderLayout.NORTH);

        // Chat Area
        txtChat = new JTextPane();
        txtChat.setContentType("text/html");
        txtChat.setEditable(false);
        txtChat.setBackground(new Color(30, 41, 59));
        JScrollPane scroll = new JScrollPane(txtChat);
        add(scroll, BorderLayout.CENTER);

        // Set initial welcome text
        txtChat.setText("<html><body style='font-family: Segoe UI; color: #cbd5e1;'>" +
                "<h3>Welcome to MediCheck AI Assistant.</h3>" +
                "<p>I can help you look up information on common conditions such as:</p>" +
                "<p style='color:#38bdf8;'>" + String.join(", ", aiService.getAvailableConditions()) + "</p>" +
                "<p>Type a condition in the box below and press Enter.</p>" +
                "</body></html>");

        // Input Area
        JPanel pnlBottom = new JPanel(new BorderLayout(10, 0));
        txtInput = new JTextField();
        txtInput.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        txtInput.putClientProperty("JTextField.placeholderText", "Ask about a disease or symptom...");
        
        JButton btnSend = new JButton("Ask AI");
        btnSend.setBackground(new Color(99, 102, 241));
        btnSend.setForeground(Color.WHITE);
        btnSend.setFocusPainted(false);
        
        Runnable sendAction = () -> {
            String query = txtInput.getText().trim();
            if (!query.isEmpty()) {
                fetchResponse(query);
                txtInput.setText("");
            }
        };
        
        btnSend.addActionListener(e -> sendAction.run());
        txtInput.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) sendAction.run();
            }
        });

        pnlBottom.add(txtInput, BorderLayout.CENTER);
        pnlBottom.add(btnSend, BorderLayout.EAST);
        add(pnlBottom, BorderLayout.SOUTH);
    }

    private void fetchResponse(String query) {
        // Append user question
        txtChat.setText(""); // clear previous for cleaner look
        
        // Show loading state
        txtChat.setText("<html><body style='font-family:Segoe UI; color:#94A3B8;'>" +
                "<i>Thinking about '" + query + "'...</i></body></html>");
                
        // Fetch async to prevent UI block if using OpenAI
        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                return aiService.getSuggestion(query);
            }

            @Override
            protected void done() {
                try {
                    String responseHtml = get();
                    txtChat.setText(responseHtml);
                } catch (Exception e) {
                    txtChat.setText("<html><body style='color:#EF4444;'>Error occurred while fetching data.</body></html>");
                }
            }
        };
        worker.execute();
    }
}
