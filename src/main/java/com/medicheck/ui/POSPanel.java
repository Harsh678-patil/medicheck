package com.medicheck.ui;

import com.medicheck.model.Patient;
import com.medicheck.model.Sale;
import com.medicheck.model.SaleItem;
import com.medicheck.model.Medicine;
import com.medicheck.service.MedicineService;
import com.medicheck.service.PatientService;
import com.medicheck.service.SaleService;
import com.medicheck.util.PrintUtil;
import com.medicheck.util.SessionManager;

import javax.swing.*;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class POSPanel extends JPanel {

    private final MedicineService medicineService = new MedicineService();
    private final SaleService saleService = new SaleService();
    private final PatientService patientService = new PatientService();

    private JTextField txtBarcode;
    private JComboBox<Medicine> cbMedicine;
    private JSpinner spnQuantity;
    private JComboBox<Patient> cbPatient;
    private JTable cartTable;
    private DefaultTableModel cartModel;
    private JLabel lblSubtotal, lblTax, lblTotal;
    private final List<SaleItem> currentCart = new ArrayList<>();

    public POSPanel() {
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        initUI();
    }

    private void initUI() {
        JPanel pnlTop = new JPanel(new GridLayout(1, 2, 20, 0));

        JPanel pnlEntry = new JPanel(new GridBagLayout());
        pnlEntry.setBorder(BorderFactory.createTitledBorder("Add Medicine"));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(4, 4, 4, 4);
        gbc.anchor = GridBagConstraints.WEST;

        cbMedicine = new JComboBox<>();
        cbMedicine.setPreferredSize(new Dimension(230, 30));
        loadMedicines();

        spnQuantity = new JSpinner(new SpinnerNumberModel(1, 1, 999, 1));
        spnQuantity.setPreferredSize(new Dimension(70, 30));

        JButton btnAddSelected = new JButton("Add Medicine");
        btnAddSelected.addActionListener(e -> addSelectedMedicine());

        txtBarcode = new JTextField(15);
        txtBarcode.putClientProperty("JTextField.placeholderText", "Optional barcode");
        txtBarcode.addActionListener(e -> addItemByBarcode());
        JButton btnScan = new JButton("Scan");
        btnScan.addActionListener(e -> {
            try {
                ScannerDialog dlg = new ScannerDialog((Frame) SwingUtilities.getWindowAncestor(this));
                String code = dlg.showDialog();
                if (code != null && !code.isEmpty()) { txtBarcode.setText(code); addItemByBarcode(); }
            } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Camera not available."); }
        });
        JButton btnAddBarcode = new JButton("Add Barcode");
        btnAddBarcode.addActionListener(e -> addItemByBarcode());

        gbc.gridx = 0; gbc.gridy = 0;
        pnlEntry.add(new JLabel("Medicine:"), gbc);
        gbc.gridx = 1;
        pnlEntry.add(cbMedicine, gbc);
        gbc.gridx = 2;
        pnlEntry.add(new JLabel("Qty:"), gbc);
        gbc.gridx = 3;
        pnlEntry.add(spnQuantity, gbc);
        gbc.gridx = 4;
        pnlEntry.add(btnAddSelected, gbc);

        gbc.gridx = 0; gbc.gridy = 1;
        pnlEntry.add(new JLabel("Barcode:"), gbc);
        gbc.gridx = 1;
        pnlEntry.add(txtBarcode, gbc);
        gbc.gridx = 2;
        pnlEntry.add(btnAddBarcode, gbc);
        gbc.gridx = 3;
        pnlEntry.add(btnScan, gbc);

        JPanel pnlPatient = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        pnlPatient.setBorder(BorderFactory.createTitledBorder("Customer / Patient"));
        cbPatient = new JComboBox<>();
        cbPatient.addItem(null);
        try { for (Patient p : patientService.getAllPatients()) cbPatient.addItem(p); } catch (Exception ignored) {}
        pnlPatient.add(cbPatient);

        pnlTop.add(pnlEntry); pnlTop.add(pnlPatient);
        add(pnlTop, BorderLayout.NORTH);

        String[] cols = {"Medicine", "Unit Price", "Qty", "Total"};
        cartModel = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int row, int col) { return col == 2; }
        };
        cartModel.addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE && e.getColumn() == 2) {
                syncQuantityFromTable(e.getFirstRow());
            }
        });
        cartTable = new JTable(cartModel);
        cartTable.setRowHeight(30);
        add(new JScrollPane(cartTable), BorderLayout.CENTER);

        JPanel pnlBottom = new JPanel(new BorderLayout());
        pnlBottom.setBorder(BorderFactory.createTitledBorder("Summary & Payment"));

        JPanel pnlTotals = new JPanel(new GridLayout(3, 2, 5, 5));
        lblSubtotal = new JLabel("\u20b90.00", SwingConstants.RIGHT);
        lblTax = new JLabel("\u20b90.00", SwingConstants.RIGHT);
        lblTotal = new JLabel("\u20b90.00", SwingConstants.RIGHT);
        lblTotal.setFont(new Font("Segoe UI", Font.BOLD, 20));
        lblTotal.setForeground(new Color(220, 38, 38));
        pnlTotals.add(new JLabel("Subtotal:", SwingConstants.RIGHT)); pnlTotals.add(lblSubtotal);
        pnlTotals.add(new JLabel("Tax (18%):", SwingConstants.RIGHT)); pnlTotals.add(lblTax);
        pnlTotals.add(new JLabel("Total:", SwingConstants.RIGHT)); pnlTotals.add(lblTotal);

        JPanel pnlCheckout = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 10));
        JComboBox<String> cbPayment = new JComboBox<>(SaleService.PAYMENT_METHODS);
        JButton btnRemove = new JButton("Remove Selected");
        btnRemove.addActionListener(e -> removeSelectedItem());
        JButton btnClear = new JButton("Clear Cart"); btnClear.addActionListener(e -> clearCart());
        JButton btnPay = new JButton("Take Payment & Print Bill");
        btnPay.setBackground(new Color(34, 197, 94)); btnPay.setForeground(Color.WHITE);
        btnPay.setFont(new Font("Segoe UI", Font.BOLD, 14));
        btnPay.addActionListener(e -> processSale((String) cbPayment.getSelectedItem()));
        pnlCheckout.add(new JLabel("Payment Method:")); pnlCheckout.add(cbPayment);
        pnlCheckout.add(btnRemove); pnlCheckout.add(btnClear); pnlCheckout.add(btnPay);

        pnlBottom.add(pnlTotals, BorderLayout.WEST); pnlBottom.add(pnlCheckout, BorderLayout.EAST);
        add(pnlBottom, BorderLayout.SOUTH);
    }

    private void loadMedicines() {
        cbMedicine.removeAllItems();
        try {
            medicineService.getAllMedicines().stream()
                    .filter(Medicine::isActive)
                    .filter(m -> m.getQuantity() > 0)
                    .sorted(Comparator.comparing(Medicine::getName, String.CASE_INSENSITIVE_ORDER))
                    .forEach(cbMedicine::addItem);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Unable to load medicines: " + ex.getMessage());
        }
    }

    private void addSelectedMedicine() {
        Medicine medicine = (Medicine) cbMedicine.getSelectedItem();
        int quantity = (Integer) spnQuantity.getValue();
        if (medicine == null) {
            JOptionPane.showMessageDialog(this, "Select a medicine first.");
            return;
        }
        addMedicineToCart(medicine, quantity);
    }

    private void addItemByBarcode() {
        String code = txtBarcode.getText().trim();
        if (code.isEmpty()) return;
        try {
            Optional<Medicine> opt = medicineService.findByBarcode(code);
            if (opt.isEmpty()) {
                JOptionPane.showMessageDialog(this, "Medicine not found: " + code);
                txtBarcode.setText("");
                return;
            }
            addMedicineToCart(opt.get(), 1);
            txtBarcode.setText("");
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage()); }
    }

    private void addMedicineToCart(Medicine medicine, int quantityToAdd) {
        if (medicine.getQuantity() <= 0) {
            JOptionPane.showMessageDialog(this, "This medicine is out of stock.");
            return;
        }
        int existingQty = 0;
        int existingIndex = -1;
        for (int i = 0; i < currentCart.size(); i++) {
            SaleItem item = currentCart.get(i);
            if (item.getMedicineId() == medicine.getId()) {
                existingQty = item.getQuantity();
                existingIndex = i;
                break;
            }
        }
        int newQty = existingQty + quantityToAdd;
        if (newQty > medicine.getQuantity()) {
            JOptionPane.showMessageDialog(this,
                    "Only " + medicine.getQuantity() + " units available for " + medicine.getName() + ".");
            return;
        }

        if (existingIndex >= 0) {
            SaleItem item = currentCart.get(existingIndex);
            item.setQuantity(newQty);
            item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(newQty)));
            cartModel.setValueAt(item.getQuantity(), existingIndex, 2);
            cartModel.setValueAt(item.getTotalPrice(), existingIndex, 3);
        } else {
            SaleItem item = new SaleItem(medicine.getId(), medicine.getName(), quantityToAdd, medicine.getPrice());
            currentCart.add(item);
            cartModel.addRow(new Object[]{item.getMedicineName(), item.getUnitPrice(), item.getQuantity(), item.getTotalPrice()});
        }
        spnQuantity.setValue(1);
        updateTotals();
    }

    private void syncQuantityFromTable(int row) {
        if (row < 0 || row >= currentCart.size()) return;
        Object value = cartModel.getValueAt(row, 2);
        int quantity;
        try {
            quantity = Integer.parseInt(String.valueOf(value));
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Quantity must be a whole number.");
            cartModel.setValueAt(currentCart.get(row).getQuantity(), row, 2);
            return;
        }

        if (quantity <= 0) {
            JOptionPane.showMessageDialog(this, "Quantity must be at least 1.");
            cartModel.setValueAt(currentCart.get(row).getQuantity(), row, 2);
            return;
        }

        SaleItem item = currentCart.get(row);
        Optional<Medicine> medicineOpt = medicineService.getById(item.getMedicineId());
        if (medicineOpt.isPresent() && quantity > medicineOpt.get().getQuantity()) {
            JOptionPane.showMessageDialog(this,
                    "Only " + medicineOpt.get().getQuantity() + " units available for " + item.getMedicineName() + ".");
            cartModel.setValueAt(item.getQuantity(), row, 2);
            return;
        }

        item.setQuantity(quantity);
        item.setTotalPrice(item.getUnitPrice().multiply(BigDecimal.valueOf(quantity)));
        cartModel.setValueAt(item.getTotalPrice(), row, 3);
        updateTotals();
    }

    private void updateTotals() {
        try {
            Sale temp = saleService.calculateTotals(currentCart, BigDecimal.ZERO);
            lblSubtotal.setText(String.format("\u20b9%.2f", temp.getSubtotal()));
            lblTax.setText(String.format("\u20b9%.2f", temp.getTaxAmount()));
            lblTotal.setText(String.format("\u20b9%.2f", temp.getTotal()));
        } catch (Exception ignored) {}
    }

    private void clearCart() {
        currentCart.clear(); cartModel.setRowCount(0); updateTotals(); cbPatient.setSelectedIndex(0);
    }

    private void removeSelectedItem() {
        int row = cartTable.getSelectedRow();
        if (row < 0) {
            JOptionPane.showMessageDialog(this, "Select a cart item to remove.");
            return;
        }
        currentCart.remove(row);
        cartModel.removeRow(row);
        updateTotals();
    }

    private void processSale(String paymentMethod) {
        if (currentCart.isEmpty()) { JOptionPane.showMessageDialog(this, "Cart is empty."); return; }
        if (!SessionManager.canAccess("Admin", "Pharmacist")) {
            JOptionPane.showMessageDialog(this,
                    "Only Admin or Pharmacist users can complete billing payments.",
                    "Access Denied",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Sale sale = saleService.calculateTotals(currentCart, BigDecimal.ZERO);
            Patient selectedPatient = (Patient) cbPatient.getSelectedItem();
            if (selectedPatient != null) { sale.setPatientId(selectedPatient.getId()); sale.setPatientName(selectedPatient.getFullName()); }
            else { sale.setPatientName("Walk-in Customer"); }
            sale.setPaymentMethod(paymentMethod);
            if (!collectPaymentDetails(sale, paymentMethod)) {
                return;
            }
            Sale completed = saleService.processSale(sale, currentCart);
            JOptionPane.showMessageDialog(this, "Sale successful! Invoice: " + completed.getInvoiceNo());
            PrintUtil.printInvoice(completed, currentCart);
            clearCart();
            loadMedicines();
        } catch (Exception ex) { JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE); }
    }

    private boolean collectPaymentDetails(Sale sale, String paymentMethod) {
        JPanel form = new JPanel(new GridLayout(0, 2, 8, 8));
        JTextField txtReference = new JTextField(16);
        JTextField txtAmountReceived = new JTextField(sale.getTotal().toPlainString());

        form.add(new JLabel("Customer:"));
        form.add(new JLabel(sale.getPatientName()));
        form.add(new JLabel("Payment Method:"));
        form.add(new JLabel(paymentMethod));
        form.add(new JLabel("Bill Total:"));
        form.add(new JLabel(String.format("\u20b9%.2f", sale.getTotal())));

        if ("Cash".equals(paymentMethod)) {
            form.add(new JLabel("Amount Received:"));
            form.add(txtAmountReceived);
        } else {
            form.add(new JLabel("Reference ID:"));
            form.add(txtReference);
        }

        int choice = JOptionPane.showConfirmDialog(
                this,
                form,
                "Confirm Payment",
                JOptionPane.OK_CANCEL_OPTION,
                JOptionPane.PLAIN_MESSAGE
        );

        if (choice != JOptionPane.OK_OPTION) return false;

        if ("Cash".equals(paymentMethod)) {
            BigDecimal amountReceived;
            try {
                amountReceived = new BigDecimal(txtAmountReceived.getText().trim());
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Enter a valid received amount.");
                return false;
            }
            if (amountReceived.compareTo(sale.getTotal()) < 0) {
                JOptionPane.showMessageDialog(this, "Received amount cannot be less than the bill total.");
                return false;
            }
            BigDecimal change = amountReceived.subtract(sale.getTotal());
            sale.setPaymentStatus("Paid");
            sale.setNotes("Cash received: " + amountReceived.toPlainString() + " | Change returned: " + change.toPlainString());
            return true;
        }

        String reference = txtReference.getText().trim();
        if (reference.length() < 6) {
            JOptionPane.showMessageDialog(this, "Enter a valid payment reference with at least 6 characters.");
            return false;
        }

        if ("Insurance".equals(paymentMethod)) {
            sale.setPaymentStatus("Pending");
            sale.setNotes("Insurance claim reference: " + reference);
        } else {
            sale.setPaymentStatus("Paid");
            sale.setNotes(paymentMethod + " reference: " + reference);
        }
        return true;
    }
}
