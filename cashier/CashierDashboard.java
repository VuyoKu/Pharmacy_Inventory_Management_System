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

    // the panel in the middle that shows whichever screen is picked
    private JPanel contentArea;
    private CardLayout contentLayout;

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

        // Header at the top
        JPanel header = buildHeader();
        root.add(header, BorderLayout.NORTH);

        // Sidebar of buttons on the left (instead of a JTabbedPane)
        JPanel sidebar = buildSidebar();
        root.add(sidebar, BorderLayout.WEST);

        // Content area in the middle. CardLayout lets us "flip" between
        // panels without removing/adding components manually.
        contentLayout = new CardLayout();
        contentArea = new JPanel(contentLayout);
        contentArea.setBackground(UITheme.BG);

        contentArea.add(new POSPanel(),        "POS");
        contentArea.add(new StockCheckPanel(),  "STOCK");

        root.add(contentArea, BorderLayout.CENTER);
        setContentPane(root);
    }

    private JPanel buildHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(UITheme.PRIMARY_DARK);
        header.setBorder(new EmptyBorder(12, 20, 12, 20));
        header.setPreferredSize(new Dimension(0, 65));

        JLabel title = new JLabel("💊  HealthFirst – Cashier POS");
        title.setFont(new Font(UITheme.FONT_FAMILY, Font.BOLD, 18));
        title.setForeground(Color.WHITE);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT, 16, 0));
        right.setOpaque(false);

        String time = new SimpleDateFormat("dd MMM yyyy  HH:mm").format(new Date());
        JLabel timeLabel = new JLabel("🕐  " + time);
        timeLabel.setFont(UITheme.FONT_SMALL);
        timeLabel.setForeground(UITheme.SIDEBAR_TEXT);

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

    // builds a simple vertical menu of buttons that switch the content area
    private JPanel buildSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(UITheme.SIDEBAR_BG);
        sidebar.setPreferredSize(new Dimension(190, 0));
        sidebar.setBorder(new EmptyBorder(20, 12, 20, 12));

        JButton posBtn   = sidebarButton("🛒  Point of Sale");
        JButton stockBtn = sidebarButton("🔍  Stock Check");

        posBtn.addActionListener(e -> contentLayout.show(contentArea, "POS"));
        stockBtn.addActionListener(e -> contentLayout.show(contentArea, "STOCK"));

        sidebar.add(posBtn);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(stockBtn);
        sidebar.add(Box.createVerticalGlue());

        return sidebar;
    }

    // small helper so every sidebar button looks the same
    private JButton sidebarButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UITheme.FONT_BOLD);
        btn.setForeground(Color.WHITE);
        btn.setBackground(UITheme.PRIMARY);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setHorizontalAlignment(SwingConstants.LEFT);
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(Integer.MAX_VALUE, 42));
        btn.setBorder(new EmptyBorder(10, 14, 10, 14));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
