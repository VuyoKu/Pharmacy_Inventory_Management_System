package cashier;

import database_connection.db_con;
import utilities.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class StockCheckPanel extends JPanel {

    private JTextField searchField;
    private JTable table;
    private DefaultTableModel model;

    private static final String[] COLS = {
        "Medicine", "Type", "Price (R)", "In Stock", "Expiry Date", "Status"
    };

    public StockCheckPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG);
        buildUI();
        loadAll();
    }

    private void buildUI() {
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setBackground(UITheme.BG);
        toolbar.setBorder(new EmptyBorder(16, 20, 12, 20));
        toolbar.add(UITheme.headingLabel("🔍  Stock Check (Read-Only)"), BorderLayout.WEST);

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        searchPanel.setOpaque(false);
        searchField = UITheme.styledField(22);
        searchField.putClientProperty("JTextField.placeholderText", "Search by name or type…");
        JButton searchBtn = UITheme.primaryButton("Search");
        JButton showAllBtn = UITheme.accentButton("Show All");

        searchBtn.addActionListener(e -> search());
        showAllBtn.addActionListener(e -> { searchField.setText(""); loadAll(); });
        searchField.addActionListener(e -> search());

        searchPanel.add(searchField); searchPanel.add(searchBtn); searchPanel.add(showAllBtn);
        toolbar.add(searchPanel, BorderLayout.EAST);

        model = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        UITheme.styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UITheme.CARD_BG);

        JLabel note = new JLabel("  ℹ  Cashiers can view stock but cannot modify inventory.");
        note.setFont(UITheme.FONT_SMALL);
        note.setForeground(UITheme.TEXT_MUTED);
        note.setBorder(new EmptyBorder(6, 12, 6, 12));

        add(toolbar, BorderLayout.NORTH);
        add(scroll,  BorderLayout.CENTER);
        add(note,    BorderLayout.SOUTH);
    }

    // Load all medicines from the database and populate the table with their details, including name, type, price, stock quantity, expiry date, and status.
    private void loadAll() {
        populateTable("SELECT name, medicine_type, price, quantity_in_stock, expiry_date " +
            "FROM medicines ORDER BY name", null);
    }

    // Search for medicines based on the user's input in the search field, filtering by name or type, and populate the table with the matching results. If the search field is empty, load all medicines instead.
    private void search() {
        String q = searchField.getText().trim();
        if (q.isEmpty()) { loadAll(); return; }
        populateTable(
            "SELECT name, medicine_type, price, quantity_in_stock, expiry_date " +
            "FROM medicines WHERE name LIKE ? OR medicine_type LIKE ? ORDER BY name",
            "%" + q + "%");
    }

    // Populate the table with medicine data based on the provided SQL query and optional parameter for filtering. The method retrieves data from the database, calculates stock status, and updates the table model accordingly.
    private void populateTable(String sql, String param) {
        model.setRowCount(0);
        try {
            PreparedStatement ps = db_con.getConnection().prepareStatement(sql);
            if (param != null) { ps.setString(1, param); ps.setString(2, param); }
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int qty = rs.getInt("quantity_in_stock");
                String status = qty == 0 ? "Out of Stock"
                              : qty <= 10 ? "Low Stock"
                              : "Available";
                model.addRow(new Object[]{
                    rs.getString("name"),
                    rs.getString("medicine_type"),
                    "R " + rs.getBigDecimal("price"),
                    qty,
                    rs.getDate("expiry_date"),
                    status
                });
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Error: " + ex.getMessage());
        }
    }
}
