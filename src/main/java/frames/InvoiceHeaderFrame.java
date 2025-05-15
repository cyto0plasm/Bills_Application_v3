    package frames;

    import Data_Access_Object.Client.Client;
    import Data_Access_Object.Client.ClientDaoImp;
    import Data_Access_Object.InvoiceHeaders.InvoiceHeader;
    import Data_Access_Object.InvoiceHeaders.InvoiceHeaderDaoImp;
import Data_Access_Object.invoice_payment_history.InvoicePaymentHistory;
    import Data_Access_Object.invoice_payment_history.InvoicePaymentHistoryDao;
    import Data_Access_Object.invoice_payment_history.InvoicePaymentHistoryDaoImp;
    import frames.components.GenericTable;
    import frames.components.InvoiceStatusRenderer;

    import javax.swing.*;
    import java.awt.*;
    import java.awt.event.ActionEvent;
    import java.awt.event.KeyAdapter;
    import java.awt.event.KeyEvent;
    import java.awt.event.MouseAdapter;
    import java.awt.event.MouseEvent;
    import java.awt.event.WindowAdapter;
    import java.awt.event.WindowEvent;
    import java.sql.Timestamp;
    import java.text.ParseException;
    import java.text.SimpleDateFormat;
    import java.util.ArrayList;
    import java.util.Arrays;
    import java.util.Date;
    import java.util.List;
    import java.util.logging.Level;
    import java.util.logging.Logger;
import javax.swing.border.EmptyBorder;
    import javax.swing.event.DocumentEvent;
    import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumnModel;

    public class InvoiceHeaderFrame extends JFrame {
    private static InvoiceHeaderFrame instance;

        private int userId;
        private int clientId;
        private String clientName;
        private String clientPhone;
        private JLabel fullRemainingLabel;
        private float totalRemaining;
        private InvoiceHeaderDaoImp invoiceHeaderDao;
        private List<InvoiceHeader> invoiceHeadersData;
        private GenericTable invoiceHeaderTable;
        private static boolean isOpen = false;
        private Color InvoiceHeaderColor=new Color(90, 106, 112);
private JTextField updateField;
private JTable leftTable;
        // Constructor
        public InvoiceHeaderFrame(int clientId, String clientName, String clientPhone, int userId) {
              instance = this;
            if (isOpen) {
                JOptionPane.showMessageDialog(null, "نافذة الفواتير مفتوحة مسبقا");
                return;
            }
            isOpen = true;
            this.userId = userId;
            this.clientId = clientId;
            this.clientName = clientName;
            this.clientPhone = clientPhone;     
            this.invoiceHeaderDao = new InvoiceHeaderDaoImp();
            this.invoiceHeadersData = new ArrayList<>();

            // Initialize the table with appropriate column headers for Invoice Header
            String[] tableHeaders = {"رقم الفاتورة", "المبلغ الكلي", "الدخل", "المتبقي","طريقة الدفع", "تاريخ الإنشاء"};
            List<Integer> editableColumns = Arrays.asList();

            this.invoiceHeaderTable = new GenericTable(tableHeaders, new ArrayList<>(), false, editableColumns,InvoiceHeaderColor);

            // Apply color coding to the table
            // Load invoice header data
            setupUI();
            loadInvoiceHeaderData();
            // Setup Frame

            setupKeyListeners();
        }
    public static InvoiceHeaderFrame getInstance() {
        return instance;
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
                System.out.println("Disposing InvoiceFrame...");
        isOpen = false;

                dispose(); // Close the frame
 System.out.println("Disposed of  InvoiceFrame...");

            }
        }
