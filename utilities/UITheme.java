// applies a global theme for all frames and panels
package utilities;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;

import java.awt.*;

public class UITheme {
    // color palette
    public static final Color PRIMARY       = new Color(0, 102, 153);   // deep teal-blue
    public static final Color PRIMARY_DARK  = new Color(0,  72, 110);
    public static final Color PRIMARY_LIGHT = new Color(0, 140, 200);

    public static final Color DANGER        = new Color(220, 53, 69);    // red
    public static final Color WARNING       = new Color(255, 193, 7);    // yellow/orange
    public static final Color SUCCESS       = new Color(40, 167, 69);    // green
    public static final Color ACCENT        = new Color(23, 162, 184);   // cyan/teal

    public static final Color BG            = new Color(245, 248, 252);
    public static final Color CARD_BG       = Color.WHITE;
    public static final Color SIDEBAR_BG    = new Color(18,  52,  86);
    public static final Color SIDEBAR_TEXT  = new Color(200, 220, 240);
    public static final Color TABLE_HEADER  = new Color(0, 102, 153);
    public static final Color TABLE_ALT     = new Color(235, 243, 252);
    public static final Color TEXT_PRIMARY  = new Color(30,  40,  50);
    public static final Color TEXT_MUTED    = new Color(110, 130, 150);
    public static final Color BORDER_COLOR  = new Color(200, 215, 230);


    // fonts 
    public static final Font FONT_TITLE   = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_HEADING = new Font("Segoe UI", Font.BOLD,  15);
    public static final Font FONT_LABEL   = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_BOLD    = new Font("Segoe UI", Font.BOLD,  13);
    public static final Font FONT_SMALL   = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_MONO    = new Font("Consolas",  Font.PLAIN, 13);


    public static void applyGlobalLook() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        UIManager.put("Panel.background",           BG);
        UIManager.put("OptionPane.background",      BG);
        UIManager.put("OptionPane.messageFont",     FONT_LABEL);
        UIManager.put("Button.font",                FONT_LABEL);
        UIManager.put("TextField.font",             FONT_LABEL);
        UIManager.put("ComboBox.font",              FONT_LABEL);
        UIManager.put("Label.font",                 FONT_LABEL);
        UIManager.put("Table.font",                 FONT_LABEL);
        UIManager.put("TableHeader.font",           FONT_BOLD);
        UIManager.put("TabbedPane.font",            FONT_BOLD);
        UIManager.put("TabbedPane.selected",        PRIMARY);
        UIManager.put("TabbedPane.selectedForeground", Color.WHITE);
    }


    // buttons coloring
    public static JButton primaryButton(String text) {
        return styledButton(text, PRIMARY, Color.WHITE);
    }

    public static JButton successButton(String text) {
        return styledButton(text, new Color(22, 163, 74), new Color(255, 255, 255));
    }

    public static JButton dangerButton(String text) {
        return styledButton(text, DANGER, Color.WHITE);
    }

    public static JButton warningButton(String text) {
        return styledButton(text, new Color(255, 193, 7), new Color(40, 40, 40));
    }

    public static JButton accentButton(String text) {
        return styledButton(text, ACCENT, Color.WHITE);
    }


    // helper method to style buttons 
    private static JButton styledButton(String text, Color bg, Color fg) {
        JButton btn = new JButton(text);
        btn.setFont(FONT_BOLD);
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setOpaque(true);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setBorder(new EmptyBorder(9, 22, 9, 22));
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent e) {
                btn.setBackground(bg.darker());
            }
            public void mouseExited(java.awt.event.MouseEvent e) {
                btn.setBackground(bg);
            }
        });
        return btn;
    }


    // helper method to style text fields
    public static JTextField styledField(int cols) {
        JTextField f = new JTextField(cols);
        f.setFont(FONT_LABEL);
        f.setBorder(fieldBorder());
        f.setBackground(Color.WHITE);
        return f;
    }


    // helper method to style password fields
    public static JPasswordField styledPasswordField(int cols) {
        JPasswordField f = new JPasswordField(cols);
        f.setFont(FONT_LABEL);
        f.setBorder(fieldBorder());
        f.setBackground(Color.WHITE);
        return f;
    }


    // helper method to style combo boxes
    public static JComboBox<String> styledCombo(String[] items) {
        JComboBox<String> c = new JComboBox<>(items);
        c.setFont(FONT_LABEL);
        c.setBackground(Color.WHITE);
        return c;
    }


    // helper method to style borders for text fields and password fields
    private static Border fieldBorder() {
        return new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(6, 10, 6, 10)
        );
    }


    // labels
    public static JLabel titleLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_TITLE);
        l.setForeground(PRIMARY);
        return l;
    }



    public static JLabel headingLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_HEADING);
        l.setForeground(TEXT_PRIMARY);
        return l;
    }



    // table styling
    public static void styleTable(JTable table) {
        table.setFont(FONT_LABEL);
        table.setRowHeight(28);
        table.setShowGrid(false);
        table.setIntercellSpacing(new Dimension(0, 0));
        table.setSelectionBackground(PRIMARY_LIGHT);
        table.setSelectionForeground(Color.WHITE);
        table.setBackground(CARD_BG);
        table.setFillsViewportHeight(true);

        JTableHeader header = table.getTableHeader();
        header.setFont(FONT_BOLD);
        header.setBackground(TABLE_HEADER);
        header.setForeground(Color.WHITE);
        header.setPreferredSize(new Dimension(header.getWidth(), 34));
        header.setReorderingAllowed(false);

        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object val,
                    boolean sel, boolean foc, int row, int col) {
                super.getTableCellRendererComponent(t, val, sel, foc, row, col);
                if (!sel) {
                    setBackground(row % 2 == 0 ? CARD_BG : TABLE_ALT);
                    setForeground(TEXT_PRIMARY);
                }
                setBorder(new EmptyBorder(0, 10, 0, 10));
                return this;
            }
        });
    }

    /* ── Panels ──────────────────────────────────────────────── */

    public static JPanel card(LayoutManager layout) {
        JPanel p = new JPanel(layout);
        p.setBackground(CARD_BG);
        p.setBorder(new CompoundBorder(
            new LineBorder(BORDER_COLOR, 1, true),
            new EmptyBorder(16, 16, 16, 16)
        ));
        return p;
    }

    public static JLabel formLabel(String text) {
        JLabel l = new JLabel(text);
        l.setFont(FONT_BOLD);
        l.setForeground(TEXT_PRIMARY);
        return l;
    }

}
