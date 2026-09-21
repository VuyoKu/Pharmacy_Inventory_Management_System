package admin;

// imports of java libraries
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

// imports from other packages/folder
import database_connection.db_con;
import models.Medicine;
import utilities.UITheme;

public class MedicinePanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private JTextField searchField;
    private List<Medicine> medicines = new ArrayList<>();

    private static final String[] COLS = {
        "ID", "Name", "Company", "Type", "Price (R)", "In Stock", "Reorder Lvl", "Expiry", "Supplier"
    };

    public MedicinePanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG);
        buildUI();
        loadData();
    }

    private void buildUI() {
        // Toolbar Panel 
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setBackground(UITheme.BG);
        toolbar.setBorder(new EmptyBorder(16, 20, 12, 20));

        JLabel heading = UITheme.headingLabel("Medicine Inventory");
        toolbar.add(heading, BorderLayout.WEST);

        JPanel btnPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnPanel.setOpaque(false);

        searchField = UITheme.styledField(18);
        searchField.putClientProperty("JTextField.placeholderText", "Search medicines…");
        JButton searchBtn = UITheme.primaryButton("Search");
        JButton clearBtn  = UITheme.warningButton("Clear");
        JButton addBtn    = UITheme.successButton("Add Medicine");
        JButton editBtn   = UITheme.primaryButton("Edit");
        JButton delBtn    = UITheme.dangerButton("Delete");
        JButton refreshBtn = UITheme.accentButton("Refresh");

        searchBtn.addActionListener(e -> searchMedicines());
        clearBtn.addActionListener(e -> { searchField.setText(""); loadData(); });
        addBtn.addActionListener(e -> openAddEditDialog(null));
        editBtn.addActionListener(e -> editSelected());
        delBtn.addActionListener(e -> deleteSelected());
        refreshBtn.addActionListener(e -> loadData());

        btnPanel.add(searchField);
        btnPanel.add(searchBtn);
        btnPanel.add(clearBtn);
        btnPanel.add(Box.createHorizontalStrut(8));
        btnPanel.add(addBtn);
        btnPanel.add(editBtn);
        btnPanel.add(delBtn);
        btnPanel.add(refreshBtn);
        toolbar.add(btnPanel, BorderLayout.EAST);

        // Table 
        model = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        UITheme.styleTable(table);
        table.getColumnModel().getColumn(0).setPreferredWidth(40);
        table.getColumnModel().getColumn(1).setPreferredWidth(160);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(70);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UITheme.CARD_BG);

        add(toolbar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    public void loadData() {
        medicines.clear();
        model.setRowCount(0);
        try {
            Connection conn = db_con.getConnection();
            String sql = """
                SELECT m.*, s.name AS supplier_name
                FROM medicines m
                LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id
                ORDER BY m.name
                """;
            ResultSet rs = conn.createStatement().executeQuery(sql);
            while (rs.next()) {
                Medicine med = mapRow(rs);
                medicines.add(med);
                model.addRow(new Object[]{
                    med.getMedicineId(), med.getName(), med.getCompany(),
                    med.getMedicineType(), "R " + med.getPrice(),
                    med.getQuantityInStock(), med.getReorderLevel(),
                    med.getExpiryDate(), med.getSupplierName()
                });
            }
        } catch (SQLException ex) {
            showError("Failed to load medicines: " + ex.getMessage());
        }
    }

    private void searchMedicines() {
        String q = searchField.getText().trim();
        if (q.isEmpty()) { loadData(); return; }
        medicines.clear();
        model.setRowCount(0);
        try {
            Connection conn = db_con.getConnection();
            String sql = """
                SELECT m.*, s.name AS supplier_name
                FROM medicines m
                LEFT JOIN suppliers s ON m.supplier_id = s.supplier_id
                WHERE m.name LIKE ? OR m.company LIKE ? OR m.medicine_type LIKE ?
                ORDER BY m.name
                """;
            PreparedStatement ps = conn.prepareStatement(sql);
            String like = "%" + q + "%";
            ps.setString(1, like); ps.setString(2, like); ps.setString(3, like);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                Medicine med = mapRow(rs);
                medicines.add(med);
                model.addRow(new Object[]{
                    med.getMedicineId(), med.getName(), med.getCompany(),
                    med.getMedicineType(), "R " + med.getPrice(),
                    med.getQuantityInStock(), med.getReorderLevel(),
                    med.getExpiryDate(), med.getSupplierName()
                });
            }
        } catch (SQLException ex) {
            showError("Search failed: " + ex.getMessage());
        }
    }

    private Medicine mapRow(ResultSet rs) throws SQLException {
        Medicine m = new Medicine();
        m.setMedicineId(rs.getInt("medicine_id"));
        m.setName(rs.getString("name"));
        m.setCompany(rs.getString("company"));
        m.setMedicineType(rs.getString("medicine_type"));
        m.setPrice(rs.getBigDecimal("price"));
        m.setQuantityInStock(rs.getInt("quantity_in_stock"));
        m.setReorderLevel(rs.getInt("reorder_level"));
        m.setExpiryDate(rs.getDate("expiry_date"));
        m.setSupplierId(rs.getInt("supplier_id"));
        m.setSupplierName(rs.getString("supplier_name"));
        return m;
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a medicine to edit."); return; }
        openAddEditDialog(medicines.get(row));
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Please select a medicine to delete."); return; }
        Medicine m = medicines.get(row);
        int c = JOptionPane.showConfirmDialog(this,
            "Delete '" + m.getName() + "'?  This cannot be undone.", "Confirm Delete",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) return;
        try {
            PreparedStatement ps = db_con.getConnection()
                .prepareStatement("DELETE FROM medicines WHERE medicine_id=?");
            ps.setInt(1, m.getMedicineId());
            ps.executeUpdate();
            loadData();
            JOptionPane.showMessageDialog(this, "Medicine deleted successfully.");
        } catch (SQLException ex) {
            showError("Delete failed: " + ex.getMessage());
        }
    }

    private void openAddEditDialog(Medicine existing) {
        boolean isEdit = (existing != null);
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            isEdit ? "Edit Medicine" : "Add Medicine", true);
        dlg.setSize(520, 540);
        dlg.setLocationRelativeTo(this);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(UITheme.CARD_BG);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL;
        g.insets = new Insets(5, 5, 5, 5);

        // Fields
        JTextField nameF    = UITheme.styledField(20);
        JTextField companyF = UITheme.styledField(20);
        String[] types = {"Tablet","Capsule","Syrup","Injection","Cream","Ointment","Drops","Other"};
        JComboBox<String> typeCombo = UITheme.styledCombo(types);
        JTextField priceF   = UITheme.styledField(10);
        JTextField qtyF     = UITheme.styledField(10);
        JTextField reorderF = UITheme.styledField(10);
        JTextField expiryF  = UITheme.styledField(12);
        expiryF.putClientProperty("JTextField.placeholderText", "YYYY-MM-DD");
        JComboBox<String> supplierCombo = new JComboBox<>();
        supplierCombo.setFont(UITheme.FONT_LABEL);
        loadSuppliers(supplierCombo);

        if (isEdit) {
            nameF.setText(existing.getName());
            companyF.setText(existing.getCompany());
            typeCombo.setSelectedItem(existing.getMedicineType());
            priceF.setText(existing.getPrice().toPlainString());
            qtyF.setText(String.valueOf(existing.getQuantityInStock()));
            reorderF.setText(String.valueOf(existing.getReorderLevel()));
            expiryF.setText(existing.getExpiryDate().toString());
            // select supplier
            for (int i = 0; i < supplierCombo.getItemCount(); i++) {
                if (supplierCombo.getItemAt(i).startsWith(existing.getSupplierId() + "|"))
                    supplierCombo.setSelectedIndex(i);
            }
        }
