package frames;

import Data_Access_Object.InvoiceHeaders.InvoiceHeader;
import Data_Access_Object.InvoiceHeaders.InvoiceHeaderDaoImp;
import Data_Access_Object.invoice_Details.Invoice_Details;
import Data_Access_Object.invoice_Details.Invoice_DetailsDaoImp;
import com.formdev.flatlaf.FlatLightLaf;
import frames.components.GenericTable;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import javax.swing.event.TableModelEvent;
import javax.swing.table.DefaultTableModel;

public class InvoiceFrame extends JFrame {

    private int userId;
    private InvoiceHeaderDaoImp invoiceHeaderDao;
    private Invoice_DetailsDaoImp invoiceDetailsDao;
        private int invoiceHeaderId;

    private static final String[] TABLE_HEADERS = {
        "ID", "المنتج", "الكمية", "السعر", "الإجمالي"
    };
      String[] paymentMethodsChoices = { "cash", "credit card", "wallet" };
      String[] paymentMethodsChoicesArabic = { "كاش", "بطاقة ائتمان", "محفظة الكترونية" };

 private static final String[] BUTTON_LABELS = {
    "حفظ البيانات",          // Save
    "طباعة",                // Print
    "تحديث البيانات",        // Refresh
    "حذف المنتج",         // Delete
    "إضافة منتج"     // Add New Invoice
};

private static final String[] BUTTON_TOOLTIPS = {
    "حفظ الفاتورة الحالية وكل تفاصيلها",         // Save tooltip
    "طباعة نسخة من الفاتورة المحددة",             // Print tooltip
    "تحديث جدول الفواتير المعروضة",               // Refresh tooltip
    "حذف الفاتورة المحددة",                        // Delete tooltip
    "إضافة منتج"                           // Add tooltip
};

// Assuming you have these methods defined somewhere in the class:
private Runnable[] BUTTON_ACTIONS = {
    this::saveInvoiceData,          // Save action
    this::printInvoiceReceipt,      // Print action
      () -> loadInvoiceData(invoiceHeaderId),       // Refresh action
    this::deleteInvoice,            // Delete action
    this::addNewInvoice             // Add action
};

 private static final Dimension TEXT_FIELD_SIZE = new Dimension(250, 35);
    private final Invoice_Details invoice = null;
    private final Invoice_DetailsDaoImp invoiceDao;
    private final List<Object[]> invoicesData;
    private final GenericTable invoiceTable;
    
    private JTextField incomeField;
    private JTextField totalLabel;
    private JComboBox payComboBox;
    private JLabel payMethodLabel;
    private final JLabel statusLabel;
    
    private final Timer messageTimer;
    private final int clientId;
    private final String clientName;
    private final String clientPhone;
    private static boolean isOpen = false;
private Color InvoiceHeaderColor=new Color(22, 67, 87);
    public InvoiceFrame(int clientId, String clientName, String clientPhone, int userId, int invoiceHeaderId) {
        this.userId = userId;
        this.clientId = clientId;
        this.clientName = clientName;
        this.clientPhone = clientPhone;
        this.invoiceHeaderDao = new InvoiceHeaderDaoImp();
        this.invoiceDetailsDao = new Invoice_DetailsDaoImp();
        this.invoiceDao = new Invoice_DetailsDaoImp();
        this.invoicesData = new ArrayList<>();
        this.messageTimer = new Timer();
        this.statusLabel = createStyledLabel();
        List<Integer> editableColumns = Arrays.asList(1, 2, 3);

        this.invoiceTable = new GenericTable(TABLE_HEADERS, invoicesData, true, editableColumns,InvoiceHeaderColor);

        this.totalLabel = new JTextField(15);
        this.totalLabel.setEditable(false);
        this.totalLabel.setHorizontalAlignment(JTextField.CENTER);
        this.totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        if (isOpen) {
            JOptionPane.showMessageDialog(null, "الصفحة مفتوحة مسبقا");
            return;
        }
        isOpen = true;
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                isOpen = false;
            }
        });

        setupTableListeners();
        initializeFrame();
        setupUI();

        loadInvoiceData(invoiceHeaderId);
        setVisible(true);
        setupKeyListeners();
    }

    private void setupKeyListeners() {
        // Add a key listener to the entire frame
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                // Check if the pressed key is ESC (27 is the key code for ESC)
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    exitFrame(); // Call the exitFrame method
                    e.consume(); // Consume the event so it doesn't propagate further
                }
            }
        });

        // Set the frame to be focusable so it can receive key events
        this.setFocusable(true);
        this.requestFocusInWindow();
    }

