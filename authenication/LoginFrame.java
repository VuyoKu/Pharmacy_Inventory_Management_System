package authenication;
// imports from other packages/folders
import admin.AdminDashboard;
import cashier.CashierDashboard;
import database_connection.db_con;
import models.User;
import utilities.SessionManager;
import utilities.UITheme;


// imports of java libraries
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;


public class LoginFrame extends JFrame{

    // ui components
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JLabel messageLabel;
    private int failedAttempts = 0;

    // constructor for login frame
    public LoginFrame() {
        UITheme.applyGlobalLook(); // handles the themes for the panels
        setTitle("HealthFirst PIMS – Login");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        buildUserInterface();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void buildUserInterface(){
         // rootPanel panel
        JPanel rootPanel = new JPanel(new BorderLayout());
        rootPanel.setBackground(UITheme.BG);

        // top banner panel (was on the left side before, now on top)
        JPanel banner = new JPanel();
        banner.setLayout(new BoxLayout(banner, BoxLayout.Y_AXIS));
        banner.setBackground(UITheme.PRIMARY);
        banner.setBorder(new EmptyBorder(24, 30, 24, 30));

        JLabel logo = new JLabel("❤️");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 46));
        logo.setForeground(Color.WHITE);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel appName = new JLabel("HealthFirst PIMS");
        appName.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 24));
        appName.setForeground(Color.WHITE);
        appName.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel tagline = new JLabel("Pharmacy Inventory Management System");
        tagline.setFont(new Font(UITheme.FONT_FAMILY, Font.ITALIC, 12));
        tagline.setForeground(UITheme.SIDEBAR_TEXT);
        tagline.setAlignmentX(Component.CENTER_ALIGNMENT);

        banner.add(logo);
        banner.add(Box.createVerticalStrut(10));
        banner.add(appName);
        banner.add(Box.createVerticalStrut(4));
        banner.add(tagline);

        // login cardPanel 
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(UITheme.CARD_BG);
        cardPanel.setBorder(new EmptyBorder(40, 60, 40, 60));
        cardPanel.setPreferredSize(new Dimension(420, 400));

        JLabel welcome = new JLabel("Welcome Back");
        welcome.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 24));
        welcome.setForeground(UITheme.PRIMARY);
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel signInLabel = new JLabel("Sign in to your account");
        signInLabel.setFont(UITheme.FONT_LABEL);
        signInLabel.setForeground(UITheme.TEXT_MUTED);
        signInLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        messageLabel = new JLabel(" ");
        messageLabel.setFont(UITheme.FONT_SMALL);
        messageLabel.setForeground(UITheme.DANGER);
        messageLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        usernameField = UITheme.styledField(20);
        passwordField = UITheme.styledPasswordField(20);
        usernameField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));
        passwordField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 38));

        JButton loginBtn = UITheme.primaryButton("  Sign In  ");
        loginBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        loginBtn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));

        loginBtn.addActionListener(e -> attemptLogin());
        usernameField.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin());

        cardPanel.add(welcome);
        cardPanel.add(Box.createVerticalStrut(6));
        cardPanel.add(signInLabel);
        cardPanel.add(Box.createVerticalStrut(24)); //Adds a fixed vertical gap between components
        cardPanel.add(UITheme.formLabel("Username"));
        cardPanel.add(Box.createVerticalStrut(5));
        cardPanel.add(usernameField);
        cardPanel.add(Box.createVerticalStrut(18));
        cardPanel.add(UITheme.formLabel("Password"));
        cardPanel.add(Box.createVerticalStrut(5));
        cardPanel.add(passwordField);
        cardPanel.add(Box.createVerticalStrut(16));
        cardPanel.add(messageLabel);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(loginBtn);
        cardPanel.add(Box.createVerticalStrut(20));

        JLabel hint = new JLabel("Default — Admin: admin / admin123   |  Cashier: cashier / cash123");
        hint.setFont(new Font(UITheme.FONT_FAMILY, Font.ITALIC, 10));
        hint.setForeground(UITheme.TEXT_MUTED);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(hint);

        rootPanel.add(banner, BorderLayout.NORTH);
        rootPanel.add(cardPanel, BorderLayout.CENTER);
        setContentPane(rootPanel);
    }


    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword()).trim();

        if (username.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please enter username and password.");
            return;
        }

        try {
            Connection conn = db_con.getConnection();
            String sql = "SELECT * FROM users WHERE username = ? AND password = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                User user = new User(
                    rs.getInt("user_id"),
                    rs.getString("username"),
                    rs.getString("password"),
                    rs.getString("role"),
                    rs.getString("full_name")
                );
                SessionManager.setCurrentUser(user);
                dispose();
                if ("Admin".equals(user.getRole())) {
                    new AdminDashboard();
                } else {
                    new CashierDashboard();
                }
            } else {
                failedAttempts++;
                messageLabel.setText("✘  Invalid credentials. Attempt " + failedAttempts + "/3");
                passwordField.setText("");
                if (failedAttempts >= 3) {
                    messageLabel.setText("✘  Too many failed attempts. Please contact admin.");
                    usernameField.setEnabled(false);
                    passwordField.setEnabled(false);
                }
            }
        } catch (SQLException ex) {
            messageLabel.setText("✘  Database error: " + ex.getMessage());
            ex.printStackTrace();
        }
        
    }

    public static void main(String[] args) {
        // starts new LoginFrame frame on Swing's Event Dispatch Thread.
        // EDT is used to creating/updating Swing user interfaces
        SwingUtilities.invokeLater(new Runnable() {
        @Override
        public void run() {
            new LoginFrame();
        }
    });
    }
}
