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
        rootPanel.setBackground(UITheme.PRIMARY_DARK);

        // left banner panel
        JPanel banner = new JPanel(new GridBagLayout());
        banner.setBackground(UITheme.PRIMARY_DARK);
        banner.setPreferredSize(new Dimension(260, 0));
        banner.setBorder(new EmptyBorder(40, 30, 40, 30));

        JLabel logo = new JLabel("💊");
        logo.setFont(new Font("Segoe UI Emoji", Font.PLAIN, 60));
        logo.setForeground(Color.WHITE);
        logo.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel appName = new JLabel("<html><center>HealthFirst<br>PIMS</center></html>");
        appName.setFont(new Font("Segoe UI", Font.BOLD, 26));
        appName.setForeground(Color.WHITE);
        appName.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel tagline = new JLabel("<html><center>Pharmacy Inventory<br>Management System</center></html>");
        tagline.setFont(new Font("Segoe UI", Font.ITALIC, 12));
        tagline.setForeground(UITheme.SIDEBAR_TEXT);
        tagline.setHorizontalAlignment(SwingConstants.CENTER);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0; gbc.gridy = 0; gbc.insets = new Insets(0, 0, 16, 0);
        banner.add(logo, gbc);
        gbc.gridy = 1; gbc.insets = new Insets(0, 0, 8, 0);
        banner.add(appName, gbc);
        gbc.gridy = 2;
        banner.add(tagline, gbc);

        // login cardPanel 
        JPanel cardPanel = new JPanel();
        cardPanel.setLayout(new BoxLayout(cardPanel, BoxLayout.Y_AXIS));
        cardPanel.setBackground(UITheme.CARD_BG);
        cardPanel.setBorder(new EmptyBorder(50, 50, 50, 50));
        cardPanel.setPreferredSize(new Dimension(400, 480));

        JLabel welcome = new JLabel("Welcome Back");
        welcome.setFont(new Font("Segoe UI", Font.BOLD, 24));
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
        cardPanel.add(Box.createVerticalStrut(30)); //Adds a fixed vertical gap of 30 pixels between components
        cardPanel.add(UITheme.formLabel("Username"));
        cardPanel.add(Box.createVerticalStrut(5));
        cardPanel.add(usernameField);
        cardPanel.add(Box.createVerticalStrut(18));
        cardPanel.add(UITheme.formLabel("Password"));
        cardPanel.add(Box.createVerticalStrut(5));
        cardPanel.add(passwordField);
        cardPanel.add(Box.createVerticalStrut(20));
        cardPanel.add(messageLabel);
        cardPanel.add(Box.createVerticalStrut(10));
        cardPanel.add(loginBtn);
        cardPanel.add(Box.createVerticalStrut(24));

        JLabel hint = new JLabel("Default — Admin: admin / admin123   |   Cashier: cashier / cash123");
        hint.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        hint.setForeground(UITheme.TEXT_MUTED);
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        cardPanel.add(hint);

        rootPanel.add(banner, BorderLayout.WEST);
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
