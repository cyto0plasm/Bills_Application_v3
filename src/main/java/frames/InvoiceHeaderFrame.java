package frames;

import Data_Access_Object.Client.Client;
import Data_Access_Object.Client.ClientDaoImp;
import Data_Access_Object.InvoiceHeaders.InvoiceHeader;
import Data_Access_Object.InvoiceHeaders.InvoiceHeaderDaoImp;
import Data_Access_Object.invoice_payment_history.InvoicePaymentHistory;
import Data_Access_Object.invoice_payment_history.InvoicePaymentHistoryDao;
import Data_Access_Object.invoice_payment_history.InvoicePaymentHistoryDaoImp;
import Data_Access_Object.invoice_payment_history.InvoicePaymentService;
import frames.components.GenericTable;
import frames.components.InvoiceStatusRenderer;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
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
    private Color InvoiceHeaderColor = new Color(90, 106, 112);
    private JTextField updateField;
    private JTable leftTable;
private InvoicePaymentHistory lastPayment = null;

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

        String[] tableHeaders = {"رقم الفاتورة", "المبلغ الكلي", "الدخل", "الدخل الاولي","الخصم", "المتبقي", "طريقة الدفع", "تاريخ الإنشاء"};
        List<Integer> editableColumns = Arrays.asList();

        this.invoiceHeaderTable = new GenericTable(tableHeaders, new ArrayList<>(), false, editableColumns, InvoiceHeaderColor);

        setupUI();
        loadInvoiceHeaderData();
        setupKeyListeners();
    }

    public static InvoiceHeaderFrame getInstance() {
        return instance;
    }

    private void setupKeyListeners() {
        this.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    exitFrame();
                    e.consume();
                }
            }
        });
        this.setFocusable(true);
        this.requestFocusInWindow();
    }

    private void exitFrame() {
        int option = JOptionPane.showConfirmDialog(this,
                "هل تريد حقًا الخروج؟", "تأكيد الخروج", JOptionPane.YES_NO_OPTION);
        if (option == JOptionPane.YES_OPTION) {
            System.out.println("Disposing InvoiceFrame...");
            isOpen = false;
            dispose();
            System.out.println("Disposed of InvoiceFrame...");
        }
    }

    private void setupUI() {
        setTitle("فواتير العميل: " + clientName);
        setSize(1080, 720);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setLayout(new BorderLayout());
        invoiceHeaderTable.getTable().setName("invoiceHeaderTable");

        JPanel leftPanel = createLeftPanel();

        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, leftPanel, invoiceHeaderTable);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0);
        add(splitPane, BorderLayout.CENTER);

        add(createHeader(), BorderLayout.NORTH);

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

        buttonPanel.add(createStyledButton("حذف الفاتورة", e -> deleteSelectedInvoiceHeader()));
        buttonPanel.add(createStyledButton("تحديث البيانات", e -> loadInvoiceHeaderData()));
        buttonPanel.add(createStyledButton("تحديث الفاتورة", e->updateSelectedInvoiceHeader()));
        add(buttonPanel, BorderLayout.SOUTH);

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
        if (number == Math.floor(number)) {
            return String.format("%.0f", number);
        } else {
            return String.format("%.2f", number);
        }
    }
public static String mapArabicPaymentMethod(String selected) {
       switch (selected) {
        case "نقدي": return "cash";
        case "محفظة": return "wallet";
        case "بطاقة ائتمان": return "credit card";
        default: return ""; // <-- هذا سبب المشكلة
    }
}
private String mapPaymentMethodForDisplay(String method) {
    if (method == null) {
        return "غير معروف"; // أو قيمة افتراضية مناسبة
    }
    switch (method) {
        case "cash": return "نقدي";
        case "credit card": return "بطاقة ائتمان";
        case "wallet": return "محفظة";
        default: return "غير معروف";
    }
}

