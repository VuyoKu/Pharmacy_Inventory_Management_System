package admin;

// imports from other packages/folders
import authenication.LoginFrame;
import utilities.SessionManager;
import utilities.UITheme;
import reports.ReportsPanel;

// imports of java libraries
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;

public class AdminDashboard extends JFrame {

    public AdminDashboard() {
        setTitle("HealthFirst PIMS – Admin Dashboard");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setExtendedState(JFrame.MAXIMIZED_BOTH);
        buildUI();
        setVisible(true);
    }

    private void buildUI() {
        JPanel root = new JPanel(new BorderLayout(0, 0));
        root.setBackground(UITheme.BG);

        // Top Header 
        JPanel header = buildHeader();
        root.add(header, BorderLayout.NORTH);

        // Tabbed Content
        JTabbedPane tabs = new JTabbedPane(JTabbedPane.LEFT);
        tabs.setFont(UITheme.FONT_BOLD);
        tabs.setBackground(UITheme.SIDEBAR_BG);
        tabs.setForeground(UITheme.SIDEBAR_TEXT);

        tabs.addTab("Dashboard", buildHomePanel());

        tabs.addTab("Medicines", new MedicinePanel());
        tabs.addTab("Suppliers", new SupplierPanel());
        tabs.addTab("Users", new UserPanel());
        tabs.addTab("Reports", new ReportsPanel());

        root.add(tabs, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY_DARK);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));
        header.setPreferredSize(new Dimension(0, 65));

        JLabel title = new JLabel("HealthFirst Pharmacy Inventory Management");
        title.setFont(new Font("Segoe UI", Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setOpaque(false);

        String time = new SimpleDateFormat("dd MMM yyyy  HH:mm").format(new Date());
        JLabel timeLabel = new JLabel("🕐  " + time);
        timeLabel.setFont(UITheme.FONT_SMALL);
        timeLabel.setForeground(UITheme.SIDEBAR_TEXT);

        JLabel userLabel = new JLabel("👤  " + SessionManager.getCurrentUser().getFullName() + "  [Admin]");
        userLabel.setFont(UITheme.FONT_BOLD);
        userLabel.setForeground(Color.WHITE);

        JButton logoutBtn = UITheme.dangerButton("Logout");
        logoutBtn.setBorder(new EmptyBorder(6, 14, 6, 14));
        logoutBtn.addActionListener(e -> logout());

        right.add(timeLabel);
        right.add(userLabel);
        right.add(logoutBtn);

        header.add(title, BorderLayout.WEST);
        header.add(right, BorderLayout.EAST);
        return header;
    }

    private JPanel buildHomePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(UITheme.BG);
        panel.setBorder(new EmptyBorder(30, 30, 30, 30));

        JLabel heading = UITheme.titleLabel("Admin Dashboard");
        heading.setBorder(new EmptyBorder(0, 0, 20, 0));
        panel.add(heading, BorderLayout.NORTH);

        // Stats row
        JPanel statsRow = new JPanel(new GridLayout(1, 4, 16, 0));
        statsRow.setOpaque(false);

        statsRow.add(statCard("Total Medicines", getMedicineCount(), UITheme.PRIMARY));
        statsRow.add(statCard("Total Suppliers", getSupplierCount(), UITheme.ACCENT));
        statsRow.add(statCard("Sales Today", getSalesToday(), UITheme.SUCCESS));
        statsRow.add(statCard("Low Stock Items", getLowStockCount(), UITheme.DANGER));

        JPanel statsWrapper = new JPanel(new BorderLayout());
        statsWrapper.setOpaque(false);
        statsWrapper.setBorder(new EmptyBorder(0, 0, 24, 0));
        statsWrapper.add(statsRow, BorderLayout.CENTER);
        panel.add(statsWrapper, BorderLayout.CENTER);

        JLabel info = new JLabel("<html><b>Quick Tips:</b><br>" +
            "Use the Medicines tab to add, edit, or remove medicines.<br>" +
            "Manage supplier contacts in the Suppliers tab.<br>" +
            "Create and manage cashier accounts in the Users tab.<br>" +
            "View analytical reports in the Reports tab.</html>");
        info.setFont(UITheme.FONT_LABEL);
        info.setForeground(UITheme.TEXT_MUTED);
        info.setBorder(new EmptyBorder(20, 0, 0, 0));
        panel.add(info, BorderLayout.SOUTH);

        return panel;
    }

    private JPanel statCard(String label, String value, Color color) {
        JPanel card = UITheme.card(new BorderLayout(0, 8));
        card.setBorder(new EmptyBorder(20, 24, 20, 24));

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 34));
        valueLabel.setForeground(color);

        JLabel nameLabel = new JLabel(label);
        nameLabel.setFont(UITheme.FONT_LABEL);
        nameLabel.setForeground(UITheme.TEXT_MUTED);

        JPanel stripe = new JPanel();
        stripe.setBackground(color);
        stripe.setPreferredSize(new Dimension(0, 4));

        card.add(stripe, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);
        card.add(nameLabel, BorderLayout.SOUTH);
        return card;
    }

    private String getMedicineCount() {
        try {
            var rs = database_connection.db_con.getConnection()
                .createStatement().executeQuery("SELECT COUNT(*) FROM medicines");
            if (rs.next()) return String.valueOf(rs.getInt(1));
        } catch (Exception ignored) {}
        return "–";
    }

    private String getSupplierCount() {
        try {
            var rs = database_connection.db_con.getConnection()
                .createStatement().executeQuery("SELECT COUNT(*) FROM suppliers");
            if (rs.next()) return String.valueOf(rs.getInt(1));
        } catch (Exception ignored) {}
        return "–";
    }

    private String getSalesToday() {
        try {
            var rs = database_connection.db_con.getConnection()
                .createStatement().executeQuery(
                    "SELECT COUNT(*) FROM sales WHERE DATE(sale_date)=CURDATE()");
            if (rs.next()) return String.valueOf(rs.getInt(1));
        } catch (Exception ignored) {}
        return "–";
    }

    private String getLowStockCount() {
        try {
            var rs = database_connection.db_con.getConnection()
                .createStatement().executeQuery(
                    "SELECT COUNT(*) FROM medicines WHERE quantity_in_stock <= reorder_level");
            if (rs.next()) return String.valueOf(rs.getInt(1));
        } catch (Exception ignored) {}
        return "–";
    }

    private void logout() {
        int r = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to logout?", "Logout", JOptionPane.YES_NO_OPTION);
        if (r == JOptionPane.YES_OPTION) {
            SessionManager.clearSession();
            dispose();
            new LoginFrame();
        }
    }
}
