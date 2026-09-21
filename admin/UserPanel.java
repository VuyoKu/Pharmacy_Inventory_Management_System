package admin;

import database_connection.db_con;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import models.User;
import utilities.SessionManager;
import utilities.UITheme;

public class UserPanel extends JPanel {

    private JTable table;
    private DefaultTableModel model;
    private List<User> users = new ArrayList<>();

    private static final String[] COLS = {"ID", "Username", "Full Name", "Role"};

    public UserPanel() {
        setLayout(new BorderLayout());
        setBackground(UITheme.BG);
        buildUI();
        loadData();
    }

    private void buildUI() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(UITheme.BG);
        toolbar.setBorder(new EmptyBorder(16, 20, 12, 20));
        toolbar.add(UITheme.headingLabel("User Management"), BorderLayout.WEST);

        JPanel btns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
        btns.setOpaque(false); // Makes the button panel transparent so the toolbar background is visible.
        JButton addBtn    = UITheme.successButton("Create User");
        JButton delBtn    = UITheme.dangerButton("Delete");
        JButton resetBtn  = UITheme.warningButton("Reset Password");
        JButton refreshBtn = UITheme.accentButton("Refresh");

        // button action listners
        addBtn.addActionListener(e -> openDialog());
        delBtn.addActionListener(e -> deleteSelected());
        resetBtn.addActionListener(e -> resetPassword());
        refreshBtn.addActionListener(e -> loadData());

        btns.add(addBtn); 
        btns.add(delBtn);
        btns.add(resetBtn); 
        btns.add(refreshBtn);
        toolbar.add(btns, BorderLayout.EAST);

        // Prevents users from editing table cells directly.
        model = new DefaultTableModel(COLS, 0) {
            // Creates a table using the defined columns and rows.
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };

        table = new JTable(model);
        UITheme.styleTable(table);

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(UITheme.CARD_BG);

        // Info panel
        JLabel info = new JLabel("<html>Note: Passwords are stored as plaintext</html>");
        info.setFont(UITheme.FONT_SMALL);
        info.setForeground(UITheme.WARNING);
        info.setBorder(new EmptyBorder(8, 20, 8, 20));

        add(toolbar, BorderLayout.NORTH);
        add(scroll, BorderLayout.CENTER);
        add(info, BorderLayout.SOUTH);
    }

    // Loads the users from the database and displays them in the table.
    private void loadData() {
        // Removes all users currently stored in the list before loading fresh data.
        users.clear();
        model.setRowCount(0);
        try {
            ResultSet rs = db_con.getConnection()
                .createStatement().executeQuery(
                    "SELECT user_id,username,full_name,role FROM users ORDER BY role,username");
            while (rs.next()) {
                User u = new User(rs.getInt(1), rs.getString(2), "", rs.getString(4), rs.getString(3));
                users.add(u);
                model.addRow(new Object[]{u.getUserId(), u.getUsername(), u.getFullName(), u.getRole()});
            }
        } catch (SQLException ex) {
            showError("Load failed: " + ex.getMessage());
        }
    }

    // Deletes the selected user from the database after checking that it can be safely removed.
    private void deleteSelected() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a user first."); return; }
        User u = users.get(row);
        if (u.getUserId() == SessionManager.getCurrentUser().getUserId()) {
            JOptionPane.showMessageDialog(this, "You cannot delete your own account."); return;
        }
        if ("Admin".equals(u.getRole())) {
            JOptionPane.showMessageDialog(this, "Admin accounts cannot be deleted here for safety."); return;
        }
        int c = JOptionPane.showConfirmDialog(this,
            "Delete user '" + u.getUsername() + "'?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (c != JOptionPane.YES_OPTION) return;
        try {
            PreparedStatement ps = db_con.getConnection()
                .prepareStatement("DELETE FROM users WHERE user_id=?");
            ps.setInt(1, u.getUserId());
            ps.executeUpdate();
            loadData();
        } catch (SQLException ex) {
            showError("Delete failed: " + ex.getMessage());
        }
    }

    // Resets the selected user's password and saves the new password in the database.
    private void resetPassword() {
        int row = table.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select a user first."); return; }
        User u = users.get(row);
        String newPass = JOptionPane.showInputDialog(this,
            "Enter new password for '" + u.getUsername() + "':");
        if (newPass == null || newPass.trim().isEmpty()) return;
        try {
            PreparedStatement ps = db_con.getConnection()
                .prepareStatement("UPDATE users SET password=? WHERE user_id=?");
            ps.setString(1, newPass.trim());
            ps.setInt(2, u.getUserId());
            ps.executeUpdate();
            JOptionPane.showMessageDialog(this, "Password updated successfully.");
        } catch (SQLException ex) {
            showError("Reset failed: " + ex.getMessage());
        }
    }

    
    // Opens a window where the administrator can create a new user.
    private void openDialog() {
        JDialog dlg = new JDialog((Frame) SwingUtilities.getWindowAncestor(this), // Finds the window that contains this panel so the dialog can use it as its parent.
         "Create User", true);
        dlg.setSize(420, 360);
        dlg.setLocationRelativeTo(this);

        JPanel content = new JPanel(new GridBagLayout());
        content.setBackground(UITheme.CARD_BG);
        content.setBorder(new EmptyBorder(24, 28, 24, 28));
        GridBagConstraints g = new GridBagConstraints();
        // Makes the form fields stretch across the available space.
        g.fill = GridBagConstraints.HORIZONTAL; 
        // Adds spacing around each form component: top, left, bottom, and right.
        g.insets = new Insets(7, 5, 7, 5);

        JTextField usernameF = UITheme.styledField(20);
        JTextField fullNameF = UITheme.styledField(20);
        JPasswordField passF = UITheme.styledPasswordField(20);
        JComboBox<String> roleCombo = UITheme.styledCombo(new String[]{"Cashier", "Admin"});

        String[] labels = {"Username *", "Full Name *", "Password *", "Role"};
        JComponent[] comps = {usernameF, fullNameF, passF, roleCombo};
        // Loops through each form field and places its label and input component in the correct grid position.
        for (int i = 0; i < labels.length; i++) {
            // Places the label in the first column.
            g.gridx = 0;
            g.gridy = i;
            g.weightx = 0.35;
            content.add(UITheme.formLabel(labels[i]), g);
            // Places the input field in the second column with more space.
            g.gridx = 1;
            g.weightx = 0.65;
            content.add(comps[i], g);
}

        JButton saveBtn   = UITheme.successButton("Create");
        JButton cancelBtn = UITheme.dangerButton("Cancel");
        saveBtn.addActionListener(e -> {
            String user = usernameF.getText().trim();
            String name = fullNameF.getText().trim();
            String pass = new String(passF.getPassword()).trim();
            if (user.isEmpty() || name.isEmpty() || pass.isEmpty()) {
                JOptionPane.showMessageDialog(dlg, "All fields are required."); return;
            }
            try {
                PreparedStatement ps = db_con.getConnection()
                    .prepareStatement("INSERT INTO users(username,password,role,full_name) VALUES(?,?,?,?)");
                ps.setString(1, user); ps.setString(2, pass);
                ps.setString(3, (String) roleCombo.getSelectedItem()); ps.setString(4, name);
                ps.executeUpdate();
                dlg.dispose(); loadData();
                JOptionPane.showMessageDialog(this, "User created successfully!");
            } catch (SQLIntegrityConstraintViolationException ex) {
                JOptionPane.showMessageDialog(dlg, "Username already exists.");
            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(dlg, "Error: " + ex.getMessage());
            }
        });
        // Closes the dialog window when the Cancel button is clicked.
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