private void updateTotalRemaining(boolean applyToOldestFirst, String paymentMethod) {
    String input = updateField.getText().trim();

    try {
        float paymentAmount = Float.parseFloat(input);
        if (paymentAmount <= 0 || paymentAmount > totalRemaining) {
            throw new IllegalArgumentException("قيمة غير صحيحة");
        }

        InvoicePaymentService service = new InvoicePaymentService(invoiceHeaderDao, new InvoicePaymentHistoryDaoImp());
        
        // Now pass the paymentMethod to your service
        boolean success = service.applyPaymentToInvoices(clientId, paymentAmount, applyToOldestFirst, paymentMethod);

        if (success) {
            loadInvoiceHeaderData();       // Refresh left panel or invoice list
            updatePaymentHistoryTable();   // Reload payment history table
            updateField.setText("");       // Clear input field

            JOptionPane.showMessageDialog(this, "تم التحديث بنجاح", "نجاح", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "لم يتم تطبيق الدفعة", "تنبيه", JOptionPane.WARNING_MESSAGE);
        }
    } catch (NumberFormatException ex) {
        JOptionPane.showMessageDialog(this, "الرجاء إدخال قيمة رقمية", "خطأ", JOptionPane.ERROR_MESSAGE);
    } catch (IllegalArgumentException ex) {
        JOptionPane.showMessageDialog(this, ex.getMessage(), "تنبيه", JOptionPane.WARNING_MESSAGE);
    } catch (Exception ex) {
        JOptionPane.showMessageDialog(this, "حدث خطأ: " + ex.getMessage(), "خطأ", JOptionPane.ERROR_MESSAGE);
        ex.printStackTrace();
    }
}
private JPanel createLeftPanel() {
    JPanel leftPanel = new JPanel(new BorderLayout());
    leftPanel.setPreferredSize(new Dimension(400, 0));
    leftPanel.setBorder(BorderFactory.createTitledBorder("بيانات اجمالي التحديثات"));
    leftPanel.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

    // Columns: Amount, Payment Method, Date
    String[] columns = {"المبلغ", "طريقة الدفع", "التاريخ"};
    DefaultTableModel leftTableModel = new DefaultTableModel(columns, 0);
    leftTable = new JTable(leftTableModel);

    leftTable.getTableHeader().setFont(new Font("Arial", Font.BOLD, 14));
    leftTable.getTableHeader().setBackground(new Color(235, 200, 91));
    leftTable.setFont(new Font("Arial", Font.PLAIN, 14));
    leftTable.setRowHeight(25);
    leftTable.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

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

    JPanel updatePanel = new JPanel(new GridBagLayout());
    updatePanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
    updatePanel.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(10, 5, 10, 5);
    gbc.fill = GridBagConstraints.HORIZONTAL;
    gbc.anchor = GridBagConstraints.LINE_END;

    JLabel updateLabel = new JLabel("إضافة دفعة جديدة:");
    updateLabel.setFont(new Font("Arial", Font.BOLD, 14));

    updateField = new JTextField();
    updateField.setFont(new Font("Arial", Font.PLAIN, 14));
    updateField.setHorizontalAlignment(JTextField.RIGHT);

    JLabel methodLabel = new JLabel("طريقة الدفع:");
    methodLabel.setFont(new Font("Arial", Font.BOLD, 14));

    JComboBox<String> methodCombo = new JComboBox<>(new String[]{"نقدي", "بطاقة ائتمان", "محفظة"});
    methodCombo.setFont(new Font("Arial", Font.PLAIN, 14));
    methodCombo.setPreferredSize(new Dimension(150, 25));

    JPanel orderPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
    orderPanel.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

    JRadioButton oldestFirstRadio = new JRadioButton("الفواتير الأقدم أولاً", true);
    JRadioButton newestFirstRadio = new JRadioButton("الفواتير الأحدث أولاً");

    ButtonGroup orderGroup = new ButtonGroup();
    orderGroup.add(oldestFirstRadio);
    orderGroup.add(newestFirstRadio);

    orderPanel.add(oldestFirstRadio);
    orderPanel.add(newestFirstRadio);

    JButton updateButton = new JButton("إضافة دفعة");
    updateButton.setFont(new Font("Arial", Font.BOLD, 14));

    updateButton.addActionListener(e -> {
        boolean applyToOldestFirst = oldestFirstRadio.isSelected();
       String selectedMethod = (String) methodCombo.getSelectedItem();
String mappedMethod = mapArabicPaymentMethod(selectedMethod);
updateTotalRemaining(applyToOldestFirst, mappedMethod);
    });

    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.weightx = 0.3;
    updatePanel.add(updateLabel, gbc);

    gbc.gridx = 1;
    gbc.weightx = 0.7;
    updatePanel.add(updateField, gbc);

    gbc.gridx = 0;
    gbc.gridy = 1;
    gbc.weightx = 0.3;
    updatePanel.add(methodLabel, gbc);

    gbc.gridx = 1;
    gbc.weightx = 0.7;
    updatePanel.add(methodCombo, gbc);

    gbc.gridx = 0;
    gbc.gridy = 2;
    gbc.gridwidth = 2;
    updatePanel.add(orderPanel, gbc);

    gbc.gridx = 0;
    gbc.gridy = 3;
    gbc.gridwidth = 2;
    gbc.weightx = 0;
    gbc.fill = GridBagConstraints.NONE;
    updatePanel.add(updateButton, gbc);

    leftPanel.add(updatePanel, BorderLayout.CENTER);

    return leftPanel;
}

    private JPanel createHeader() {
        JLabel headerLabel = new JLabel("ادارة فواتير العميل: " + clientName);
        headerLabel.setFont(new Font("Times New Roman", Font.BOLD, 24));
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerLabel.setBorder(new EmptyBorder(10, 0, 5, 0));
        setForeground(InvoiceHeaderColor);
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(241, 241, 241));
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
        return;
    }

    JDialog paymentDialog = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), "تحديث الفاتورة", true);
    paymentDialog.setLayout(new BorderLayout());
    paymentDialog.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

    Font arabicFont = new Font("Arial", Font.PLAIN, 14);
    Font boldArabicFont = new Font("Arial", Font.BOLD, 14);

    JPanel mainPanel = new JPanel();
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
    mainPanel.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

    JPanel headerInfoPanel = new JPanel(new GridLayout(3, 2, 10, 5));
    headerInfoPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "معلومات الفاتورة"));
    headerInfoPanel.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

    JLabel invoiceIdLabel = new JLabel("رقم الفاتورة:");
    JLabel invoiceIdValue = new JLabel(String.valueOf(header.getId()));
    JLabel totalAmountLabel = new JLabel("المبلغ الإجمالي:");
    JLabel totalAmountValue = new JLabel(String.format("%.2f", header.getTotalAmount()));
    JLabel paidAmountLabel = new JLabel("المدفوع:");
    JLabel paidAmountValue = new JLabel(String.format("%.2f", header.getIncome()));

    JLabel[] infoLabels = {invoiceIdLabel, invoiceIdValue, totalAmountLabel, totalAmountValue, paidAmountLabel, paidAmountValue};
    for (JLabel label : infoLabels) {
        label.setFont(label == invoiceIdLabel || label == totalAmountLabel || label == paidAmountLabel ? boldArabicFont : arabicFont);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
    }

    headerInfoPanel.add(invoiceIdLabel);
    headerInfoPanel.add(invoiceIdValue);
    headerInfoPanel.add(totalAmountLabel);
    headerInfoPanel.add(totalAmountValue);
    headerInfoPanel.add(paidAmountLabel);
    headerInfoPanel.add(paidAmountValue);

    InvoicePaymentHistoryDaoImp historyDao = new InvoicePaymentHistoryDaoImp();
    List<InvoicePaymentHistory> paymentHistoryList = historyDao.findByInvoiceHeaderId(invoiceId);

    JPanel historyPanel = new JPanel(new BorderLayout());
    historyPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "سجل الدفعات"));
    historyPanel.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

    String[] columns = {"المبلغ", "طريقة الدفع", "التاريخ"};
