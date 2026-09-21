package admin;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import database_connection.db_con;
import models.Supplier;
import utilities.UITheme;

public class SupplierPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private List<Supplier> suppliers = new ArrayList<>();

    private static final String[] COLS = {"ID", "Company Name", "Contact Person", "Phone", "Email", "Address"};

    public SupplierPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG);
        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel toolbar = new JPanel(new BorderLayout(12, 0));
        toolbar.setBackground(UITheme.BG);
        toolbar.setBorder(new EmptyBorder(16, 20, 12, 20));
        toolbar.add(UITheme.headingLabel("🏭  Supplier Management"), BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false);
        JButton addBtn    = UITheme.successButton("+ Add Supplier");
        JButton editBtn   = UITheme.primaryButton("✎ Edit");
        JButton delBtn    = UITheme.dangerButton("✖ Delete");
        JButton refreshBtn = UITheme.accentButton("↺ Refresh");

        addBtn.addActionListener(e -> openDialog(null));
        editBtn.addActionListener(e -> editSelected());
        delBtn.addActionListener(e -> deleteSelected());
        refreshBtn.addActionListener(e -> loadData());

        btns.add(addBtn); btns.add(editBtn); btns.add(delBtn); btns.add(refreshBtn);
        toolbar.add(btns, BorderLayout.EAST);

        model = new DefaultTableModel(COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        table = new JTable(model);
        UITheme.styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UITheme.CARD_BG);

        add(toolbar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
    }

    private void loadData() {
        suppliers.clear();
        model.setRowCount(0);
        try {
            ResultSet rs = db_con.getConnection()
                .createStatement().executeQuery("SELECT * FROM suppliers ORDER BY name");
            while (rs.next()) {
                Supplier s = new Supplier(
                    rs.getInt("supplier_id"), rs.getString("name"),
                    rs.getString("contact_person"), rs.getString("phone"),
                    rs.getString("email"), rs.getString("address")
                );
                suppliers.add(s);
                model.addRow(new Object[]{
                    s.getSupplierId(), s.getName(), s.getContactPerson(),
                    s.getPhone(), s.getEmail(), s.getAddress()
                });
            }
        } catch (SQLException ex) {
            showError("Load failed: " + ex.getMessage());
        }
    }

    private void editSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a supplier first."); return; }
        openDialog(suppliers.get(row));
    }

    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a supplier first."); return; }
        Supplier s = suppliers.get(row);
        int c = JOptionPane.showConfirmDialog(this,
            "Delete supplier '" + s.getName() + "'?", "Confirm Delete",
            JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (c != JOptionPane.YES_OPTION) return;
        try {
            PreparedStatement ps = db_con.getConnection()
                .prepareStatement("DELETE FROM suppliers WHERE supplier_id=?");
            ps.setInt(1, s.getSupplierId());
            ps.executeUpdate();
            loadData();
        } catch (SQLException ex) {
            showError("Delete failed: " + ex.getMessage());
        }
    }

    private void openDialog(Supplier existing) {
        boolean isEdit = (existing != null);
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this),
            isEdit ? "Edit Supplier" : "Add Supplier", true);
        dlg.setSize(460, 440);
        dlg.setLocationRelativeTo(this);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(UITheme.CARD_BG);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));
        GridBagConstraints g = new GridBagConstraints();
        g.fill = GridBagConstraints.HORIZONTAL; g.insets = new Insets(6, 5, 6, 5);

        JTextField nameF    = UITheme.styledField(22);
        JTextField contactF = UITheme.styledField(22);
        JTextField phoneF   = UITheme.styledField(22);
        JTextField emailF   = UITheme.styledField(22);
        JTextArea  addrF    = new JTextArea(3, 22);
        addrF.setFont(UITheme.FONT_LABEL);
        addrF.setLineWrap(true);
        addrF.setWrapStyleWord(true);

        if (isEdit) {
            nameF.setText(existing.getName());
            contactF.setText(existing.getContactPerson());
            phoneF.setText(existing.getPhone());
            emailF.setText(existing.getEmail());
            addrF.setText(existing.getAddress());
        }

        String[] labels = {"Company Name *", "Contact Person", "Phone *", "Email", "Address"};
        JComponent[] comps = {nameF, contactF, phoneF, emailF, new JScrollPane(addrF)};
        for (int i = 0; i < labels.length; i++) {
            g.gridx = 0; g.gridy = i; g.weightx = 0.35;
            content.add(UITheme.formLabel(labels[i]), g);
            g.gridx = 1; g.weightx = 0.65;
            content.add(comps[i], g);
        }

        JButton saveBtn   = UITheme.successButton(isEdit ? "Update" : "Save");
        JButton cancelBtn = UITheme.dangerButton("Cancel");
        saveBtn.addActionListener(e -> {
            if (nameF.getText().trim().isEmpty() || phoneF.getText().trim().isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "Name and Phone are required."); return;
            }
            try {
                Connection conn = db_con.getConnection();
                if (isEdit) {
                    PreparedStatement ps = conn.prepareStatement(
                        "UPDATE suppliers SET name=?,contact_person=?,phone=?,email=?,address=? WHERE supplier_id=?");
                    ps.setString(1, nameF.getText().trim()); ps.setString(2, contactF.getText().trim());
                    ps.setString(3, phoneF.getText().trim()); ps.setString(4, emailF.getText().trim());
                    ps.setString(5, addrF.getText().trim()); ps.setInt(6, existing.getSupplierId());
                    ps.executeUpdate();
                } else {
                    PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO suppliers(name,contact_person,phone,email,address) VALUES(?,?,?,?,?)");
                    ps.setString(1, nameF.getText().trim()); ps.setString(2, contactF.getText().trim());
                    ps.setString(3, phoneF.getText().trim()); ps.setString(4, emailF.getText().trim());
                    ps.setString(5, addrF.getText().trim());
                    ps.executeUpdate();
                }
                dlg.dispose(); loadData();
                JOptionPane.showMessageDialog(this, isEdit ? "Supplier updated!" : "Supplier added!");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dlg, "Error: " + ex.getMessage());
            }
        });
        cancelBtn.addActionListener(e -> dlg.dispose());

        JPanel btnsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btnsPanel.setOpaque(false);
        btnsPanel.add(cancelBtn); btnsPanel.add(saveBtn);

        g.gridx = 0; g.gridy = labels.length; g.gridwidth = 2;
        g.insets = new Insets(16, 5, 5, 5);
        content.add(btnsPanel, g);

        dlg.setContentPane(content);
        dlg.setVisible(true);
    }

    private void showError(String msg) {
        JOptionPane.showMessageDialog(this, msg, "Error", JOptionPane.ERROR_MESSAGE);
    }
}
