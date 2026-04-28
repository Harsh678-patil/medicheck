package com.medicheck.ui;

import com.medicheck.model.User;
import com.medicheck.dao.UserDAO;
import com.medicheck.service.AuthService;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class AdminPanel extends JPanel {

    private final UserDAO userDAO = new UserDAO();
    private final AuthService authService = new AuthService();
    private JTable table;
    private DefaultTableModel tableModel;

    public AdminPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        initUI();
        loadData();
    }

    private void initUI() {
        JPanel pnlTop = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton btnAdd = new JButton("Add User"); btnAdd.addActionListener(e -> showAddUserDialog());
        JButton btnToggle = new JButton("Toggle Active"); btnToggle.addActionListener(e -> toggleStatus());
        JButton btnReset = new JButton("Reset Password"); btnReset.addActionListener(e -> resetPassword());
        pnlTop.add(btnAdd); pnlTop.add(btnReset); pnlTop.add(btnToggle);
        add(pnlTop, BorderLayout.NORTH);

        String[] cols = {"ID", "Username", "Full Name", "Email", "Role", "Active", "Last Login"};
        tableModel = new DefaultTableModel(cols, 0);
        table = new JTable(tableModel); table.setRowHeight(30);
        add(new JScrollPane(table), BorderLayout.CENTER);
    }

    private void loadData() {
        tableModel.setRowCount(0);
        try {
            List<User> users = userDAO.findAll();
            for (User u : users) {
                tableModel.addRow(new Object[]{
                    u.getId(), u.getUsername(), u.getFullName(), u.getEmail(),
                    u.getRoleName(), u.isActive() ? "Yes" : "No", u.getLastLogin()
                });
            }
        } catch (Exception ignored) {}
    }

    private void toggleStatus() {
        int row = table.getSelectedRow(); if (row < 0) return;
        try {
            int id = (int) tableModel.getValueAt(row, 0);
            boolean currentActive = tableModel.getValueAt(row, 5).equals("Yes");
            userDAO.setActive(id, !currentActive); loadData();
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    }

    private void resetPassword() {
        int row = table.getSelectedRow(); if (row < 0) return;
        int id = (int) tableModel.getValueAt(row, 0);
        String newPass = JOptionPane.showInputDialog(this, "Enter new password:");
        if (newPass != null && !newPass.trim().isEmpty()) {
            try { authService.resetPassword(id, newPass); JOptionPane.showMessageDialog(this, "Password reset successfully"); }
            catch (Exception ex) { JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        }
    }

    private void showAddUserDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "Add System User", true);
        dlg.setSize(400, 300); dlg.setLocationRelativeTo(this);
        JPanel pnlForm = new JPanel(new GridLayout(5, 2, 10, 10));
        pnlForm.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JTextField txtUser = new JTextField(), txtFull = new JTextField(), txtEmail = new JTextField();
        JPasswordField txtPass = new JPasswordField();
        JComboBox<String> cbRole = new JComboBox<>(new String[]{"Admin", "Doctor", "Pharmacist"});

        pnlForm.add(new JLabel("Username:")); pnlForm.add(txtUser);
        pnlForm.add(new JLabel("Full Name:")); pnlForm.add(txtFull);
        pnlForm.add(new JLabel("Email:")); pnlForm.add(txtEmail);
        pnlForm.add(new JLabel("Password:")); pnlForm.add(txtPass);
        pnlForm.add(new JLabel("Role:")); pnlForm.add(cbRole);

        JButton btnSave = new JButton("Create User");
        btnSave.addActionListener(e -> {
            try {
                User u = new User();
                u.setUsername(txtUser.getText()); u.setFullName(txtFull.getText());
                u.setEmail(txtEmail.getText()); u.setRoleId(cbRole.getSelectedIndex() + 1);
                authService.register(u, new String(txtPass.getPassword()));
                dlg.dispose(); loadData();
            } catch (Exception ex) { JOptionPane.showMessageDialog(dlg, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
        });
        JPanel pnlBtns = new JPanel(); pnlBtns.add(btnSave);
        dlg.add(pnlForm, BorderLayout.CENTER); dlg.add(pnlBtns, BorderLayout.SOUTH);
        dlg.setVisible(true);
    }
}