DefaultTableModel historyModel = new DefaultTableModel(columns, 0) {
    @Override
    public boolean isCellEditable(int row, int column) {
        return false; // Make all cells non-editable
    }
};
    for (InvoicePaymentHistory payment : paymentHistoryList) {
        historyModel.addRow(new Object[]{
            String.format("%.2f", payment.getAmount_paid()),
            mapPaymentMethodForDisplay(payment.getPayment_method()),
            payment.getCreatedAt().toString()
        });
    }

    if (paymentHistoryList.isEmpty()) {
        JLabel noHistoryLabel = new JLabel("لا يوجد دفعات سابقة", JLabel.CENTER);
        noHistoryLabel.setFont(arabicFont);
        noHistoryLabel.setForeground(Color.GRAY);
        historyPanel.add(noHistoryLabel, BorderLayout.CENTER);
    } else {
        JTable historyTable = new JTable(historyModel);
        
        historyTable.setFont(arabicFont);
        historyTable.getTableHeader().setFont(boldArabicFont);
        historyTable.setRowHeight(25);
        historyTable.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

        DefaultTableCellRenderer rightRenderer = new DefaultTableCellRenderer();
        rightRenderer.setHorizontalAlignment(JLabel.RIGHT);
        historyTable.getColumnModel().getColumn(0).setCellRenderer(rightRenderer);
        historyTable.getColumnModel().getColumn(1).setCellRenderer(rightRenderer);

        JScrollPane historyScroll = new JScrollPane(historyTable);
        historyScroll.setPreferredSize(new Dimension(350, 150));
        historyScroll.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        historyPanel.add(historyScroll, BorderLayout.CENTER);
    }

    JPanel newPaymentPanel = new JPanel(new GridLayout(4, 2, 10, 10));
    newPaymentPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(), "دفعة جديدة"));
    newPaymentPanel.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

    JLabel remainingLabel = new JLabel("المتبقي:");
    JLabel remainingValue = new JLabel(String.format("%.2f", header.getRemaining()));
    remainingValue.setForeground(new Color(204, 0, 0));

    JLabel newPaymentLabel = new JLabel("المبلغ الجديد للدفع:");
    JTextField incomeField = new JTextField(String.valueOf(header.getRemaining()));
    incomeField.setHorizontalAlignment(JTextField.RIGHT);

    JLabel methodLabel = new JLabel("طريقة الدفع:");
    String[] paymentMethods = {"نقدي", "بطاقة ائتمان", "محفظة"};
    JComboBox<String> methodCombo = new JComboBox<>(paymentMethods);
    
    methodCombo.setFont(arabicFont);
