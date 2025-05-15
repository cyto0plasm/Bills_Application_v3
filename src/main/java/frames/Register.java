package frames;

import Data_Access_Object.user.User;
import Data_Access_Object.user.UserDao;
import Data_Access_Object.user.UserDaoImp;
import com.formdev.flatlaf.FlatClientProperties;
import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.ui.FlatBorder;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.util.List;

public class Register extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton registerButton;
    private JLabel loginLabel;
    UserDao userDao = new UserDaoImp();

    public Register() {
        try {
            // Try to setup FlatLaf, fall back to system look and feel if it fails
            if (!FlatLightLaf.setup()) {
                try {
                    UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        } catch (Exception e) {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        setTitle("تسجيل حساب جديد");
        setSize(450, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        // Create main panel with padding
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new GridBagLayout());
        mainPanel.setBackground(new Color(250, 251, 252));
        mainPanel.setBorder(new EmptyBorder(30, 40, 30, 40));
        mainPanel.putClientProperty("JPanel.border", new FlatBorder());

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(12, 0, 12, 0);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;

        // Logo/Brand (you can replace this with an actual logo)
        JLabel logoLabel = new JLabel("\uD83D\uDCDD", SwingConstants.CENTER); // Registration emoji
        logoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(logoLabel, gbc);

        // Title
        JLabel titleLabel = new JLabel("إنشاء حساب جديد", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(30, 41, 59));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(titleLabel, gbc);

        // Username field
        JLabel usernameLabel = new JLabel("اسم المستخدم");
        usernameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 15));
        usernameLabel.setForeground(new Color(71, 85, 105));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 8, 0);
        mainPanel.add(usernameLabel, gbc);

        usernameField = new JTextField();
        usernameField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        usernameField.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc: 8;"
                + "focusWidth: 1;"
                + "borderWidth: 1;"
                + "borderColor: #CBD5E1;"
                + "focusedBorderColor: #3B82F6;"
        );
        usernameField.setPreferredSize(new Dimension(200, 40));
        usernameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "أدخل اسم المستخدم");
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridy = 3;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(usernameField, gbc);

        // Password field
        JLabel passwordLabel = new JLabel("كلمة المرور");
        passwordLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordLabel.setForeground(new Color(71, 85, 105));
        gbc.gridy = 4;
        gbc.insets = new Insets(0, 0, 8, 0);
        mainPanel.add(passwordLabel, gbc);

        passwordField = new JPasswordField();
        passwordField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        passwordField.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc: 8;"
                + "focusWidth: 2;"
                + "borderWidth: 1;"
                + "borderColor: #CBD5E1;"
                + "focusedBorderColor: #3B82F6;");
        passwordField.setPreferredSize(new Dimension(200, 40));
        passwordField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "أدخل كلمة المرور");
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(passwordField, gbc);

        // Register button
        registerButton = new JButton("إنشاء حساب");
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 16));
        registerButton.setForeground(Color.WHITE);
        registerButton.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc: 8;"
                + "background: #3B82F6;"
                + "hoverBackground: #2563EB;"
                + "pressedBackground: #1D4ED8;"
                + "focusWidth: 0;"
        );
        registerButton.setPreferredSize(new Dimension(200, 44));
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(registerButton, gbc);

        // Login link
        loginLabel = new JLabel("لديك حساب بالفعل؟ تسجيل الدخول", SwingConstants.CENTER);
        loginLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        loginLabel.setForeground(new Color(59, 130, 246));
        loginLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 0, 0);
        mainPanel.add(loginLabel, gbc);

        // Add main panel to frame
        add(mainPanel);

        // Add action listeners
        registerButton.addActionListener(e -> registerUser());
        loginLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // Open login frame
                new Login().setVisible(true);
                // Close current register frame
                dispose();
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                loginLabel.setForeground(new Color(37, 99, 235));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                loginLabel.setForeground(new Color(59, 130, 246));
            }
        });
    }

    private void registerUser() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            showErrorDialog("يجب ملء جميع الحقول!");
            return;
        }

        // Check if username already exists
        if (isUsernameTaken(username)) {
            showErrorDialog("اسم المستخدم موجود بالفعل!");
            return;
        }

        // Create new user
        User newUser = new User(0, username, password, null, null);

        try {
            userDao.save(newUser);

            JOptionPane optionPane = new JOptionPane(
                    "تم إنشاء الحساب بنجاح! يمكنك الآن تسجيل الدخول",
                    JOptionPane.INFORMATION_MESSAGE
            );
            JDialog dialog = optionPane.createDialog(this, "نجاح");
            dialog.setLocationRelativeTo(this);
            dialog.setVisible(true);

            // Return to login
            new Login().setVisible(true);
            dispose();

        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("حدث خطأ أثناء إنشاء الحساب!");
        }
    }

    private boolean isUsernameTaken(String username) {
        try {
            List<User> users = userDao.findAll();
            if (users != null) {
                return users.stream()
                        .anyMatch(user -> user.getName().equals(username));
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("حدث خطأ في الاتصال بقاعدة البيانات");
        }
        return false;
    }

    private void showErrorDialog(String message) {
        JOptionPane optionPane = new JOptionPane(
                message,
                JOptionPane.ERROR_MESSAGE
        );
        JDialog dialog = optionPane.createDialog(this, "خطأ");
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
    }
}
