package cashier;


import authenication.LoginFrame;
import utilities.SessionManager;
import utilities.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class CashierDashboard extends JFrame {

    public CashierDashboard() {
        setTitle("HealthFirst PIMS – Cashier POS");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UITheme.BG);

        // Header
        JPanel header = buildHeader();
        root.add(header, BorderLayout.NORTH);

        // Tabbed pane
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
        tabs.setFont(UITheme.FONT_BOLD);

        tabs.addTab("🛒  Point of Sale", new POSPanel());
        tabs.addTab("🔍  Stock Check",   new StockCheckPanel());

        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(new Color(0, 100, 60));
        header.setBorder(new EmptyBorder(12, 20, 12, 20));
        header.setPreferredSize(new Dimension(0, 65));

        JLabel title = new JLabel("💊  HealthFirst – Cashier POS");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setOpaque(false);

        String time = new SimpleDateFormat("dd MMM yyyy  HH:mm").format(new Date());
        JLabel timeLabel = new JLabel("🕐  " + time);
        timeLabel.setFont(UITheme.FONT_SMALL);
        timeLabel.setForeground(new Color(200, 240, 220));

        JLabel userLabel = new JLabel("👤  " + SessionManager.getCurrentUser().getFullName() + "  [Cashier]");
        userLabel.setFont(UITheme.FONT_BOLD);
        userLabel.setForeground(Color.WHITE);

        JButton logoutBtn = UITheme.dangerButton("Logout");
        logoutBtn.setBorder(new EmptyBorder(6, 14, 6, 14));
        logoutBtn.addActionListener(e -> {
            int r = JOptionPane.showConfirmDialog(this,
                "Logout?", "Confirm", JOptionPane.YES_NO_OPTION);
            if (r == JOptionPane.YES_OPTION) {
                SessionManager.clearSession();
                dispose();
                new LoginFrame();
            }
        });

        right.add(timeLabel); right.add(userLabel); right.add(logoutBtn);
        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }
}