// Method to handle frame exit logic
    private void exitFrame() {
        int option = JOptionPane.showConfirmDialog(this,
                "هل تريد حقًا الخروج؟", "تأكيد الخروج", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
                    isOpen = false;

            dispose(); // Close the frame
        }
    }

    private void initializeFrame() {
        setTitle("فاتورة العميل: " + clientName);
        setSize(1080, 720);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setLookAndFeel();
    }

    private void setLookAndFeel() {
        try {
            UIManager.setLookAndFeel(new FlatLightLaf());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }


private void setupUI() {
    JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
    mainPanel.setBorder(BorderFactory.createCompoundBorder(
            new EmptyBorder(5, 15, 20, 15),
            BorderFactory.createLineBorder(Color.LIGHT_GRAY)
    ));
    // Add header
    mainPanel.add(createHeader(), BorderLayout.NORTH);
    mainPanel.add(invoiceTable, BorderLayout.CENTER);
    
    // Create button panel
   JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));

for (int i = 0; i < BUTTON_LABELS.length; i++) {
    JButton button = createStyledButton(BUTTON_LABELS[i], BUTTON_ACTIONS[i]);
    button.setToolTipText(BUTTON_TOOLTIPS[i]);
    buttonPanel.add(button);
}

    
    // Create bottom panel
    JPanel bottomPanel = new JPanel(new BorderLayout(10, 10));
    
    // Create financial info panel with properly labeled fields
    JPanel financialInfoPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 20, 5));
    
    // Payment method selection with label
    JPanel paymentMethodPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
    payMethodLabel = new JLabel("طريقة الدفع:");
    payMethodLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
    payComboBox = new JComboBox(paymentMethodsChoicesArabic);
    payComboBox.setPreferredSize(new Dimension(120, 25));
    paymentMethodPanel.add(payComboBox);
    paymentMethodPanel.add(payMethodLabel);
    
    // Income field with label
    JPanel incomePanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
    JLabel incomeLabel = new JLabel("المبلغ المدفوع:");
    incomeLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
    incomeField = new JTextField(10);
    incomeField.setHorizontalAlignment(JTextField.CENTER);
    incomeField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
    incomePanel.add(incomeField);
    incomePanel.add(incomeLabel);
    
    // Total amount display
    totalLabel.setText("الإجمالي: 0.00");
    totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
    JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
    totalPanel.add(totalLabel);
    
    // Add all financial components to the financial info panel
    financialInfoPanel.add(incomePanel);
    financialInfoPanel.add(paymentMethodPanel);
    financialInfoPanel.add(totalPanel);
    
    // Add status label
    JPanel statusPanel = new JPanel(new BorderLayout());
    statusPanel.add(statusLabel, BorderLayout.CENTER);
    
    // Assemble the bottom panel
    bottomPanel.add(financialInfoPanel, BorderLayout.NORTH);
    bottomPanel.add(buttonPanel, BorderLayout.CENTER);
    bottomPanel.add(statusPanel, BorderLayout.SOUTH);
    
    // Add the bottom panel to the main panel
    mainPanel.add(bottomPanel, BorderLayout.SOUTH);
    
    // Set the main content pane
    setContentPane(mainPanel);
}
    private JPanel createHeader() {
        JLabel headerLabel = new JLabel("فاتورة");
        headerLabel.setFont(new Font("Times New Roman", Font.BOLD, 24));
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerLabel.setBorder(new EmptyBorder(10, 0, 5, 0));
        headerLabel.setForeground(InvoiceHeaderColor);

        // Labels for client details
        JLabel nameLabel = new JLabel("للعميل: " + clientName);
        JLabel phoneLabel = new JLabel("الهاتف: " + clientPhone);
        phoneLabel.setForeground(new Color(20,20,20));
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        phoneLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));

        // Center align text inside labels
        nameLabel.setHorizontalAlignment(SwingConstants.CENTER);
        phoneLabel.setHorizontalAlignment(SwingConstants.CENTER);

        // Panel for client info
        JPanel clientInfoPanel = new JPanel(new GridLayout(2, 1, 0, 5)); // 2 rows, 1 column
        clientInfoPanel.add(nameLabel);
        clientInfoPanel.add(phoneLabel);
        clientInfoPanel.setBackground(new Color(241,241,241));

        // Header panel
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.add(headerLabel, BorderLayout.NORTH);
        headerPanel.add(clientInfoPanel, BorderLayout.CENTER); // Centered horizontally & vertically
        headerPanel.setBackground(new Color(241,241,241));

        return headerPanel;
    }

    private void setupTableListeners() {
        JTable table = invoiceTable.getTable();
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.setShowGrid(false);
        table.setRowHeight(25); // Better row height for visibility

        // Custom editor with text selection on focus
        JTextField textField = new JTextField();
        textField.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                textField.selectAll();
                textField.setBorder(BorderFactory.createLineBorder(Color.BLUE, 2)); // Soft focus border
            }

            @Override
            public void focusLost(FocusEvent e) {
                textField.setBorder(null); // Remove focus border
            }
        });

        table.setDefaultEditor(Object.class, new DefaultCellEditor(textField));

        // Handle mouse click for single-click editing
        table.addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                int row = table.rowAtPoint(e.getPoint());
                int col = table.columnAtPoint(e.getPoint());

                if (row != -1 && col != -1 && table.isCellEditable(row, col)) {
                    table.editCellAt(row, col);
                    selectEditorText(table);
                }
            }
        });

        // Handle keyboard navigation
        table.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int row = table.getSelectedRow();
                int col = table.getSelectedColumn();

                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    e.consume();
                    if (table.isEditing()) {
                        table.getCellEditor().stopCellEditing();
                    }
                    moveToNextCell(table, true);
                } else if (e.getKeyCode() == KeyEvent.VK_TAB) {
                    e.consume();
                    moveToNextCell(table, !e.isShiftDown()); // Shift+Tab for reverse direction
                }
            }
        });

        // Auto-update row total when Quantity or Price changes
        table.getModel().addTableModelListener(e -> {
            if (e.getType() == TableModelEvent.UPDATE) {
                int row = e.getFirstRow();
                int col = e.getColumn();
                if (col == 2 || col == 3) { // Assuming these are the Quantity & Price columns
                    updateRowTotal(row);
                }
            }
        });
    }

