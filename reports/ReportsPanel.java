package reports;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

import database_connection.db_con;
import utilities.UITheme;

public class ReportsPanel extends JPanel {

    public ReportsPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG);
        buildUI();
    }

    private void buildUI() {
        JLabel heading = UITheme.headingLabel("📊  Reports");
        heading.setBorder(new EmptyBorder(16, 20, 8, 20));
        heading.setBackground(UITheme.BG);
        heading.setOpaque(true);
        add(heading, BorderLayout.NORTH);

        JTabbedPane tabs = new JTabbedPane();
        tabs.setFont(UITheme.FONT_BOLD);
        tabs.addTab("Sales Report", buildSalesReport());
        tabs.addTab("Item-Wise Sales", buildItemWiseReport());
        tabs.addTab("Low Stock Alert", buildLowStockReport());
        tabs.addTab("Expiry Report", buildExpiryReport());

        add(tabs, BorderLayout.CENTER);
    }

    // 1. Sales Report
    private JPanel buildSalesReport() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UITheme.BG);
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        // Date filter
        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filter.setOpaque(false);
        JTextField fromField = UITheme.styledField(12);
        fromField.putClientProperty("JTextField.placeholderText", "From YYYY-MM-DD");
        JTextField toField   = UITheme.styledField(12);
        toField.putClientProperty("JTextField.placeholderText", "To YYYY-MM-DD");
        JButton runBtn = UITheme.primaryButton("Generate Report");
        JButton allBtn = UITheme.accentButton("Show All");

        filter.add(UITheme.formLabel("From:")); filter.add(fromField);
        filter.add(UITheme.formLabel("To:"));   filter.add(toField);
        filter.add(runBtn); filter.add(allBtn);

        String[] cols = {"Sale ID", "Date & Time", "Processed By", "Items", "Total (R)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        UITheme.styleTable(table);

        JLabel totalLabel = new JLabel(" ");
        totalLabel.setFont(UITheme.FONT_BOLD);
        totalLabel.setForeground(UITheme.PRIMARY);
        totalLabel.setBorder(new EmptyBorder(6, 0, 0, 0));

        // Show all sales, or fetch only the date range selected by the user
        Runnable loadAll = () -> loadSalesData(model, null, null, totalLabel);
        allBtn.addActionListener(e -> loadAll.run());
        runBtn.addActionListener(e ->
            loadSalesData(model, fromField.getText().trim(), toField.getText().trim(), totalLabel));

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UITheme.CARD_BG);

        panel.add(filter, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(totalLabel, BorderLayout.SOUTH);

        loadAll.run();
        return panel;
    }

    private void loadSalesData(DefaultTableModel model, String from, String to, JLabel totalLabel) {
        model.setRowCount(0);
        try {
            StringBuilder sql = new StringBuilder("""
                SELECT s.sale_id, s.sale_date, u.full_name,
                       (SELECT COUNT(*) FROM sale_items si WHERE si.sale_id=s.sale_id) AS items,
                       s.total_amount
                FROM sales s JOIN users u ON s.user_id=u.user_id
                """);
            boolean hasFrom = from != null && !from.isEmpty();
            boolean hasTo   = to   != null && !to.isEmpty();
            if (hasFrom || hasTo) sql.append(" WHERE ");
            if (hasFrom) sql.append("DATE(s.sale_date) >= '").append(from).append("' ");
            if (hasFrom && hasTo) sql.append("AND ");
            if (hasTo)   sql.append("DATE(s.sale_date) <= '").append(to).append("' ");
            sql.append("ORDER BY s.sale_date DESC");

            ResultSet rs = db_con.getConnection()
                .createStatement().executeQuery(sql.toString());
            double total = 0;
            int count = 0;
            while (rs.next()) {
                double amt = rs.getDouble("total_amount");
                total += amt;
                count++;
                model.addRow(new Object[]{
                    rs.getInt("sale_id"),
                    rs.getTimestamp("sale_date"),
                    rs.getString("full_name"),
                    rs.getInt("items"),
                    String.format("R %.2f", amt)
                });
            }
            totalLabel.setText(String.format("  Total Sales: %d   |   Grand Total: R %.2f", count, total));
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }

    // 2. Item-Wise Sales

    private JPanel buildItemWiseReport() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UITheme.BG);
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel filter = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        filter.setOpaque(false);
        JButton runBtn = UITheme.primaryButton("Load Report");
        filter.add(runBtn);

        String[] cols = {"Medicine", "Type", "Units Sold", "Revenue (R)"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        UITheme.styleTable(table);

        JLabel totalLabel = new JLabel(" ");
        totalLabel.setFont(UITheme.FONT_BOLD);
        totalLabel.setForeground(UITheme.PRIMARY);
        totalLabel.setBorder(new EmptyBorder(6, 0, 0, 0));

        Runnable run = () -> {
            model.setRowCount(0);
            try {
                ResultSet rs = db_con.getConnection().createStatement().executeQuery("""
                    SELECT m.name, m.medicine_type,
                           SUM(si.quantity_sold) AS units,
                           SUM(si.quantity_sold * si.price_at_sale) AS revenue
                    FROM sale_items si
                    JOIN medicines m ON si.medicine_id = m.medicine_id
                    GROUP BY m.medicine_id, m.name, m.medicine_type
                    ORDER BY revenue DESC
                    """);
                double grand = 0;
                while (rs.next()) {
                    double rev = rs.getDouble("revenue");
                    grand += rev;
                    model.addRow(new Object[]{
                        rs.getString("name"), rs.getString("medicine_type"),
                        rs.getInt("units"), String.format("R %.2f", rev)
                    });
                }
                totalLabel.setText(String.format("  Grand Revenue: R %.2f", grand));
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        };
        runBtn.addActionListener(e -> run.run());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UITheme.CARD_BG);

        panel.add(filter, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        panel.add(totalLabel, BorderLayout.SOUTH);
        run.run();
        return panel;
    }

    // 3. Low Stock Alert

    private JPanel buildLowStockReport() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UITheme.BG);
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        header.setOpaque(false);
        JLabel warn = new JLabel("⚠  Medicines at or below reorder level:");
        warn.setFont(UITheme.FONT_BOLD);
        warn.setForeground(UITheme.WARNING);
        JButton refreshBtn = UITheme.accentButton("↺ Refresh");
        header.add(warn); header.add(refreshBtn);

        String[] cols = {"ID", "Medicine", "Type", "In Stock", "Reorder Level", "Deficit", "Supplier"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        UITheme.styleTable(table);

        // Reload the low-stock report with medicines that need replenishment
        Runnable run = () -> {
            model.setRowCount(0);
            try {
                ResultSet rs = db_con.getConnection().createStatement().executeQuery("""
                    SELECT m.medicine_id, m.name, m.medicine_type,
                           m.quantity_in_stock, m.reorder_level,
                           (m.reorder_level - m.quantity_in_stock) AS deficit,
                           s.name AS supplier
                    FROM medicines m
                    LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id
                    WHERE m.quantity_in_stock <= m.reorder_level
                    ORDER BY deficit DESC
                    """);
                while (rs.next()) {
                    model.addRow(new Object[]{
                        rs.getInt("medicine_id"), rs.getString("name"),
                        rs.getString("medicine_type"), rs.getInt("quantity_in_stock"),
                        rs.getInt("reorder_level"), rs.getInt("deficit"),
                        rs.getString("supplier")
                    });
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        };
        refreshBtn.addActionListener(e -> run.run());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UITheme.CARD_BG);

        panel.add(header, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        run.run();
        return panel;
    }

    // ── 4. Expiry Report ─────────────────────────────────────

    private JPanel buildExpiryReport() {
        JPanel panel = new JPanel(new BorderLayout(0, 8));
        panel.setBackground(UITheme.BG);
        panel.setBorder(new EmptyBorder(12, 16, 12, 16));

        JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 4));
        header.setOpaque(false);
        JLabel info = new JLabel("📅  Medicines expiring within the next 30 days:");
        info.setFont(UITheme.FONT_BOLD);
        info.setForeground(UITheme.DANGER);
        JButton refreshBtn = UITheme.accentButton("↺ Refresh");
        header.add(info); header.add(refreshBtn);

        String[] cols = {"ID", "Medicine", "Type", "In Stock", "Expiry Date", "Days Left", "Supplier"};
        DefaultTableModel model = new DefaultTableModel(cols, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        JTable table = new JTable(model);
        UITheme.styleTable(table);

        // Reload the expiry report for medicines expiring soon
        Runnable run = () -> {
            model.setRowCount(0);
            try {
                ResultSet rs = db_con.getConnection().createStatement().executeQuery("""
                    SELECT m.medicine_id, m.name, m.medicine_type,
                           m.quantity_in_stock, m.expiry_date,
                           DATEDIFF(m.expiry_date, CURDATE()) AS days_left,
                           s.name AS supplier
                    FROM medicines m
                    LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id
                    WHERE m.expiry_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 30 DAY)
                    ORDER BY m.expiry_date ASC
                    """);
                while (rs.next()) {
                    int daysLeft = rs.getInt("days_left");
                    model.addRow(new Object[]{
                        rs.getInt("medicine_id"), rs.getString("name"),
                        rs.getString("medicine_type"), rs.getInt("quantity_in_stock"),
                        rs.getDate("expiry_date"),
                        daysLeft <= 7 ? "⚠ " + daysLeft + " days" : daysLeft + " days",
                        rs.getString("supplier")
                    });
                }
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
            }
        };
        refreshBtn.addActionListener(e -> run.run());

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UITheme.CARD_BG);

        panel.add(header, BorderLayout.NORTH);
        panel.add(scroll, BorderLayout.CENTER);
        run.run();
        return panel;
    }
}