// Setup the User Interface
private void setupUI() {
    setTitle("فواتير العميل: " + clientName);
    setSize(1080, 720);
    setLocationRelativeTo(null);
    setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    setExtendedState(JFrame.MAXIMIZED_BOTH);
    setLayout(new BorderLayout());
    invoiceHeaderTable.getTable().setName("invoiceHeaderTable");

    // Create left panel with payment history + update controls
    JPanel leftPanel = createLeftPanel();

    // Split pane to hold leftPanel and invoiceHeaderTable side by side
    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, invoiceHeaderTable);
    splitPane.setDividerLocation(400);
    splitPane.setResizeWeight(0); // Let right panel stretch
    add(splitPane, BorderLayout.CENTER);

    add(createHeader(), BorderLayout.NORTH);

    // Button panel (bottom)
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
    fullRemainingLabel = new JLabel("المتبقي: 0.00");
    fullRemainingLabel.setFont(new Font("Tahoma", Font.BOLD, 18));
    buttonPanel.add(fullRemainingLabel);

    buttonPanel.add(createStyledButton("إضافة فاتورة جديدة", e -> {
        try {
            addNewInvoiceHeader();
        } catch (ParseException ex) {
            Logger.getLogger(InvoiceHeaderFrame.class.getName()).log(Level.SEVERE, null, ex);
        }
    }));

   // buttonPanel.add(createStyledButton("تحديث الدخل", e -> updateSelectedInvoiceHeader()));
    buttonPanel.add(createStyledButton("حذف الفاتورة", e -> deleteSelectedInvoiceHeader()));
    buttonPanel.add(createStyledButton("تحديث البيانات", e -> loadInvoiceHeaderData()));

    add(buttonPanel, BorderLayout.SOUTH);

    // Double-click listener on main invoice table
    invoiceHeaderTable.getTable().addMouseListener(new MouseAdapter() {
        @Override
        public void mouseClicked(MouseEvent e) {
            if (e.getClickCount() == 2) {
                int row = invoiceHeaderTable.getTable().getSelectedRow();
                if (row >= 0) {
                    int invoiceHeaderId = (int) invoiceHeaderTable.getTable().getValueAt(row, 0);
                    openInvoiceDetailsFrame(invoiceHeaderId);
                }
            }
        }
    });

    setVisible(true);

    addWindowListener(new WindowAdapter() {
        @Override
        public void windowClosing(WindowEvent e) {
            isOpen = false;
        }
    });
    initializePaymentHistoryTable();
}

private void initializePaymentHistoryTable() {
    updatePaymentHistoryTable();
}
 private String formatNumber(float number) {
        // Check if the number is a whole number (no decimal part)
        if (number == Math.floor(number)) {
            return String.format("%.0f", number); // Format as integer with no decimal places
        } else {
            return String.format("%.2f", number); // Format with 2 decimal places
        }
    }