/*
        String[][] rows = {
            {"Medicine Name *", null}, {"Company *", null}, {"Type *", null},
            {"Price (R) *", null}, {"Qty In Stock *", null}, {"Reorder Level *", null},
            {"Expiry Date *", null}, {"Supplier", null}
        };
         */
        JComponent[] comps = {nameF, companyF, typeCombo, priceF, qtyF, reorderF, expiryF, supplierCombo};

        String[] labels = {"Medicine Name *","Company *","Type *","Price (R) *",
                           "Qty In Stock *","Reorder Level *","Expiry Date *","Supplier"};
        for (int i = 0; i < labels.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0.3;
            content.add(UITheme.formLabel(labels[i]), g);
            g.gridx = 1; g.weightx = 0.7;
            content.add(comps[i], g);
        }

        JButton saveBtn   = UITheme.successButton(isEdit ? "Update" : "Save");
        JButton cancelBtn = UITheme.dangerButton("Cancel");

        saveBtn.addActionListener(e -> {
            if (nameF.getText().trim().isEmpty() || priceF.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Name and Price are required."); return;
            }
            try {
                String name    = nameF.getText().trim();
                String company = companyF.getText().trim();
                String type    = (String) typeCombo.getSelectedItem();
                BigDecimal price = new BigDecimal(priceF.getText().trim());
                int qty    = Integer.parseInt(qtyF.getText().trim());
                int reorder = Integer.parseInt(reorderF.getText().trim());
                Date expiry = Date.valueOf(expiryF.getText().trim());
                String sup = (String) supplierCombo.getSelectedItem();
                int supId  = sup != null ? Integer.parseInt(sup.split("\\|")[0]) : 0;

                Connection conn = db_con.getConnection();
                if (isEdit) {
                    PreparedStatement ps = conn.prepareStatement(
                        "UPDATE medicines SET name=?,company=?,medicine_type=?,price=?," +
                        "quantity_in_stock=?,reorder_level=?,expiry_date=?,supplier_id=? " +
                        "WHERE medicine_id=?");
                    ps.setString(1, name); ps.setString(2, company); ps.setString(3, type);
                    ps.setBigDecimal(4, price); ps.setInt(5, qty); ps.setInt(6, reorder);
                    ps.setDate(7, expiry); ps.setInt(8, supId); ps.setInt(9, existing.getMedicineId());
                    ps.executeUpdate();
                } else {
                    PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO medicines(name,company,medicine_type,price," +
                        "quantity_in_stock,reorder_level,expiry_date,supplier_id) VALUES(?,?,?,?,?,?,?,?)");
                    ps.setString(1, name); ps.setString(2, company); ps.setString(3, type);
                    ps.setBigDecimal(4, price); ps.setInt(5, qty); ps.setInt(6, reorder);
                    ps.setDate(7, expiry); ps.setInt(8, supId);
                    ps.executeUpdate();
                }
                dlg.dispose();
                loadData();
                JOptionPane.showMessageDialog(this, isEdit ? "Medicine updated!" : "Medicine added!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(dlg, "Error: " + ex.getMessage());
            }
        });
        cancelBtn.addActionListener(e -> dlg.dispose());

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        btns.add(cancelBtn); btns.add(saveBtn);

        g.gridx = 0; g.gridy = labels.length; g.gridwidth = 2;
        g.insets = new Insets(16, 5, 5, 5);
        content.add(btns, g);

        dlg.setContentPane(content);
        dlg.setVisible(true);
    }

    private void loadSuppliers(JComboBox<String> combo) {
        try {
            ResultSet rs = db_con.getConnection()
                .createStatement().executeQuery("SELECT supplier_id, name FROM suppliers ORDER BY name");
            while (rs.next()) {
                combo.addItem(rs.getInt(1) + "|" + rs.getString(2));
            }
        } catch (SQLException ex) {
            showError("Cannot load suppliers: " + ex.getMessage());
        }
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