// Move to the next editable cell
    private void moveToNextCell(JTable table, boolean forward) {
        int row = table.getSelectedRow();
        int col = table.getSelectedColumn();

        while (true) {
            if (forward) {
                if (col < table.getColumnCount() - 1) {
                    col++;
                } else if (row < table.getRowCount() - 1) {
                    row++;
                    col = 0;
                } else {
                    break; // End of table
                }
            } else { // Reverse direction
                if (col > 0) {
                    col--;
                } else if (row > 0) {
                    row--;
                    col = table.getColumnCount() - 1;
                } else {
                    break; // Start of table
                }
            }

            if (table.isCellEditable(row, col)) {
                table.changeSelection(row, col, false, false);
                table.editCellAt(row, col);
                selectEditorText(table);
                return;
            }
        }
    }

// Select all text in the editor field
    private void selectEditorText(JTable table) {
        Component editor = table.getEditorComponent();
        if (editor instanceof JTextField textField) {
            textField.requestFocus();
            textField.selectAll();
        }
    }

    private void updateRowTotal(int row) {
        JTable table = invoiceTable.getTable();
        DefaultTableModel model = (DefaultTableModel) table.getModel();

        try {
            float quantity = Float.parseFloat(String.valueOf(model.getValueAt(row, 2)));
            float price = Float.parseFloat(String.valueOf(model.getValueAt(row, 3)));
            float total = quantity * price;

            model.setValueAt(total, row, 4);
            updateTotalAmount();
        } catch (NumberFormatException ex) {
            // Invalid number format, skip calculation
        }
    }

    private void updateTotalAmount() {
        JTable table = invoiceTable.getTable();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        float totalAmount = 0.0f;

        for (int i = 0; i < model.getRowCount(); i++) {
            Object totalValue = model.getValueAt(i, 4);
            if (totalValue != null) {
                try {
                    totalAmount += Float.parseFloat(String.valueOf(totalValue));
                } catch (NumberFormatException ex) {
                    // Skip invalid values
                }
            }
        }

        totalLabel.setText(String.format("%.2f :الإجمالي", totalAmount));
        incomeField.setText(String.format("%.2f", totalAmount));
    }

    private void addNewInvoice() {
        DefaultTableModel model = (DefaultTableModel) invoiceTable.getTable().getModel();
        model.addRow(new Object[]{
            0,
            "اسم المنتج",
            0,
            0,
            0,
            invoiceHeaderId
        });
    }