private void updateIncomeForInvoices() {
    try {
        // Parse the new income value from the text field
        String input = updateField.getText().trim();
        if (input.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "الرجاء إدخال قيمة للدخل الجديد", 
                "خطأ", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        float newIncome = Float.parseFloat(input);
        
        // Check if new value is valid (non-negative)
        if (newIncome <= 0) {
            JOptionPane.showMessageDialog(this, 
                "قيمة الدخل يجب أن تكون موجبة", 
                "خطأ", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Check if there's any remaining amount to pay
        if (totalRemaining <= 0) {
            JOptionPane.showMessageDialog(this, 
                "لا يوجد متبقي للدفع في أي فاتورة", 
                "تنبيه", 
                JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        // Check if new income exceeds total remaining amount
        if (newIncome > totalRemaining) {
            float difference = newIncome - totalRemaining;
            JOptionPane.showMessageDialog(this, 
                String.format("الدخل الجديد (%s) يتجاوز المبلغ المتبقي (%s) بفارق (%s)", 
                    formatNumber(newIncome), formatNumber(totalRemaining), formatNumber(difference)), 
                "خطأ", 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Confirm the update with the user
        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("هل أنت متأكد من إضافة دخل جديد بقيمة %s؟", formatNumber(newIncome)),
                "تأكيد التحديث",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Get all invoices for this client with remaining > 0, sorted by creation date (newest first)
        List<InvoiceHeader> headers = invoiceHeaderDao.findByClientId(clientId);
        List<InvoiceHeader> unpaidHeaders = new ArrayList<>();
        
        for (InvoiceHeader header : headers) {
            if (header.getRemaining() > 0) {
                unpaidHeaders.add(header);
            }
        }
        
        // Sort by creation date (newest first)
        unpaidHeaders.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        
        if (unpaidHeaders.isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                "لا توجد فواتير غير مدفوعة للتحديث", 
                "تنبيه", 
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Apply the income to the newest invoices first
        float remainingIncome = newIncome;
        Timestamp now = new Timestamp(System.currentTimeMillis());
        InvoicePaymentHistoryDaoImp historyDao = new InvoicePaymentHistoryDaoImp();
        
        for (InvoiceHeader header : unpaidHeaders) {
            if (remainingIncome <= 0) {
                break; // No more income to distribute
            }
            
            float currentRemaining = header.getRemaining();
            float amountToApply = Math.min(remainingIncome, currentRemaining);
            
            // Update the invoice
            float newHeaderIncome = header.getIncome() + amountToApply;
            float newHeaderRemaining = header.getRemaining() - amountToApply;
            
            header.setIncome(newHeaderIncome);
            header.setRemaining(newHeaderRemaining);
            header.setUpdatedAt(now);
            
            // Save the updated header
            invoiceHeaderDao.save(header);
            
            // Save the payment history
            InvoicePaymentHistory paymentHistory = new InvoicePaymentHistory();
            paymentHistory.setInvoice_header_id(header.getId());
            paymentHistory.setAmount_paid(amountToApply);
            paymentHistory.setIncome(newHeaderIncome);
            paymentHistory.setCreatedAt(now);
            paymentHistory.setUpdatedAt(now);
            historyDao.save(paymentHistory);
            
            // Reduce the remaining income
            remainingIncome -= amountToApply;
        }
        
        // Update the UI
        loadInvoiceHeaderData();
        updatePaymentHistoryTable();
        
        // Clear the input field
        updateField.setText("");
        
        // Show success message
        if (remainingIncome > 0) {
            // Some income was not applied
            JOptionPane.showMessageDialog(this, 
                String.format("تم تحديث الفواتير بنجاح. تم استخدام %s من أصل %s", 
                    formatNumber(newIncome - remainingIncome), formatNumber(newIncome)), 
                "نجاح", 
                JOptionPane.INFORMATION_MESSAGE);
        } else {
            // All income was applied
            JOptionPane.showMessageDialog(this, 
                "تم تحديث الفواتير بنجاح", 
                "نجاح", 
                JOptionPane.INFORMATION_MESSAGE);
        }
        
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, 
            "الرجاء إدخال قيمة رقمية صحيحة", 
            "خطأ", 
            JOptionPane.ERROR_MESSAGE);
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, 
            "حدث خطأ أثناء تحديث الفواتير: " + ex.getMessage(), 
            "خطأ", 
            JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}private void updateTotalRemaining() {
    String input = updateField.getText().trim();
    
    // If input is empty, just return silently — do NOT show error here.
    if (input.isEmpty()) {
        return;  // silently ignore empty input, no dialog
    }
    try {
        float newTotalRemaining = Float.parseFloat(input);
        
        // Check for negative value
        if (newTotalRemaining < 0) {
            JOptionPane.showMessageDialog(this,
                "المتبقي لا يمكن أن يكون سالباً",
                "خطأ",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Calculate the difference between new and current total remaining
        float difference = newTotalRemaining - totalRemaining;
        
        // If there's no change, just clear the field and return
        if (difference == 0) {
            updateField.setText("");
            return;
        }
        
        // If adding money (increasing the remaining amount)
        if (difference > 0) {
            JOptionPane.showMessageDialog(this,
                "لا يمكن زيادة المتبقي. يمكن فقط تقليل المتبقي من خلال الدفعات",
                "خطأ",
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Confirm the update with the user
        int confirm = JOptionPane.showConfirmDialog(this,
                String.format("هل أنت متأكد من تحديث المتبقي من %.2f إلى %.2f؟",
                        totalRemaining, newTotalRemaining),
                "تأكيد التحديث",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }
        
        // Find invoices for this client
        List<InvoiceHeader> headers = invoiceHeaderDao.findByClientId(clientId);
        if (headers.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "لا توجد فواتير للتحديث",
                "تنبيه",
                JOptionPane.WARNING_MESSAGE);
            return;
        }
        
        // Handle special case: setting all to fully paid
        if (newTotalRemaining == 0) {
            updateAllInvoicesToFullyPaid(headers);
        } else {
            // Update invoices proportionally based on the payment difference
            updateInvoicesProportionally(headers, difference);
        }
        
        // Save payment history, reload data and clear field
        savePaymentHistory(difference);
        loadInvoiceHeaderData();
        updatePaymentHistoryTable();
        updateField.setText("");
        
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this,
            "الرجاء إدخال قيمة رقمية صحيحة",
            "خطأ",
            JOptionPane.ERROR_MESSAGE);
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this,
            "حدث خطأ أثناء تحديث المتبقي: " + ex.getMessage(),
            "خطأ",
            JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}

// Update all invoices to fully paid
private void updateAllInvoicesToFullyPaid(List<InvoiceHeader> headers) {
    Timestamp now = new Timestamp(System.currentTimeMillis());
    
    for (InvoiceHeader header : headers) {
        if (header.getRemaining() > 0) {
            // Calculate the additional income needed to make remaining zero
            float additionalIncome = header.getRemaining();
            
            // Update the header
            header.setIncome(header.getTotalAmount()); // Set income to total amount
            header.setRemaining(0); // Set remaining to zero
            header.setUpdatedAt(now);
            
            // Save the updated header
            invoiceHeaderDao.save(header);
            
            // Save the payment history
            InvoicePaymentHistory paymentHistory = new InvoicePaymentHistory();
            paymentHistory.setInvoice_header_id(header.getId());
            paymentHistory.setAmount_paid(additionalIncome);
            paymentHistory.setIncome(header.getTotalAmount());
            paymentHistory.setCreatedAt(now);
            paymentHistory.setUpdatedAt(now);
            
            InvoicePaymentHistoryDaoImp historyDao = new InvoicePaymentHistoryDaoImp();
            historyDao.save(paymentHistory);
        }
    }
}

// Update invoices proportionally based on their current remaining amounts
private void updateInvoicesProportionally(List<InvoiceHeader> headers, float totalDifference) {
    Timestamp now = new Timestamp(System.currentTimeMillis());
    InvoicePaymentHistoryDaoImp historyDao = new InvoicePaymentHistoryDaoImp();
    
    // Calculate percentage change
    float percentageChange = 1 - (totalRemaining + totalDifference) / totalRemaining;
    
    for (InvoiceHeader header : headers) {
        if (header.getRemaining() > 0) {
            // Calculate the change for this invoice (proportional to its remaining amount)
            float change = header.getRemaining() * percentageChange;
            
            // Update income and remaining
            float newIncome = header.getIncome() + change;
            float newRemaining = header.getRemaining() - change;
            
            // Ensure we don't go negative
            if (newRemaining < 0) {
                newRemaining = 0;
                newIncome = header.getTotalAmount();
            }
            
            // Update the header
            header.setIncome(newIncome);
            header.setRemaining(newRemaining);
            header.setUpdatedAt(now);
            
            // Save the updated header
            invoiceHeaderDao.save(header);
            
            // Save the payment history (only if there was a payment, not if remaining increased)
            if (change > 0) {
                InvoicePaymentHistory paymentHistory = new InvoicePaymentHistory();
                paymentHistory.setInvoice_header_id(header.getId());
                paymentHistory.setAmount_paid(change);
                paymentHistory.setIncome(newIncome);
                paymentHistory.setCreatedAt(now);
                paymentHistory.setUpdatedAt(now);
                historyDao.save(paymentHistory);
            }
        }
    }
}

// Save a record of the global payment/adjustment to history
private void savePaymentHistory(float difference) {
    // Only save history if it's a payment (reduction in remaining)
    if (difference < 0) {
        // Add new entry to the left table's model for display
        DefaultTableModel model = (DefaultTableModel) leftTable.getModel();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String dateStr = dateFormat.format(new Date());
        
        // Add as first row (newest at top)
        model.insertRow(0, new Object[] {
            String.format("%.2f", Math.abs(difference)),
            dateStr
        });
        
        // If we have too many entries, remove oldest
        if (model.getRowCount() > 50) {
            model.removeRow(model.getRowCount() - 1);
        }
    }
}

// Update the payment history table in the left panel
public void updatePaymentHistoryTable() {
    // Clear the table
    DefaultTableModel model = (DefaultTableModel) leftTable.getModel();
    model.setRowCount(0);
    
    // Get all payment history for this client
    InvoicePaymentHistoryDaoImp historyDao = new InvoicePaymentHistoryDaoImp();
    List<InvoicePaymentHistory> allHistory = new ArrayList<>();
    
    // For each invoice header, get its payment history
    List<InvoiceHeader> headers = invoiceHeaderDao.findByClientId(clientId);
    for (InvoiceHeader header : headers) {
        List<InvoicePaymentHistory> headerHistory = historyDao.findByInvoiceHeaderId(header.getId());
        allHistory.addAll(headerHistory);
    }
    
    // Sort by date (newest first)
    allHistory.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
    
    // Add to table (limit to recent 30 entries)
    SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    int count = 0;
    for (InvoicePaymentHistory payment : allHistory) {
        if (count++ >= 30) break;
        
        model.addRow(new Object[] {
            String.format("%.2f", payment.getAmount_paid()),
            dateFormat.format(payment.getCreatedAt())
        });
    }
}
private JPanel createLeftPanel() {
    JPanel leftPanel = new JPanel(new BorderLayout());
    leftPanel.setPreferredSize(new Dimension(400, 0));
    leftPanel.setBorder(BorderFactory.createTitledBorder("بيانات اجمالي التحديثات"));
    leftPanel.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

    // Table columns and model
    String[] columns = {"المبلغ", "التاريخ"};
    DefaultTableModel leftTableModel = new DefaultTableModel(columns, 0);
    leftTable = new JTable(leftTableModel);

    // Style the table header with different color
    leftTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
    leftTable.getTableHeader().setBackground(new Color(200, 220, 240));
    leftTable.setFont(new Font("Arial", Font.PLAIN, 14));
    leftTable.setRowHeight(25);
    leftTable.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

    // Right-align columns
    DefaultTableCellRenderer rightAlign = new DefaultTableCellRenderer();
    rightAlign.setHorizontalAlignment(JLabel.RIGHT);
    for (int i = 0; i < leftTable.getColumnCount(); i++) {
        leftTable.getColumnModel().getColumn(i).setCellRenderer(rightAlign);
    }

    JScrollPane leftScroll = new JScrollPane(leftTable);
    leftScroll.setPreferredSize(new Dimension(380, 300));
    leftScroll.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    leftScroll.setBorder(BorderFactory.createEmptyBorder(30, 0, 0, 0));
    leftPanel.add(leftScroll, BorderLayout.NORTH);

    // Update panel with label, text field, and button
    JPanel updatePanel = new JPanel(new GridBagLayout());
updatePanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
updatePanel.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 5, 10, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.LINE_END;

    JLabel updateLabel = new JLabel("تحديث اجمالي المتبقي للعميل:");
    updateLabel.setFont(new Font("Arial", Font.BOLD, 14));

    updateField = new JTextField();
    updateField.setFont(new Font("Arial", Font.PLAIN, 14));
    updateField.setHorizontalAlignment(JTextField.RIGHT);

    JButton updateRemainingButton = new JButton("تحديث");
    updateRemainingButton.setFont(new Font("Arial", Font.BOLD, 14));
updateRemainingButton.addActionListener(e -> updateTotalRemaining());

    // Add action to update button here or in caller

    // Add components to updatePanel
// Label
gbc.gridx = 0;
gbc.gridy = 0;
gbc.weightx = 0.3;
updatePanel.add(updateLabel, gbc);

// TextField
gbc.gridx = 1;
gbc.weightx = 0.7;
updatePanel.add(updateField, gbc);

// Button — make it NOT stretch and span full width
gbc.gridx = 0;
gbc.gridy = 1;
gbc.gridwidth = 2;
gbc.weightx = 0;                 // No horizontal stretching
gbc.fill = GridBagConstraints.NONE;  // Don't stretch button
updateRemainingButton.addActionListener(e -> updateIncomeForInvoices());

updatePanel.add(updateRemainingButton, gbc);

    leftPanel.add(updatePanel, BorderLayout.CENTER);

    return leftPanel;
}
private JPanel createHeader() {
        JLabel headerLabel = new JLabel("ادارة فواتير العميل: "+clientName);
        headerLabel.setFont(new Font("Times New Roman", Font.BOLD, 24));
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerLabel.setBorder(new EmptyBorder(10, 0, 5, 0));
        setForeground(InvoiceHeaderColor);
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(241,241,241));
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        return headerPanel;
    }
private JButton createStyledButton(String text, java.awt.event.ActionListener listener) {
            JButton button = new JButton(text);
            button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
            button.setMargin(new Insets(8, 15, 8, 15));
            button.setFocusPainted(false);
            button.addActionListener(listener);
            return button;
        }
private void updateSelectedInvoiceHeader() {
    int selectedRow = invoiceHeaderTable.getTable().getSelectedRow();
    if (selectedRow == -1) {
        JOptionPane.showMessageDialog(this, "الرجاء اختيار فاتورة للتحديث", "تنبيه", JOptionPane.WARNING_MESSAGE);
        return;
    }

    int invoiceId = (int) invoiceHeaderTable.getTable().getValueAt(selectedRow, 0);
    InvoiceHeader header = invoiceHeaderDao.findById(invoiceId);

    if (header == null) {
        JOptionPane.showMessageDialog(this, "الفاتورة غير موجودة", "خطأ", JOptionPane.ERROR_MESSAGE);
        return;
    }

    if (header.getRemaining() == 0) {
        JOptionPane.showMessageDialog(this, "لا يوجد متبقي لتحديثه — الفاتورة مدفوعة بالكامل", "تنبيه", JOptionPane.WARNING_MESSAGE);
    }
   
    // Create a custom dialog with better layout
    JDialog paymentDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "تحديث الفاتورة", true);
    paymentDialog.setLayout(new BorderLayout());
    
    // Set RTL orientation for the entire dialog
    paymentDialog.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    
    // Set a consistent font for all components
    Font arabicFont = new Font("Arial", Font.PLAIN, 14);
    Font boldArabicFont = new Font("Arial", Font.BOLD, 14);
    
    // Main panel with padding
    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    mainPanel.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    
    // Header info panel
    JPanel headerInfoPanel = new JPanel(new GridLayout(3, 2, 10, 5));
    headerInfoPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "معلومات الفاتورة"));
    headerInfoPanel.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    
    // Add invoice details
    JLabel invoiceIdLabel = new JLabel("رقم الفاتورة:");
    JLabel invoiceIdValue = new JLabel(String.valueOf(header.getId()));
    JLabel totalAmountLabel = new JLabel("المبلغ الإجمالي:");
    JLabel totalAmountValue = new JLabel(String.format("%.2f", header.getTotalAmount()));
    JLabel paidAmountLabel = new JLabel("المدفوع:");
    JLabel paidAmountValue = new JLabel(String.format("%.2f", header.getIncome()));
    
    invoiceIdLabel.setFont(boldArabicFont);
    invoiceIdValue.setFont(arabicFont);
    totalAmountLabel.setFont(boldArabicFont);
    totalAmountValue.setFont(arabicFont);
    paidAmountLabel.setFont(boldArabicFont);
    paidAmountValue.setFont(arabicFont);
    
    // Ensure correct text alignment for Arabic
    invoiceIdLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    invoiceIdValue.setHorizontalAlignment(SwingConstants.RIGHT);
    totalAmountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    totalAmountValue.setHorizontalAlignment(SwingConstants.RIGHT);
    paidAmountLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    paidAmountValue.setHorizontalAlignment(SwingConstants.RIGHT);
    
    headerInfoPanel.add(invoiceIdLabel);
    headerInfoPanel.add(invoiceIdValue);
    headerInfoPanel.add(totalAmountLabel);
    headerInfoPanel.add(totalAmountValue);
    headerInfoPanel.add(paidAmountLabel);
    headerInfoPanel.add(paidAmountValue);
    
    // Fetch payment history
    InvoicePaymentHistoryDaoImp historyDao = new InvoicePaymentHistoryDaoImp();
    List<InvoicePaymentHistory> paymentHistoryList = historyDao.findByInvoiceHeaderId(invoiceId);

    // Payment history panel
    JPanel historyPanel = new JPanel(new BorderLayout());
    historyPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "سجل الدفعات"));
    historyPanel.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    
    // Create a formatted table for payment history
    String[] columns = {"المبلغ", "التاريخ"};
    DefaultTableModel historyModel = new DefaultTableModel(columns, 0);
    
    if (!paymentHistoryList.isEmpty()) {
        for (InvoicePaymentHistory payment : paymentHistoryList) {
            historyModel.addRow(new Object[]{
                String.format("%.2f", payment.getAmount_paid()),
                payment.getCreatedAt().toString()
            });
        }
    }
    
    JTable historyTable = new JTable(historyModel);
    historyTable.setFont(arabicFont);
    historyTable.getTableHeader().setFont(boldArabicFont);
    historyTable.setRowHeight(25);
    historyTable.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    
    // Set column alignments for RTL display
    DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
    rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
    historyTable.getColumnModel().getColumn(0).setCellRenderer(rightRenderer);
    historyTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);
    
    // Adjust column order for proper RTL display
    TableColumnModel columnModel = historyTable.getColumnModel();
    historyTable.setAutoCreateColumnsFromModel(false);
    
    JScrollPane historyScroll = new JScrollPane(historyTable);
    historyScroll.setPreferredSize(new Dimension(350, 150));
    historyScroll.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    historyPanel.add(historyScroll, BorderLayout.CENTER);
    
    // If no payments, show message
    if (paymentHistoryList.isEmpty()) {
        JLabel noHistoryLabel = new JLabel("لا يوجد دفعات سابقة", JLabel.CENTER);
        noHistoryLabel.setFont(arabicFont);
        noHistoryLabel.setForeground(Color.GRAY);
        historyPanel.add(noHistoryLabel, BorderLayout.CENTER);
    }
    
    // New payment panel
    JPanel newPaymentPanel = new JPanel(new GridLayout(3, 2, 10, 10));
    newPaymentPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "دفعة جديدة"));
    newPaymentPanel.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    
    JLabel remainingLabel = new JLabel("المتبقي:");
    JLabel remainingValue = new JLabel(String.format("%.2f", header.getRemaining()));
    remainingValue.setForeground(new Color(204, 0, 0)); // Red for emphasis
    
    JLabel newPaymentLabel = new JLabel("المبلغ الجديد للدفع:");
    JTextField incomeField = new JTextField(String.valueOf(header.getRemaining()));
    incomeField.setHorizontalAlignment(JTextField.RIGHT); // Right-align text field content
    
    JLabel newRemainingLabel = new JLabel("المتبقي بعد الدفع:");
    JLabel newRemainingValue = new JLabel("0.00");
    
    remainingLabel.setFont(boldArabicFont);
    remainingValue.setFont(boldArabicFont);
    newPaymentLabel.setFont(boldArabicFont);
    incomeField.setFont(arabicFont);
    newRemainingLabel.setFont(boldArabicFont);
    newRemainingValue.setFont(boldArabicFont);
    
    // Set text alignment for labels
    remainingLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    remainingValue.setHorizontalAlignment(SwingConstants.RIGHT);
    newPaymentLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    newRemainingLabel.setHorizontalAlignment(SwingConstants.RIGHT);
    newRemainingValue.setHorizontalAlignment(SwingConstants.RIGHT);
    
    // Update remaining amount when user types
    incomeField.getDocument().addDocumentListener(new DocumentListener() {
        private void updateRemaining() {
            try {
                float newIncome = Float.parseFloat(incomeField.getText());
                if (newIncome >= 0) {
                    float remaining = header.getRemaining() - newIncome;
                    newRemainingValue.setText(String.format("%.2f", remaining));
                    
                    // Change color based on remaining amount
                    if (remaining < 0) {
                        newRemainingValue.setForeground(new Color(204, 0, 0)); // Red for overpayment
                    } else if (remaining == 0) {
                        newRemainingValue.setForeground(new Color(0, 128, 0)); // Green for exact payment
                    } else {
                        newRemainingValue.setForeground(Color.BLACK); // Black for partial payment
                    }
                }
            } catch (NumberFormatException e) {
                newRemainingValue.setText("---");
                newRemainingValue.setForeground(Color.BLACK);
            }
        }

        @Override
        public void insertUpdate(DocumentEvent e) { updateRemaining(); }
        @Override
        public void removeUpdate(DocumentEvent e) { updateRemaining(); }
        @Override
        public void changedUpdate(DocumentEvent e) { updateRemaining(); }
    });    
    
    // Initial calculation
    try {
        float newIncome = Float.parseFloat(incomeField.getText());
        float remaining = header.getRemaining() - newIncome;
        newRemainingValue.setText(String.format("%.2f", remaining));
    } catch (NumberFormatException e) {
        // Default handling
    }
    
    newPaymentPanel.add(remainingLabel);
    newPaymentPanel.add(remainingValue);
    newPaymentPanel.add(newPaymentLabel);
    newPaymentPanel.add(incomeField);
    newPaymentPanel.add(newRemainingLabel);
    newPaymentPanel.add(newRemainingValue);
    
    // Button panel
    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
    buttonPanel.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
    
    JButton saveButton = new JButton("حفظ الدفعة");
    JButton cancelButton = new JButton("إلغاء");
    
    saveButton.setFont(boldArabicFont);
    cancelButton.setFont(arabicFont);
    
    // Add action listeners
    saveButton.addActionListener(e -> {
        try {
            float additionalIncome = Float.parseFloat(incomeField.getText());
            if (additionalIncome == 0) {
                JOptionPane.showMessageDialog(paymentDialog, "الرجاء إدخال مبلغ أكبر من او اقل من 0", "تحذير", JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Update income
            float newIncome = header.getIncome() + additionalIncome;
            header.setIncome(newIncome);
            header.setRemaining(header.getTotalAmount() - newIncome);
            header.setUpdatedAt(new java.sql.Timestamp(System.currentTimeMillis()));

            // Save updated invoice
            invoiceHeaderDao.save(header);

            // Save payment history
            InvoicePaymentHistory newPayment = new InvoicePaymentHistory();
            newPayment.setInvoice_header_id(header.getId());
            newPayment.setAmount_paid(additionalIncome);
            newPayment.setIncome(newIncome);
            Timestamp now = new Timestamp(System.currentTimeMillis());
            newPayment.setCreatedAt(now);
            newPayment.setUpdatedAt(now);
            historyDao.save(newPayment);

            loadInvoiceHeaderData();
            paymentDialog.dispose();
            JOptionPane.showMessageDialog(this, "تم تحديث الفاتورة بنجاح", "نجاح", JOptionPane.INFORMATION_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(paymentDialog, "الرجاء إدخال رقم صحيح للمبلغ", "خطأ", JOptionPane.ERROR_MESSAGE);
        }
    });    
    
    cancelButton.addActionListener(e -> paymentDialog.dispose());
    
    buttonPanel.add(saveButton);
    buttonPanel.add(cancelButton);
    
    // Add all panels to main panel
    mainPanel.add(headerInfoPanel);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    mainPanel.add(historyPanel);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    mainPanel.add(newPaymentPanel);
    
    // Add main panel and button panel to dialog
    paymentDialog.add(mainPanel, BorderLayout.CENTER);
    paymentDialog.add(buttonPanel, BorderLayout.SOUTH);
    
    // Set dialog properties
    paymentDialog.pack();
    paymentDialog.setLocationRelativeTo(this);
    paymentDialog.setResizable(false);
    paymentDialog.setVisible(true);
    
    // Add this to ensure RTL UI for option panes within this context
    JOptionPane.getFrameForComponent(paymentDialog).applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
}        
private void deleteSelectedInvoiceHeader() {
            int selectedRow = invoiceHeaderTable.getTable().getSelectedRow();
            if (selectedRow == -1) {
                JOptionPane.showMessageDialog(this, "الرجاء اختيار فاتورة للحذف", "تنبيه", JOptionPane.WARNING_MESSAGE);
                return;
            }

            int invoiceId = (int) invoiceHeaderTable.getTable().getValueAt(selectedRow, 0);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "هل أنت متأكد من حذف هذه الفاتورة؟\nسيتم حذف جميع تفاصيل الفاتورة أيضاً",
                    "تأكيد الحذف",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                if (invoiceHeaderDao.delete(invoiceId)) {
                    loadInvoiceHeaderData();
                    JOptionPane.showMessageDialog(this, "تم حذف الفاتورة بنجاح",
                            "نجاح", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "حدث خطأ أثناء حذف الفاتورة",
                            "خطأ", JOptionPane.ERROR_MESSAGE);
                }
            }
        }
private void addNewInvoiceHeader() throws ParseException {
            System.out.println("Adding new invoice...");
            Timestamp timestamp=new  Timestamp(System.currentTimeMillis());
    SimpleDateFormat sdf=new SimpleDateFormat("YY-M-d H-m");
    String date= sdf.format(timestamp);
    Date dateUtil= sdf.parse(date);

    Timestamp DateTimeStamps=new Timestamp(dateUtil.getTime());
    sdf.format(dateUtil);
            // Create a new InvoiceHeader instance
            InvoiceHeader newHeader = new InvoiceHeader();
            newHeader.setClientId(clientId);
            newHeader.setCreatedAt(DateTimeStamps);
            newHeader.setUpdatedAt(DateTimeStamps);

            // Save the new invoice header and get the new ID
            int newId = invoiceHeaderDao.save(newHeader);
            System.out.println("New Invoice ID: " + newId);

            if (newId > 0) {
                // Open Invoice Details Frame
                InvoiceFrame invoiceDetailsFrame = new InvoiceFrame(clientId, clientName, clientPhone, userId, newId);
                invoiceDetailsFrame.setVisible(true);

                // ✅ Wait until the details frame is closed, then check if the invoice has items
                invoiceDetailsFrame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        int itemCount = invoiceHeaderDao.getInvoiceItemCount(newId);

                        //  If no items were added, delete the invoice
                        if (itemCount == 0) {
                            invoiceHeaderDao.delete(newId);
                            System.out.println("Deleted empty invoice ID: " + newId);
                        }
                    }
                });
            } else {
                JOptionPane.showMessageDialog(this, "فشل في إنشاء فاتورة جديدة", "خطأ", JOptionPane.ERROR_MESSAGE);
            }
        }
// Load Invoice Header Data
public  void loadInvoiceHeaderData() {
        // Fetch the data from the database using InvoiceHeaderDaoImp
        List<InvoiceHeader> headers = invoiceHeaderDao.findByClientId(clientId);

        // Prepare the data for the table, rearranged according to the new order
        List<Object[]> rowData = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");

         totalRemaining = 0f;  // Sum of all remaining values

        for (InvoiceHeader header : headers) {
            totalRemaining += header.getRemaining();  // Accumulate remaining

            Object[] row = new Object[]{
                header.getId(), // رقم الفاتورة
                header.getTotalAmount(), // المبلغ الكلي
                header.getIncome(), // الدخل
                header.getRemaining(), // المتبقي
                header.getPaymentMethod(),
                dateFormat.format(header.getCreatedAt()), // تاريخ الإنشاء
            };
            rowData.add(row);
        }

        // Update the GenericTable with the fetched and rearranged data
        invoiceHeaderTable.updateData(rowData);

        // Update the fullRemaining label
    //    if (fullRemainingLabel != null) {
    //    fullRemainingLabel.setText(String.format("المتبقي: %.2f", totalRemaining));
    //}
           try {
                   fullRemainingLabel.setText(String.format("المتبقي: %.2f", totalRemaining));

           } catch (Exception e) {
               e.printStackTrace();
           }
    }
// Open the InvoiceDetails frame for the selected invoice header
private void openInvoiceDetailsFrame(int invoiceHeaderId) {
            ClientDaoImp clientDao = new ClientDaoImp();
            Client client = clientDao.findById(clientId, userId);

            InvoiceHeaderDaoImp invoiceHeaderDaoImp = new InvoiceHeaderDaoImp();
            InvoiceHeader invoiceHeader = invoiceHeaderDaoImp.findById(invoiceHeaderId);

            if (client != null && invoiceHeader != null) {
                String clientName = client.getName();
                String clientPhone = client.getPhone();

                // Pass the invoiceHeaderId to the InvoiceFrame
                InvoiceFrame invoiceDetailsFrame = new InvoiceFrame(clientId, clientName, clientPhone, userId, invoiceHeaderId);

                // Disable the InvoiceHeaderFrame
                this.setEnabled(false);

                // Add a listener to re-enable when InvoiceFrame is closed
                invoiceDetailsFrame.addWindowListener(new WindowAdapter() {
                    @Override
                    public void windowClosed(WindowEvent e) {
                        setEnabled(true);  // Re-enable the InvoiceHeaderFrame
                        requestFocus(); // Bring focus back to the frame
                    }
                });

                invoiceDetailsFrame.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(this, "Client or Invoice details not found!", "Error", JOptionPane.ERROR_MESSAGE);
            }
        }

        // Method to check if frame is open
        public static boolean isFrameOpen() {
            return isOpen;
        }
    }
