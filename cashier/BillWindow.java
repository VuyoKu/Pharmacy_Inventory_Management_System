package cashier;

import models.SaleItem;
import utilities.UITheme;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.print.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class BillWindow extends JDialog {

    private final int saleId;
    private final List<SaleItem> items;
    private final BigDecimal total;
    private final String cashierName;

    public BillWindow(int saleId, List<SaleItem> items, BigDecimal total,
                      String cashierName, Component parent) {
        super(SwingUtilities.getWindowAncestor(parent), "Bill – Sale #" + saleId,
              ModalityType.APPLICATION_MODAL);
        this.saleId     = saleId;
        this.items      = items;
        this.total      = total;
        this.cashierName = cashierName;
        buildUI();
        setSize(500, 680);
        setLocationRelativeTo(parent);
        setVisible(true);
    }

    private void buildUI() {
        JPanel outer = new JPanel(new BorderLayout(0, 12));
        outer.setBackground(UITheme.BG);
        outer.setBorder(new EmptyBorder(20, 20, 20, 20));

        // ── Receipt panel ─────────────────────────────────────
        JPanel receipt = new JPanel();
        receipt.setLayout(new BoxLayout(receipt, BoxLayout.Y_AXIS));
        receipt.setBackground(Color.WHITE);
        receipt.setBorder(new EmptyBorder(24, 28, 24, 28));

        // Header
        addCentredLabel(receipt, "💊  HealthFirst Pharmacy", new Font("Segoe UI", Font.BOLD, 18), UITheme.PRIMARY);
        addCentredLabel(receipt, "123 Main Street, Johannesburg, SA", UITheme.FONT_SMALL, UITheme.TEXT_MUTED);
        addCentredLabel(receipt, "Tel: 011-123-4567 | healthfirst@pharmacy.co.za", UITheme.FONT_SMALL, UITheme.TEXT_MUTED);
        receipt.add(separator());
        addCentredLabel(receipt, "TAX INVOICE / RECEIPT", UITheme.FONT_BOLD, UITheme.PRIMARY);
        receipt.add(Box.createVerticalStrut(4));

        String dateStr = new SimpleDateFormat("dd MMM yyyy  HH:mm:ss").format(new Date());
        addCentredLabel(receipt, "Date: " + dateStr, UITheme.FONT_SMALL, UITheme.TEXT_MUTED);
        addCentredLabel(receipt, "Sale ID: #" + saleId, UITheme.FONT_SMALL, UITheme.TEXT_MUTED);
        addCentredLabel(receipt, "Cashier: " + cashierName, UITheme.FONT_SMALL, UITheme.TEXT_MUTED);
        receipt.add(separator());

        // Column headers
        JPanel colHeader = new JPanel(new GridLayout(1, 4, 0, 0));
        colHeader.setOpaque(false);
        colHeader.setMaximumSize(new Dimension(Integer.MAX_VALUE, 22));
        colHeader.add(boldLabel("Item"));
        colHeader.add(rightLabel("Qty"));
        colHeader.add(rightLabel("Price"));
        colHeader.add(rightLabel("Total"));
        receipt.add(colHeader);
        receipt.add(thinSeparator());

        // Line items
        for (SaleItem item : items) {
            JPanel row = new JPanel(new GridLayout(1, 4, 0, 0));
            row.setOpaque(false);
            row.setMaximumSize(new Dimension(Integer.MAX_VALUE, 24));
            row.add(smallLabel(item.getMedicineName()));
            row.add(rightSmall(String.valueOf(item.getQuantitySold())));
            row.add(rightSmall(String.format("R%.2f", item.getPriceAtSale())));
            row.add(rightSmall(String.format("R%.2f", item.getLineTotal())));
            receipt.add(row);
        }

        receipt.add(thinSeparator());

        // Total
        JPanel totalRow = new JPanel(new BorderLayout());
        totalRow.setOpaque(false);
        totalRow.setMaximumSize(new Dimension(Integer.MAX_VALUE, 36));
        JLabel totLabel = new JLabel("TOTAL");
        totLabel.setFont(new Font("Segoe UI", Font.BOLD, 15));
        totLabel.setForeground(UITheme.TEXT_PRIMARY);
        JLabel totVal = new JLabel(String.format("R %.2f", total));
        totVal.setFont(new Font("Segoe UI", Font.BOLD, 15));
        totVal.setForeground(UITheme.PRIMARY);
        totVal.setHorizontalAlignment(SwingConstants.RIGHT);
        totalRow.add(totLabel, BorderLayout.WEST);
        totalRow.add(totVal, BorderLayout.EAST);
        receipt.add(totalRow);

        receipt.add(separator());
        addCentredLabel(receipt, "Items: " + items.size(), UITheme.FONT_SMALL, UITheme.TEXT_MUTED);
        receipt.add(Box.createVerticalStrut(12));
        addCentredLabel(receipt, "Thank you for choosing HealthFirst Pharmacy!", UITheme.FONT_BOLD, UITheme.ACCENT);
        addCentredLabel(receipt, "Please keep this receipt for your records.", UITheme.FONT_SMALL, UITheme.TEXT_MUTED);
        receipt.add(Box.createVerticalStrut(8));
        addCentredLabel(receipt, "★★★★★  Excellent Service  ★★★★★", UITheme.FONT_SMALL, UITheme.WARNING);

        JScrollPane scroll = new JScrollPane(receipt);
        scroll.setBorder(BorderFactory.createLineBorder(UITheme.BORDER_COLOR));

        // ── Buttons ───────────────────────────────────────────
        JPanel btns = new JPanel(new FlowLayout(FlowLayout.CENTER, 12, 0));
        btns.setOpaque(false);

        JButton printBtn = UITheme.primaryButton("🖨 Print");
        JButton saveBtn  = UITheme.accentButton("💾 Save as Text");
        JButton closeBtn = UITheme.dangerButton("✖ Close");

        printBtn.addActionListener(e -> printReceipt(receipt));
        saveBtn.addActionListener(e -> saveReceipt());
        closeBtn.addActionListener(e -> dispose());

        btns.add(printBtn); btns.add(saveBtn); btns.add(closeBtn);

        outer.add(scroll, BorderLayout.CENTER);
        outer.add(btns,   BorderLayout.SOUTH);
        setContentPane(outer);
    }

    // ── Helpers ───────────────────────────────────────────────

    private void addCentredLabel(JPanel p, String text, Font font, Color color) {
        JLabel l = new JLabel(text);
        l.setFont(font);
        l.setForeground(color);
        l.setAlignmentX(Component.CENTER_ALIGNMENT);
        l.setHorizontalAlignment(SwingConstants.CENTER);
        p.add(l);
    }

    private JSeparator separator() {
        JSeparator s = new JSeparator();
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 10));
        s.setForeground(UITheme.BORDER_COLOR);
        return s;
    }

    private JSeparator thinSeparator() {
        JSeparator s = new JSeparator();
        s.setMaximumSize(new Dimension(Integer.MAX_VALUE, 6));
        s.setForeground(new Color(220, 230, 240));
        return s;
    }

    private JLabel boldLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(UITheme.FONT_BOLD);
        l.setForeground(UITheme.TEXT_PRIMARY);
        return l;
    }

    private JLabel rightLabel(String t) {
        JLabel l = boldLabel(t);
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }

    private JLabel smallLabel(String t) {
        JLabel l = new JLabel(t);
        l.setFont(UITheme.FONT_SMALL);
        l.setForeground(UITheme.TEXT_PRIMARY);
        return l;
    }

    private JLabel rightSmall(String t) {
        JLabel l = smallLabel(t);
        l.setHorizontalAlignment(SwingConstants.RIGHT);
        return l;
    }

    private void printReceipt(JPanel receipt) {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable((g, pf, page) -> {
            if (page > 0) return Printable.NO_SUCH_PAGE;
            Graphics2D g2 = (Graphics2D) g;
            g2.translate(pf.getImageableX(), pf.getImageableY());
            double scaleX = pf.getImageableWidth()  / receipt.getWidth();
            double scaleY = pf.getImageableHeight() / receipt.getHeight();
            double scale  = Math.min(scaleX, scaleY);
            g2.scale(scale, scale);
            receipt.printAll(g2);
            return Printable.PAGE_EXISTS;
        });
        if (job.printDialog()) {
            try { job.print(); }
            catch (PrinterException ex) {
                JOptionPane.showMessageDialog(this, "Print failed: " + ex.getMessage());
            }
        }
    }

    private void saveReceipt() {
        StringBuilder sb = new StringBuilder();
        String line = "=".repeat(50);
        sb.append(line).append("\n");
        sb.append("         HEALTHFIRST PHARMACY\n");
        sb.append("   123 Main Street, Johannesburg, SA\n");
        sb.append(line).append("\n");
        sb.append("TAX INVOICE / RECEIPT\n");
        sb.append("Date:     ").append(new SimpleDateFormat("dd MMM yyyy HH:mm:ss").format(new Date())).append("\n");
        sb.append("Sale ID:  #").append(saleId).append("\n");
        sb.append("Cashier:  ").append(cashierName).append("\n");
        sb.append(line).append("\n");
        sb.append(String.format("%-22s %4s %8s %10s%n", "ITEM", "QTY", "PRICE", "TOTAL"));
        sb.append("-".repeat(50)).append("\n");
        for (SaleItem item : items) {
            sb.append(String.format("%-22s %4d R%7.2f R%9.2f%n",
                item.getMedicineName(), item.getQuantitySold(),
                item.getPriceAtSale(), item.getLineTotal()));
        }
        sb.append("=".repeat(50)).append("\n");
        sb.append(String.format("%-36s R%9.2f%n", "TOTAL", total));
        sb.append("=".repeat(50)).append("\n");
        sb.append("Thank you for shopping at HealthFirst!\n");

        JFileChooser fc = new JFileChooser();
        fc.setSelectedFile(new java.io.File("Bill_Sale_" + saleId + ".txt"));
        if (fc.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            try (java.io.FileWriter fw = new java.io.FileWriter(fc.getSelectedFile())) {
                fw.write(sb.toString());
                JOptionPane.showMessageDialog(this, "Bill saved successfully!");
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "Save failed: " + ex.getMessage());
            }
        }
    }
}