//    private void deleteInvoice() {
//        int selectedRow = invoiceTable.getTable().getSelectedRow();
//        if (selectedRow == -1) {
//            showMessage("يرجى اختيار صف لحذف الفاتورة", true);
//            return;
//        }
//
//        int invoiceDetailId = (int) invoiceTable.getTable().getValueAt(selectedRow, 0); // Assuming the ID is in the first column
//        try {
//            invoiceDetailsDao.delete(invoiceDetailId); // Call delete without expecting a return value
//            System.out.println("Deleting detail ID: " + invoiceDetailId);
//
//            showMessage("تم حذف الفاتورة بنجاح", false);
//            loadInvoiceData(invoiceHeaderId);  // Reload the data after delete
//        } catch (Exception e) {
//            showMessage("حدث خطأ غير متوقع أثناء الحذف: " + e.getMessage(), true);
//            e.printStackTrace();
//        }
//    }
private void deleteInvoice() {
    JTable table = invoiceTable.getTable();
    DefaultTableModel model = (DefaultTableModel) table.getModel();
    int selectedRow = table.getSelectedRow();

    if (selectedRow == -1) {
        showMessage("يرجى اختيار صف لحذف الفاتورة", true);
        return;
    }

    int invoiceDetailId = (int) table.getValueAt(selectedRow, 0); // Column 0 is assumed to be the ID

    // If ID is 0, it's a new unsaved row — remove from table only
    if (invoiceDetailId == 0) {
        model.removeRow(selectedRow);
        showMessage("تم حذف الصف المؤقت بنجاح", false);
        return;
    }

    // Otherwise, delete from DB and refresh table
    try {
        invoiceDetailsDao.delete(invoiceDetailId); // delete from DB
        showMessage("تم حذف الفاتورة بنجاح", false);
        loadInvoiceData(invoiceHeaderId); // reload table
    } catch (Exception e) {
        showMessage("حدث خطأ غير متوقع أثناء الحذف: " + e.getMessage(), true);
        e.printStackTrace();
    }
}

    private void saveInvoiceData() {
        JTable table = invoiceTable.getTable();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        String PaymentMethod="cash";
        try {
            // Validate client ID
            if (clientId <= 0) {
                showMessage("تعريف العميل غير صحيح لهذه الفاتورة", true);
                return;
            }

            float totalInvoiceAmount = 0;

            // First pass: validate all data
            for (int i = 0; i < model.getRowCount(); i++) {
                String product = String.valueOf(model.getValueAt(i, 1)).trim();

                // Validate product name
                if (product.isEmpty()) {
                    showMessage("يرجى إدخال اسم المنتج في السطر " + (i + 1), true);
                    return;
                }

                // Validate quantity and price
                if (model.getValueAt(i, 2) == null || model.getValueAt(i, 3) == null) {
                    showMessage("يرجى ملء جميع الحقول المطلوبة في السطر " + (i + 1), true);
                    return;
                }

                float quantity, price;
                try {
                    quantity = Float.parseFloat(String.valueOf(model.getValueAt(i, 2)));
                    price = Float.parseFloat(String.valueOf(model.getValueAt(i, 3)));
                } catch (NumberFormatException e) {
                    showMessage("قيم غير صالحة للكمية أو السعر في السطر " + (i + 1), true);
                    return;
                }

                if (!validateInvoiceData(product, quantity, price)) {
                    return;
                }

                float total = quantity * price;
                model.setValueAt(total, i, 4); // Update total in UI
                totalInvoiceAmount += total;
            }

            // Get and validate income
            float income;
            try {
                income = Float.parseFloat(incomeField.getText().trim());
                if (income < 0) {
                    showMessage("قيمة الدخل يجب أن تكون أكبر من أو تساوي صفر", true);
                    return;
                }
            } catch (NumberFormatException e) {
                showMessage("قيمة الدخل غير صالحة", true);
                return;
            }

            float remaining = totalInvoiceAmount - income;
          String selected = payComboBox.getSelectedItem().toString().trim();
switch (selected) {
    case "كاش":
        PaymentMethod = "cash";
        break;
    case "بطاقة ائتمان":
        PaymentMethod = "credit card";
        break;
    case "محفظة الكترونية":
        PaymentMethod = "wallet";
        break;
    default:
        showMessage("طريقة الدفع غير صالحة", true);
        return;
} 
//PaymentMethod = selected;

            
            // Save to invoice_header first
            Timestamp currentTime = new Timestamp(System.currentTimeMillis());
            InvoiceHeader invoiceHeader = new InvoiceHeader(
                    invoiceHeaderId, clientId, totalInvoiceAmount, income, remaining,selected,
                    currentTime, currentTime
            );

            // Save the InvoiceHeader, and get the new Invoice ID
            int newInvoiceId = invoiceHeaderDao.save(invoiceHeader);
            if (newInvoiceId == -1) {
                showMessage("فشل في حفظ رأس الفاتورة", true);
                return;
            }

            // Fetch existing products in the invoice to prevent duplicates
            List<Invoice_Details> existingDetails = invoiceDetailsDao.findByInvoiceId(newInvoiceId);
            List<String> existingProducts = new ArrayList<>();
            for (Invoice_Details detail : existingDetails) {
                existingProducts.add(detail.getProduct().trim());
            }

            // Second pass: create and save only NEW invoice details
            boolean success = true;
            for (int i = 0; i < model.getRowCount(); i++) {
                String product = String.valueOf(model.getValueAt(i, 1)).trim();
                float quantity = Float.parseFloat(String.valueOf(model.getValueAt(i, 2)));
                float price = Float.parseFloat(String.valueOf(model.getValueAt(i, 3)));
                float total = quantity * price;

                // Skip saving if product already exists in this invoice
                if (existingProducts.contains(product)) {
                    continue;
                }

                Invoice_Details invoiceDetail = new Invoice_Details(
                        0, // New detail, ID will be generated
                        product,
                        quantity,
                        price,
                        total,
                        PaymentMethod,
                        newInvoiceId, // Use the new invoice ID
                        currentTime,
                        currentTime
                );

                try {
                    invoiceDetailsDao.save(invoiceDetail);
                } catch (Exception e) {
                    success = false;
                    showMessage("حدث خطأ أثناء حفظ تفاصيل الفاتورة: " + e.getMessage(), true);
                    e.printStackTrace();
                    break;
                }
            }

            if (success) {
                showMessage("تم حفظ الفاتورة بنجاح", false);
                // Refresh the table with the new data
                loadInvoiceData(newInvoiceId);
            }
        } catch (Exception e) {
            showMessage("حدث خطأ غير متوقع: " + e.getMessage(), true);
            e.printStackTrace();
        }
         
         InvoiceHeaderFrame.getInstance().loadInvoiceHeaderData();
         InvoiceHeaderFrame.getInstance().updatePaymentHistoryTable();
    }

    private boolean validateInvoiceData(String product, float quantity, float price) {
        if (product == null || product.trim().isEmpty()) {
            showMessage("يرجى إدخال اسم المنتج!", true);
            return false;
        }
        if (quantity <= 0) {
            showMessage("يجب أن تكون الكمية أكبر من صفر!", true);
            return false;
        }
        if (price <= 0) {
            showMessage("يجب أن يكون السعر أكبر من صفر!", true);
            return false;
        }
        return true;
    }

    private void loadInvoiceData(int invoiceHeaderId) {
        try {
            invoicesData.clear();
            statusLabel.setText("جاري تحميل بيانات الفاتورة...");
            this.invoiceHeaderId = invoiceHeaderId;

            if (invoiceHeaderId <= 0) {
                statusLabel.setText("رقم الفاتورة غير صالح");
                return;
            }

            List<Invoice_Details> details = invoiceDetailsDao.findByInvoiceId(invoiceHeaderId);

            if (details.isEmpty()) {
                statusLabel.setText("لا توجد تفاصيل لهذه الفاتورة");
                invoiceTable.updateData(invoicesData);
                totalLabel.setText("0.00 :الإجمالي");
                return;
            }

            double totalIncome = 0.0;
            for (Invoice_Details detail : details) {
                invoicesData.add(new Object[]{
                    detail.getId(),
                    detail.getProduct(),
                    detail.getQuantity(),
                    detail.getPrice(),
                    detail.getTotal(),
                    clientId
                });
                totalIncome += detail.getTotal();
            }

            invoiceTable.updateData(invoicesData);
            updateTotalAmount();
            statusLabel.setText("تم تحميل بيانات الفاتورة بنجاح");

        } catch (Exception e) {
            statusLabel.setText("حدث خطأ أثناء تحميل بيانات الفاتورة: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private JButton createStyledButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setMargin(new Insets(8, 15, 8, 15));
        button.setFocusPainted(false);
        button.addActionListener(e -> action.run());
        return button;
    }

    private JLabel createStyledLabel() {
        JLabel label = new JLabel(" ");
        label.setHorizontalAlignment(SwingConstants.CENTER);
        label.setFont(new Font("Times New Roman", Font.BOLD, 16));
        label.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        label.setBorder(new EmptyBorder(5, 0, 5, 0));
        return label;
    }

    private void showMessage(String message, boolean isError) {
        messageTimer.cancel();
        messageTimer.purge();

        SwingUtilities.invokeLater(() -> {
            statusLabel.setText(message);
            statusLabel.setForeground(isError ? Color.RED : new Color(0, 100, 0));
            statusLabel.setVisible(true);

            new Timer().schedule(new TimerTask() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(() -> {
                        statusLabel.setVisible(false);
                        statusLabel.setText("");
                    });
                }
            }, 3000);
        });
    }

    private void printInvoiceReceipt() {
        try {
            PrinterJob job = PrinterJob.getPrinterJob();
            job.setPrintable(new InvoiceReceiptPrintable());

            if (job.printDialog()) {
                job.print();
                showMessage("تم طباعة الفاتورة بنجاح", false);
            }
        } catch (PrinterException ex) {
            showMessage("حدث خطأ أثناء الطباعة: " + ex.getMessage(), true);
            ex.printStackTrace();
        }
    }

