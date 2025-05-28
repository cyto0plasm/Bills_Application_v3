package frames;

import Data_Access_Object.Client.Client;
import Data_Access_Object.Client.ClientDaoImp;
import Data_Access_Object.user.User;
import com.formdev.flatlaf.FlatLightLaf;
import frames.components.GenericTable;
import lombok.Getter;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class ClientFrame extends JFrame {

    private static final String[] TABLE_HEADERS = {"رقم العميل", "الاسم", "رقم التليفون"};
    private static final String[] BUTTON_LABELS = {"إضافة", "تحديث", "حذف", "تحديث الكل", "تسجيل خروج"};
    private static final String[] BUTTON_TOOLTIPS={"اضافة عميل جديد ","تعديل بيانات العميل المحدد","حذف العميل المحدد","تحديث جدول العملاء","تسجيل الخروج من الحساب"};
    private static final Dimension TEXT_FIELD_SIZE = new Dimension(250, 35);

    @Getter
    private final GenericTable clientTable;
    private final ClientDaoImp clientDao;
    private final List<Object[]> clientsData;
    private final JLabel statusLabel;
    private final Timer messageTimer;
    private User currentUser;
    private Color ClientheaderColor=new Color(44, 46, 70);
    public ClientFrame(User user) {
        this.currentUser = user;
        this.clientDao = new ClientDaoImp();
        this.clientsData = new ArrayList<>();
        this.messageTimer = new Timer();
        this.statusLabel = createStyledLabel();
        List<Integer> editableColumns = Arrays.asList();

        this.clientTable = new GenericTable(TABLE_HEADERS, clientsData, false, editableColumns,ClientheaderColor);

        setTitle("العملاء - " + user.getName());
        initializeFrame();
        setupUI();
        addTableDoubleClickListener();
setupEscKeyToExitApp();
        loadClientData();
        setVisible(true);
    }
private void setupEscKeyToExitApp() {
    getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)
        .put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "ESC_EXIT_APP");

    getRootPane().getActionMap().put("ESC_EXIT_APP", new AbstractAction() {
        @Override
        public void actionPerformed(ActionEvent e) {
            int confirm = JOptionPane.showConfirmDialog(
                ClientFrame.this,
                "هل تريد إنهاء التطبيق؟",
                "تأكيد الخروج",
                JOptionPane.YES_NO_OPTION
            );

            if (confirm == JOptionPane.YES_OPTION) {
                System.exit(0); // Exit entire application
            }
        }
    });
}

