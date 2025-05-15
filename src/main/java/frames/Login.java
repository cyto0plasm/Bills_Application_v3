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

public class Login extends JFrame {

    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JLabel registerLabel;
    UserDao userDao = new UserDaoImp();

    public Login() {
        this.setExtendedState(JFrame.NORMAL); // Ensure it's not minimized
        this.toFront(); // Bring it to the front
        this.repaint(); // Force repainting

        // Try to setup FlatLaf, fall back to system look and feel if it fails
        try {
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
        setTitle("تسجيل الدخول");
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
        JLabel logoLabel = new JLabel("\uD83D\uDD12", SwingConstants.CENTER); // Lock emoji
        logoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 48));
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(logoLabel, gbc);

        // Title
        JLabel titleLabel = new JLabel("تسجيل الدخول", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 28));
        titleLabel.setForeground(new Color(30, 41, 59));
        gbc.gridy = 1;
        gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(titleLabel, gbc);

        // Username field
        JLabel usernameLabel = new JLabel("اسم المستخدم");
        usernameLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        usernameLabel.setForeground(new Color(71, 85, 105));
        gbc.gridy = 2;
        gbc.insets = new Insets(0, 0, 8, 0);
        mainPanel.add(usernameLabel, gbc);

        usernameField = new JTextField();
        usernameField.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc: 8;"
                + "focusWidth: 1;"
                + "borderWidth: 1;"
                + "borderColor: #CBD5E1;"
                + "focusedBorderColor: #3B82F6;"
        );
        usernameField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "أدخل اسم المستخدم");
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setPreferredSize(new Dimension(200, 40));

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
        passwordField.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc: 8;"
                + "focusWidth: 1;"
                + "borderWidth: 1;"
                + "borderColor: #CBD5E1;"
                + "focusedBorderColor: #3B82F6;"
        );
        passwordField.putClientProperty(FlatClientProperties.PLACEHOLDER_TEXT, "أدخل كلمة المرور");
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setPreferredSize(new Dimension(200, 40));
        gbc.gridy = 5;
        gbc.insets = new Insets(0, 0, 30, 0);
        mainPanel.add(passwordField, gbc);

        // Login button
        loginButton = new JButton("تسجيل الدخول");
        loginButton.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        loginButton.setForeground(Color.WHITE);
        loginButton.setPreferredSize(new Dimension(200, 44));
        loginButton.putClientProperty(FlatClientProperties.STYLE, ""
                + "arc: 8;"
                + "background: #3B82F6;"
                + "hoverBackground: #2563EB;"
                + "pressedBackground: #1D4ED8;"
                + "focusWidth: 0;"
        );
        gbc.gridy = 6;
        gbc.insets = new Insets(0, 0, 20, 0);
        mainPanel.add(loginButton, gbc);
        //app Link
        loginButton.addActionListener(e -> handleLogin());

        // Register link
        registerLabel = new JLabel("ليس لديك حساب؟ إنشاء حساب جديد", SwingConstants.CENTER);
        registerLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        registerLabel.setForeground(new Color(59, 130, 246));
        registerLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 0, 0);
        mainPanel.add(registerLabel, gbc);

        // Add main panel to frame
        add(mainPanel);

        // Add action listeners
        registerLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                // Open register frame
                new Register().setVisible(true);
                // Close current login frame
                dispose();
            }

            @Override
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                registerLabel.setForeground(new Color(37, 99, 235));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent evt) {
                registerLabel.setForeground(new Color(59, 130, 246));
            }
        });
    }

private void handleLogin() {
    String username = usernameField.getText().trim();
    String password = new String(passwordField.getPassword()).trim();
    if (username.isEmpty() || password.isEmpty()) {
        showErrorDialog("يرجى إدخال اسم المستخدم وكلمة المرور");
        return;
    }
    
    User authenticatedUser = authenticateUser(username, password);
    if (authenticatedUser != null) {
        JOptionPane optionPane = new JOptionPane(
                "تم تسجيل الدخول بنجاح!",
                JOptionPane.INFORMATION_MESSAGE
        );
        JDialog dialog = optionPane.createDialog(this, "نجاح");
        dialog.setLocationRelativeTo(this);
        dialog.setVisible(true);
        
        // Open ClientFrame with the authenticated user
        openClientFrame(authenticatedUser);
    } else {
        showErrorDialog("اسم المستخدم أو كلمة المرور غير صحيحة");
    }
}
    private User authenticateUser(String username, String password) {
        try {
            List<User> users = userDao.findAll();
            if (users != null) {
                return users.stream()
                        .filter(user -> user.getName().equals(username)
                        && user.getPassword().equals(password))
                        .findFirst()
                        .orElse(null);
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorDialog("حدث خطأ في الاتصال بقاعدة البيانات");
        }
        return null;
    }

    private void openClientFrame(User user) {
    SwingUtilities.invokeLater(() -> {
        new ClientFrame(user).setVisible(true);
        this.dispose();
    });
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