// Add this inner class to your InvoiceFrame class
    private class InvoiceReceiptPrintable implements Printable {

        @Override
        public int print(Graphics g, PageFormat pf, int pageIndex) throws PrinterException {
            if (pageIndex > 0) {
                return Printable.NO_SUCH_PAGE;
            }

            Graphics2D g2d = (Graphics2D) g;
            g2d.translate(pf.getImageableX(), pf.getImageableY());
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));

            // Set drawing coordinates
            int x = 10;
            int y = 10;
            int width = (int) pf.getImageableWidth() - 20;
            int lineHeight = 15;
            int tableRowHeight = 20;

            // Draw the header
            g2d.setFont(new Font("Arial", Font.BOLD, 16));
            drawCenteredString(g2d, "فاتورة", width / 2, y);
            y += lineHeight * 2;

            // Draw horizontal line
            g2d.drawLine(x, y, x + width, y);
            y += lineHeight;

            // Draw date
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd");
            g2d.drawString("التاريخ: " + dateFormat.format(new Date()), x + width - 100, y);
            y += lineHeight;

            // Draw client info
            g2d.drawString("الاسم: " + clientName, x + width - 100, y);
            y += lineHeight;
            g2d.drawString("رقم التليفون: " + clientPhone, x + width - 100, y);
            y += lineHeight * 2;

            // Draw table header
            int[] columnWidths = {width / 3, width / 3, width / 3};
            int tableX = x;

            g2d.setFont(new Font("Arial", Font.BOLD, 10));
            drawCenteredString(g2d, "السعر", tableX + columnWidths[0] / 2, y);
            drawCenteredString(g2d, "الكمية", tableX + columnWidths[0] + columnWidths[1] / 2, y);
            drawCenteredString(g2d, "النوع", tableX + columnWidths[0] + columnWidths[1] + columnWidths[2] / 2, y);

            y += lineHeight;

            // Draw table header line
            g2d.drawLine(tableX, y, tableX + width, y);

            // Draw table borders
            g2d.drawRect(tableX, y, width, tableRowHeight * invoiceTable.getTable().getRowCount());

            // Draw vertical lines
            g2d.drawLine(tableX + columnWidths[0], y,
                    tableX + columnWidths[0], y + tableRowHeight * invoiceTable.getTable().getRowCount());
            g2d.drawLine(tableX + columnWidths[0] + columnWidths[1], y,
                    tableX + columnWidths[0] + columnWidths[1], y + tableRowHeight * invoiceTable.getTable().getRowCount());

            // Draw table content
            JTable table = invoiceTable.getTable();
            g2d.setFont(new Font("Arial", Font.PLAIN, 10));

            double totalAmount = 0.0;
            DefaultTableModel model = (DefaultTableModel) table.getModel();

            for (int i = 0; i < model.getRowCount(); i++) {
                String product = String.valueOf(model.getValueAt(i, 1));
                String quantity = String.valueOf(model.getValueAt(i, 2));
                String price = String.valueOf(model.getValueAt(i, 3));
                double total = Double.parseDouble(String.valueOf(model.getValueAt(i, 4)));
                totalAmount += total;

                int rowY = y + tableRowHeight * i + tableRowHeight;

                // Draw product
                drawRightAlignedString(g2d, product,
                        tableX + columnWidths[0] + columnWidths[1] + columnWidths[2] - 5, rowY - 5);

                // Draw quantity
                drawCenteredString(g2d, quantity,
                        tableX + columnWidths[0] + columnWidths[1] / 2, rowY - 5);

                // Draw price
                drawCenteredString(g2d, price,
                        tableX + columnWidths[0] / 2, rowY - 5);

                // Draw horizontal line for each row
                g2d.drawLine(tableX, rowY, tableX + width, rowY);
            }

            // Draw total
            y = y + tableRowHeight * table.getRowCount() + lineHeight * 2;
            g2d.setFont(new Font("Arial", Font.BOLD, 12));
            drawRightAlignedString(g2d, "الإجمالي", tableX + width - 80, y);
            drawCenteredString(g2d, String.format("%.2f", totalAmount), tableX + 50, y);

            return Printable.PAGE_EXISTS;
        }

        private void drawCenteredString(Graphics2D g2d, String text, int x, int y) {
            FontMetrics metrics = g2d.getFontMetrics();
            int textWidth = metrics.stringWidth(text);
            g2d.drawString(text, x - textWidth / 2, y);
        }

        private void drawRightAlignedString(Graphics2D g2d, String text, int x, int y) {
            FontMetrics metrics = g2d.getFontMetrics();
            int textWidth = metrics.stringWidth(text);
            g2d.drawString(text, x - textWidth, y);
        }
    }

}