// Method to handle frame exit logic
private void exitFrame() {
    int option = JOptionPane.showConfirmDialog(this,
            "هل تريد حقًا الخروج؟", "تأكيد الخروج", JOptionPane.YES_NO_OPTION);
    if (option == JOptionPane.YES_OPTION) {
        dispose(); // Close the frame
        System.exit(0); // Optionally exit the application
    }
}
    private void initializeFrame() {
        setTitle("العملاء");
        setSize(1200, 800);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
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

        mainPanel.add(createHeader(), BorderLayout.NORTH);
        mainPanel.add(createCenterPanel(), BorderLayout.CENTER);
        mainPanel.add(createButtonPanel(), BorderLayout.SOUTH);

        getContentPane().add(mainPanel);
    }

    private JPanel createHeader() {
        JLabel headerLabel = new JLabel("ادارة العملاء");
        headerLabel.setFont(new Font("Times New Roman", Font.BOLD, 24));
        headerLabel.setHorizontalAlignment(SwingConstants.CENTER);
        headerLabel.setBorder(new EmptyBorder(10, 0, 5, 0));
        headerLabel.setForeground(ClientheaderColor);
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(new Color(241,241,241));
        headerPanel.add(headerLabel, BorderLayout.CENTER);
        return headerPanel;
    }

    private JPanel createCenterPanel() {
        JPanel centerPanel = new JPanel(new BorderLayout());
        JPanel statusPanel = new JPanel(new BorderLayout());
        statusPanel.add(statusLabel, BorderLayout.CENTER);
        statusPanel.setPreferredSize(new Dimension(getWidth(), 30));

        centerPanel.add(statusPanel, BorderLayout.NORTH);
        centerPanel.add(new JScrollPane(clientTable), BorderLayout.CENTER);
        return centerPanel;
    }

    private JPanel createButtonPanel() {
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        buttonPanel.setBorder(new EmptyBorder(10, 0, 0, 0));

        Runnable[] actions = {this::addClient, this::updateClient,
            this::deleteClient, this::refreshTable, this::handleLogout};

        for (int i = 0; i < BUTTON_LABELS.length; i++) {
    JButton button = createStyledButton(BUTTON_LABELS[i], actions[i]);
    button.setToolTipText(BUTTON_TOOLTIPS[i]);  
    buttonPanel.add(button);
}

        return buttonPanel;
    }

    private JButton createStyledButton(String text, Runnable action) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        button.setMargin(new Insets(10, 20, 10, 20));
        button.setFocusPainted(false);
        button.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
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

    private void loadClientData() {
        clientsData.clear();
        // Use the updated findAll method with userId parameter
        clientDao.findAll(currentUser.getId()).forEach(client
                -> clientsData.add(new Object[]{client.getId(), client.getName(), client.getPhone()})
        );
        clientTable.updateData(clientsData);
    }

    private void addClient() {
        JTextField[] fields = showClientDialog("إضافة عميل جديد", null, null);
        if (fields == null) {
            return;
        }

        String name = fields[0].getText().trim();
        String phone = fields[1].getText().trim();

        if (validateInput(name, phone)) {
            // Create client with userId included in the constructor
            Client newClient = new Client(0, currentUser.getId(), name, phone, null, null);
            clientDao.save(newClient); // Updated to not pass userId separately
            refreshTable();
            showMessage("تمت إضافة العميل بنجاح!", false);
        }
    }

    private void updateClient() {
        int selectedRow = clientTable.getSelectedRow();
        if (selectedRow == -1) {
            showMessage("يرجى تحديد عميل لتحديثه!", true);
            return;
        }

        Object[] rowData = clientTable.getSelectedRowData();
        JTextField[] fields = showClientDialog("تحديث بيانات العميل",
                rowData[1].toString(),
                rowData[2].toString());

        if (fields == null) {
            return;
        }

        String name = fields[0].getText().trim();
        String phone = fields[1].getText().trim();

        if (validateInput(name, phone)) {
            // Create client with userId included in the constructor
            Client updatedClient = new Client(
                    (int) rowData[0], // id
                    currentUser.getId(), // userId
                    name, // name
                    phone, // phone
                    null, // created_at
                    null // updated_at
            );

            clientDao.save(updatedClient); // Updated to not pass userId separately
            refreshTable();
            showMessage("تم تحديث بيانات العميل بنجاح!", false);
        }
    }

    private void deleteClient() {
        int selectedRow = clientTable.getSelectedRow();
        if (selectedRow == -1) {
            showMessage("يرجى تحديد عميل للحذف!", true);
            return;
        }

        if (JOptionPane.showConfirmDialog(this, "هل تريد حذف هذا العميل؟", "تأكيد الحذف",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            // Pass the currentUser's ID to the delete method
            clientDao.delete((int) clientTable.getSelectedRowData()[0], currentUser.getId());
            refreshTable();
            showMessage("تم حذف العميل بنجاح!", false);
        }
    }

    private JTextField[] showClientDialog(String title, String defaultName, String defaultPhone) {
        JTextField nameField = createStyledTextField(defaultName);
        JTextField phoneField = createStyledTextField(defaultPhone);

        JLabel nameLabel = createStyledDialogLabel("أدخل اسم العميل:");
        JLabel phoneLabel = createStyledDialogLabel("أدخل رقم التليفون:");

        Object[] message = {nameLabel, nameField, phoneLabel, phoneField};

        return JOptionPane.showConfirmDialog(this, message, title,
                JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.OK_OPTION
                        ? new JTextField[]{nameField, phoneField} : null;
    }

    private JTextField createStyledTextField(String defaultValue) {
        JTextField field = new JTextField(defaultValue);
        field.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        field.setPreferredSize(TEXT_FIELD_SIZE);
        return field;
    }

    private JLabel createStyledDialogLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.BOLD, 18));
        return label;
    }

    private boolean validateInput(String name, String phone) {
        if (name.isEmpty()) {
            showMessage("يرجى إدخال اسم العميل!", true);
            return false;
        }
        if (!phone.matches("\\d{8,12}")) {
            showMessage("رقم الهاتف يجب أن يحتوي على أرقام فقط (8-12 رقم)!", true);
            return false;
        }
        return true;
    }

    private void handleLogout() {
        if (JOptionPane.showConfirmDialog(this, "هل أنت متأكد من تسجيل الخروج؟",
                "تأكيد تسجيل الخروج", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            new Login().setVisible(true);
            dispose();
        }
    }

    private void addTableDoubleClickListener() {
        clientTable.getTable().addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) { // Double-click detected
                    int selectedRow = clientTable.getSelectedRow();
                    if (selectedRow != -1) {
                        try {
                            Object[] rowData = clientTable.getSelectedRowData();
                            int clientId = (int) rowData[0];
                            String clientName = String.valueOf(rowData[1]);
                            String clientPhone = String.valueOf(rowData[2]);

                            System.out.println("Opening invoice header for client: " + clientId);

                            // Open InvoiceHeaderFrame
                            InvoiceHeaderFrame invoiceHeaderFrame = new InvoiceHeaderFrame(clientId, clientName, clientPhone, currentUser.getId());

                            // Disable ClientFrame while InvoiceHeaderFrame is open
                            setEnabled(false);

                            // Re-enable ClientFrame when InvoiceHeaderFrame is closed
                            invoiceHeaderFrame.addWindowListener(new java.awt.event.WindowAdapter() {
                                @Override
                                public void windowClosed(java.awt.event.WindowEvent e) {
                                    setEnabled(true);
                                    requestFocus(); // Bring focus back to ClientFrame
                                }
                            });

                            invoiceHeaderFrame.setLocationRelativeTo(ClientFrame.this);
                            invoiceHeaderFrame.setVisible(true);

                        } catch (Exception e) {
                            showMessage("حدث خطأ أثناء فتح بيانات الفاتورة", true);
                            e.printStackTrace();
                        }
                    }
                }
            }
        });
    }

    private void refreshTable() {
        loadClientData();
        clientTable.clearSearchField();
        clientTable.revalidate();
        clientTable.repaint();
    }

}
