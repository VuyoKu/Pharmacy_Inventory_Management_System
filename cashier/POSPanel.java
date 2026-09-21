package cashier;

import database_connection.db_con;
import models.Medicine;
import models.SaleItem;
import utilities.SessionManager;
import utilities.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class POSPanel extends JPanel {

    private JComboBox<String> medicineCombo;
    private JTextField qtyField;
    private JTable cartTable;
    private DefaultTableModel cartModel;
    private JLabel totalLabel;
    private List<SaleItem>  cart     = new ArrayList<>();
    private List<Medicine>  medicines = new ArrayList<>();

    private static final String[] CART_COLS = {
        "Medicine", "Type", "Qty", "Unit Price (R)", "Line Total (R)"
    };

    public POSPanel() {
        setLayout(new BorderLayout(0, 0));
        setBackground(UITheme.BG);
        buildUI();
        loadMedicines();
    }

    private void buildUI() {
        // Top: search/add bar 
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.LEFT, 12, 10));
        topBar.setBackground(UITheme.CARD_BG);
        topBar.setBorder(new EmptyBorder(8, 12, 8, 12));

        medicineCombo = new JComboBox<>();
        medicineCombo.setFont(UITheme.FONT_LABEL);
        medicineCombo.setPreferredSize(new Dimension(300, 34));

        qtyField = UITheme.styledField(5);
        qtyField.setText("1");
        qtyField.setPreferredSize(new Dimension(60, 34));

        JButton addToCartBtn = UITheme.successButton("Add to Cart");
        addToCartBtn.addActionListener(e -> addToCart());

        JButton checkStockBtn = UITheme.primaryButton("Check Stock");
        checkStockBtn.addActionListener(e -> checkStock());

        topBar.add(UITheme.formLabel("Medicine:"));
        topBar.add(medicineCombo);
        topBar.add(UITheme.formLabel("Qty:"));
        topBar.add(qtyField);
        topBar.add(addToCartBtn);
        topBar.add(Box.createHorizontalStrut(20));
        topBar.add(checkStockBtn);

        // Cart Table 
        cartModel = new DefaultTableModel(CART_COLS, 0) {
            @Override public boolean isCellEditable(int r, int c) { return false; }
        };
        cartTable = new JTable(cartModel);
        UITheme.styleTable(cartTable);

        JScrollPane cartScroll = new JScrollPane(cartTable);
        cartScroll.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(UITheme.BORDER_COLOR),
            "  Shopping Cart", 0, 0, UITheme.FONT_BOLD, UITheme.PRIMARY));
        cartScroll.getViewport().setBackground(UITheme.CARD_BG);

        // Bottom action bar
        JPanel bottomBar = new JPanel(new BorderLayout(12, 0));
        bottomBar.setBackground(UITheme.CARD_BG);
        bottomBar.setBorder(new EmptyBorder(12, 20, 12, 20));

        totalLabel = new JLabel("Total: R 0.00");
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        totalLabel.setForeground(UITheme.PRIMARY_DARK);

        JPanel actionBtns = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionBtns.setOpaque(false);
        JButton removeBtn   = UITheme.dangerButton("Remove Selected");
        JButton clearBtn    = UITheme.warningButton("Clear Cart 🗑");
        JButton checkoutBtn = UITheme.successButton("Checkout & Bill ✔");
        checkoutBtn.setFont(new Font("Segoe UI", Font.BOLD, 15));

        removeBtn.addActionListener(e -> removeSelected());
        clearBtn.addActionListener(e -> clearCart());
        checkoutBtn.addActionListener(e -> checkout());

        actionBtns.add(removeBtn);
        actionBtns.add(clearBtn);
        actionBtns.add(checkoutBtn);

        bottomBar.add(totalLabel, BorderLayout.WEST);
        bottomBar.add(actionBtns, BorderLayout.EAST);

        add(topBar,     BorderLayout.NORTH);
        add(cartScroll, BorderLayout.CENTER);
        add(bottomBar,  BorderLayout.SOUTH);
    }

    // Load available medicines from the database into the medicines list and populate the medicineCombo dropdown with their details.
    private void loadMedicines() {
        medicines.clear();
        medicineCombo.removeAllItems();
        try {
            ResultSet rs = db_con.getConnection()
                .createStatement().executeQuery(
                    "SELECT * FROM medicines WHERE quantity_in_stock > 0 ORDER BY name");
            while (rs.next()) {
                Medicine m = new Medicine();
                m.setMedicineId(rs.getInt("medicine_id"));
                m.setName(rs.getString("name"));
                m.setMedicineType(rs.getString("medicine_type"));
                m.setPrice(rs.getBigDecimal("price"));
                m.setQuantityInStock(rs.getInt("quantity_in_stock"));
                m.setExpiryDate(rs.getDate("expiry_date"));
                medicines.add(m);
                medicineCombo.addItem(m.getMedicineId() + " | " + m.getName() +
                    " [" + m.getMedicineType() + "] - R" + m.getPrice() +
                    " (" + m.getQuantityInStock() + " in stock)");
            }
        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this, "Failed to load medicines: " + ex.getMessage());
        }
    }

    // Add the selected medicine and quantity to the cart, checking for stock availability and updating the cart table accordingly.
    private void addToCart() {
        int idx = medicineCombo.getSelectedIndex();
        if (idx < 0) { JOptionPane.showMessageDialog(this, "Please select a medicine."); return; }

        int qty;
        try {
            qty = Integer.parseInt(qtyField.getText().trim());
            if (qty <= 0) throw new NumberFormatException();
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this, "Please enter a valid quantity (>0)."); return;
        }

        Medicine med = medicines.get(idx);

        // Check if already in cart
        for (SaleItem item : cart) {
            if (item.getMedicineId() == med.getMedicineId()) {
                int newQty = item.getQuantitySold() + qty;
                if (newQty > med.getQuantityInStock()) {
                    JOptionPane.showMessageDialog(this,
                        "Not enough stock. Available: " + med.getQuantityInStock()); return;
                }
                item.setQuantitySold(newQty);
                refreshCartTable();
                return;
            }
        }

        if (qty > med.getQuantityInStock()) {
            JOptionPane.showMessageDialog(this,
                "Insufficient stock. Only " + med.getQuantityInStock() + " available."); return;
        }

        cart.add(new SaleItem(med.getMedicineId(), med.getName(), qty, med.getPrice()));
        refreshCartTable();
    }

    // Refresh the cart table to display the current items in the cart, calculating the total amount and updating the totalLabel accordingly.
    private void refreshCartTable() {
        cartModel.setRowCount(0);
        BigDecimal total = BigDecimal.ZERO;
        for (SaleItem item : cart) {
            int medIdx = findMedicineIndex(item.getMedicineId());
            String type = medIdx >= 0 ? medicines.get(medIdx).getMedicineType() : "";
            BigDecimal lineTotal = item.getLineTotal();
            total = total.add(lineTotal);
            cartModel.addRow(new Object[]{
                item.getMedicineName(), type, item.getQuantitySold(),
                String.format("R %.2f", item.getPriceAtSale()),
                String.format("R %.2f", lineTotal)
            });
        }
        totalLabel.setText("Total: R " + String.format("%.2f", total));
    }

    // Find the index of a medicine in the medicines list by its ID, returning -1 if not found.
    private int findMedicineIndex(int medicineId) {
        for (int i = 0; i < medicines.size(); i++) {
            if (medicines.get(i).getMedicineId() == medicineId) return i;
        }
        return -1;
    }

    // Remove the selected item from the cart, updating the cart table and total amount accordingly. If no item is selected, show a message prompting the user to select an item first.
    private void removeSelected() {
        int row = cartTable.getSelectedRow();
        if (row < 0) { JOptionPane.showMessageDialog(this, "Select an item to remove."); return; }
        cart.remove(row);
        refreshCartTable();
    }

    // Clear all items from the cart after confirming with the user, and refresh the cart table to reflect the changes.
    private void clearCart() {
        if (cart.isEmpty()) return;
        int c = JOptionPane.showConfirmDialog(this, "Clear all items from cart?",
            "Confirm", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            cart.clear();
            refreshCartTable();
        }
    }

    // Check the stock and details of the selected medicine, displaying the information in a message dialog. If no medicine is selected, prompt the user to select one first.
    private void checkStock() {
        int idx = medicineCombo.getSelectedIndex();
        if (idx < 0) { JOptionPane.showMessageDialog(this, "Select a medicine first."); return; }
        Medicine m = medicines.get(idx);
        JOptionPane.showMessageDialog(this,
            String.format("<html><b>%s</b><br>" +
                "Type: %s<br>" +
                "Price: R %.2f<br>" +
                "In Stock: %d units<br>" +
                "Expiry: %s</html>",
                m.getName(), m.getMedicineType(), m.getPrice(),
                m.getQuantityInStock(), m.getExpiryDate()),
            "Stock Information", JOptionPane.INFORMATION_MESSAGE);
    }

    // Checkout the items in the cart, creating a sale record in the database, updating stock quantities, and generating a bill for the transaction. If the cart is empty, prompt the user to add items first.
    private void checkout() {
        if (cart.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Cart is empty. Add items first."); return;
        }

        // Compute total
        BigDecimal total = cart.stream()
            .map(SaleItem::getLineTotal)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        int confirm = JOptionPane.showConfirmDialog(this,
            String.format("Confirm sale of %d item(s) for R %.2f?", cart.size(), total),
            "Confirm Checkout", JOptionPane.YES_NO_OPTION);
        if (confirm != JOptionPane.YES_OPTION) return;

        try {
            Connection conn = db_con.getConnection();
            conn.setAutoCommit(false);

            // Insert sale header
            PreparedStatement salePs = conn.prepareStatement(
                "INSERT INTO sales(total_amount, user_id) VALUES(?,?)",
                Statement.RETURN_GENERATED_KEYS);
            salePs.setBigDecimal(1, total);
            salePs.setInt(2, SessionManager.getCurrentUser().getUserId());
            salePs.executeUpdate();

            ResultSet genKeys = salePs.getGeneratedKeys();
            genKeys.next();
            int saleId = genKeys.getInt(1);

            // Insert items & decrement stock
            for (SaleItem item : cart) {
                PreparedStatement itemPs = conn.prepareStatement(
                    "INSERT INTO sale_items(sale_id,medicine_id,quantity_sold,price_at_sale) VALUES(?,?,?,?)");
                itemPs.setInt(1, saleId);
                itemPs.setInt(2, item.getMedicineId());
                itemPs.setInt(3, item.getQuantitySold());
                itemPs.setBigDecimal(4, item.getPriceAtSale());
                itemPs.executeUpdate();

                PreparedStatement stockPs = conn.prepareStatement(
                    "UPDATE medicines SET quantity_in_stock = quantity_in_stock - ? WHERE medicine_id=?");
                stockPs.setInt(1, item.getQuantitySold());
                stockPs.setInt(2, item.getMedicineId());
                stockPs.executeUpdate();
            }

            conn.commit();
            conn.setAutoCommit(true);

            // Show bill
            new BillWindow(saleId, cart, total,
                SessionManager.getCurrentUser().getFullName(), this);

            // Clear cart and reload medicines
            cart.clear();
            refreshCartTable();
            loadMedicines();

        } catch (SQLException ex) {
            try { db_con.getConnection().rollback(); } catch (Exception ignored) {}
            JOptionPane.showMessageDialog(this, "Checkout failed: " + ex.getMessage());
        }
    }
}