methodCombo.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

    JLabel newRemainingLabel = new JLabel("المتبقي بعد الدفع:");
    JLabel newRemainingValue = new JLabel("0.00");

    JLabel[] newPaymentLabels = {remainingLabel, newPaymentLabel, newRemainingLabel, newRemainingValue,methodLabel};
    for (JLabel label : newPaymentLabels) {
        label.setFont(boldArabicFont);
        label.setHorizontalAlignment(SwingConstants.RIGHT);
    }

    incomeField.setFont(arabicFont);
    incomeField.getDocument().addDocumentListener(new DocumentListener() {
        private void updateRemaining() {
            try {
                float newIncome = Float.parseFloat(incomeField.getText());
                float remaining = header.getRemaining() - newIncome;
                newRemainingValue.setText(String.format("%.2f", remaining));
                newRemainingValue.setForeground(
                    remaining < 0 ? new Color(204, 0, 0) :
                    remaining == 0 ? new Color(0, 128, 0) :
                    Color.BLACK
                );
            } catch (NumberFormatException e) {
                newRemainingValue.setText("---");
                newRemainingValue.setForeground(Color.BLACK);
            }
        }
        public void insertUpdate(DocumentEvent e) { updateRemaining(); }
        public void removeUpdate(DocumentEvent e) { updateRemaining(); }
        public void changedUpdate(DocumentEvent e) { updateRemaining(); }
    });

    newPaymentPanel.add(remainingLabel);
    newPaymentPanel.add(remainingValue);
    newPaymentPanel.add(newPaymentLabel);
    newPaymentPanel.add(incomeField);
    newPaymentPanel.add(methodLabel);
    newPaymentPanel.add(methodCombo);
    newPaymentPanel.add(newRemainingLabel);
    newPaymentPanel.add(newRemainingValue);

    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
    buttonPanel.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);

    JButton saveButton = new JButton("حفظ الدفعة");
    JButton cancelButton = new JButton("إلغاء");

    saveButton.setFont(boldArabicFont);
    cancelButton.setFont(arabicFont);

    saveButton.addActionListener(e -> {
        try {
            float additionalIncome = Float.parseFloat(incomeField.getText());
            if (additionalIncome <= 0) {
                JOptionPane.showMessageDialog(paymentDialog, "الرجاء إدخال مبلغ أكبر من 0", "تحذير", JOptionPane.ERROR_MESSAGE);
                return;
            }
            if (additionalIncome > header.getRemaining()) {
                JOptionPane.showMessageDialog(paymentDialog, "لا يمكن دفع أكثر من المبلغ المتبقي", "خطأ", JOptionPane.ERROR_MESSAGE);
                return;
            }

            String selectedArabicMethod = (String) methodCombo.getSelectedItem();
            String mappedMethod = mapArabicPaymentMethod(selectedArabicMethod);

            float newIncome = header.getIncome() + additionalIncome;
            header.setIncome(newIncome);
            header.setRemaining(header.getTotalAmount() - newIncome);
            header.setUpdatedAt(new Timestamp(System.currentTimeMillis()));
            invoiceHeaderDao.save(header);

            InvoicePaymentHistory newPayment = new InvoicePaymentHistory();
            newPayment.setInvoice_header_id(header.getId());
            newPayment.setAmount_paid(additionalIncome);
            newPayment.setIncome(newIncome);
            newPayment.setPayment_method(mappedMethod);
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

    mainPanel.add(headerInfoPanel);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    mainPanel.add(historyPanel);
    mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
    mainPanel.add(newPaymentPanel);

    paymentDialog.add(mainPanel, BorderLayout.CENTER);
    paymentDialog.add(buttonPanel, BorderLayout.SOUTH);

    paymentDialog.pack();
    paymentDialog.setLocationRelativeTo(this);
    paymentDialog.setResizable(false);
    paymentDialog.setVisible(true);

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
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        SimpleDateFormat sdf = new SimpleDateFormat("YY-M-d H-m");
        String date = sdf.format(timestamp);
        Date dateUtil = sdf.parse(date);

        Timestamp DateTimeStamps = new Timestamp(dateUtil.getTime());
        sdf.format(dateUtil);

        InvoiceHeader newHeader = new InvoiceHeader();
        newHeader.setClientId(clientId);
        newHeader.setDiscount(0);
        
        newHeader.setCreatedAt(DateTimeStamps);
        newHeader.setUpdatedAt(DateTimeStamps);

        int newId = invoiceHeaderDao.save(newHeader);
        System.out.println("New Invoice ID: " + newId);

        if (newId > 0) {
            InvoiceFrame invoiceDetailsFrame = new InvoiceFrame(clientId, clientName, clientPhone, userId, newId);
            invoiceDetailsFrame.setVisible(true);

            invoiceDetailsFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    int itemCount = invoiceHeaderDao.getInvoiceItemCount(newId);
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
    public void loadInvoiceHeaderData() {
        List<InvoiceHeader> headers = invoiceHeaderDao.findByClientId(clientId);

        List<Object[]> rowData = new ArrayList<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss a");

        totalRemaining = 0f;

        for (InvoiceHeader header : headers) {
            totalRemaining += header.getRemaining();

            Object[] row = new Object[]{
                    header.getId(),
                    header.getTotalAmount(),
                    header.getIncome(),
                    header.getFirstIncome(),
                    header.getDiscount(),
                    header.getRemaining(),
                    header.getPaymentMethod(),
                    dateFormat.format(header.getCreatedAt()),
            };
            rowData.add(row);
        }

        invoiceHeaderTable.updateData(rowData);

        try {
            fullRemainingLabel.setText(String.format("المتبقي: %.2f", totalRemaining));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void openInvoiceDetailsFrame(int invoiceHeaderId) {
        ClientDaoImp clientDao = new ClientDaoImp();
        Client client = clientDao.findById(clientId, userId);

        InvoiceHeaderDaoImp invoiceHeaderDaoImp = new InvoiceHeaderDaoImp();
        InvoiceHeader invoiceHeader = invoiceHeaderDaoImp.findById(invoiceHeaderId);

        if (client != null && invoiceHeader != null) {
            String clientName = client.getName();
            String clientPhone = client.getPhone();

            InvoiceFrame invoiceDetailsFrame = new InvoiceFrame(clientId, clientName, clientPhone, userId, invoiceHeaderId);

            this.setEnabled(false);

            invoiceDetailsFrame.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent e) {
                    setEnabled(true);
                    requestFocus();
                }
            });

            invoiceDetailsFrame.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Client or Invoice details not found!", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    public static boolean isFrameOpen() {
        return isOpen;
    }

    public void updatePaymentHistoryTable() {
        DefaultTableModel model = (DefaultTableModel) leftTable.getModel();
        model.setRowCount(0);

        InvoicePaymentHistoryDaoImp historyDao = new InvoicePaymentHistoryDaoImp();
        List<InvoicePaymentHistory> allHistory = new ArrayList<>();

        List<InvoiceHeader> headers = invoiceHeaderDao.findByClientId(clientId);
        for (InvoiceHeader header : headers) {
            List<InvoicePaymentHistory> headerHistory = historyDao.findByInvoiceHeaderId(header.getId());
            allHistory.addAll(headerHistory);
        }

        allHistory.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        int count = 0;
        for (InvoicePaymentHistory payment : allHistory) {
            if (count++ >= 30) break;

            model.addRow(new Object[]{
                    String.format("%.2f", payment.getAmount_paid()),
                      mapPaymentMethodForDisplay(payment.getPayment_method()),
                    dateFormat.format(payment.getCreatedAt())
            });
        }
    }
}