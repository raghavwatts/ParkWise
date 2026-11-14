package com.parkwise.ui;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.Dimension;

/**
 * Factory methods for styled Swing components.
 * Reduces boilerplate in individual screens.
 */
public final class UIHelper {

    private UIHelper() {}

    /** Creates a styled primary action button. */
    public static JButton primaryButton(String text) {
        JButton btn = new JButton(text);
        btn.setFont(UIConstants.FONT_BUTTON);
        btn.setBackground(UIConstants.ACCENT);
        btn.setForeground(Color.WHITE);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.setPreferredSize(UIConstants.BUTTON_SIZE);
        return btn;
    }

    /** Creates a styled danger (delete/cancel) button. */
    public static JButton dangerButton(String text) {
        JButton btn = primaryButton(text);
        btn.setBackground(UIConstants.DANGER);
        return btn;
    }

    /** Creates a styled success button. */
    public static JButton successButton(String text) {
        JButton btn = primaryButton(text);
        btn.setBackground(UIConstants.SUCCESS);
        return btn;
    }

    /** Creates a styled warning button. */
    public static JButton warningButton(String text) {
        JButton btn = primaryButton(text);
        btn.setBackground(UIConstants.WARNING);
        btn.setForeground(Color.WHITE);
        return btn;
    }

    /** Creates a styled section title label. */
    public static JLabel titleLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_TITLE);
        lbl.setForeground(UIConstants.PRIMARY);
        return lbl;
    }

    /** Creates a body-weight label. */
    public static JLabel bodyLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setFont(UIConstants.FONT_BODY);
        return lbl;
    }

    /** Creates a styled panel card. */
    public static JPanel card() {
        JPanel p = new JPanel();
        p.setBackground(UIConstants.CARD_BG);
        p.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(0xDDE2E9), 1),
            new EmptyBorder(UIConstants.PAD, UIConstants.PAD, UIConstants.PAD, UIConstants.PAD)
        ));
        return p;
    }

    /** Applies consistent styling to a JTable. */
    public static void styleTable(JTable table) {
        table.setFont(UIConstants.FONT_BODY);
        table.setRowHeight(30);
        table.setGridColor(new Color(0xDDE2E9));
        table.setSelectionBackground(UIConstants.ACCENT);
        table.setSelectionForeground(Color.WHITE);
        table.setShowGrid(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setFillsViewportHeight(true);

        // ── Header styling ────────────────────────────────────────────────────
        JTableHeader header = table.getTableHeader();
        header.setFont(UIConstants.FONT_SUBTITLE);
        header.setReorderingAllowed(false);
        header.setPreferredSize(new Dimension(header.getWidth(), 34));

        // Custom header renderer — forces dark background + white text
        // regardless of system Look & Feel overrides
        header.setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        t, value, false, false, row, col);
                lbl.setBackground(UIConstants.TABLE_HEADER);
                lbl.setForeground(Color.WHITE);
                lbl.setFont(UIConstants.FONT_SUBTITLE);
                lbl.setHorizontalAlignment(SwingConstants.LEFT);
                lbl.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 0, 2, 1, UIConstants.ACCENT),
                    BorderFactory.createEmptyBorder(0, 8, 0, 4)
                ));
                lbl.setOpaque(true);
                return lbl;
            }
        });

        // ── Row cell renderer — alternating rows, always visible text ─────────
        table.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object value,
                    boolean isSelected, boolean hasFocus, int row, int col) {
                JLabel lbl = (JLabel) super.getTableCellRendererComponent(
                        t, value, isSelected, hasFocus, row, col);
                lbl.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 4));
                if (isSelected) {
                    lbl.setBackground(UIConstants.ACCENT);
                    lbl.setForeground(Color.WHITE);
                } else {
                    lbl.setBackground(row % 2 == 0 ? Color.WHITE : UIConstants.TABLE_ALT_ROW);
                    lbl.setForeground(Color.BLACK);
                }
                lbl.setOpaque(true);
                return lbl;
            }
        });
    }

    /** Creates a standard text field. */
    public static JTextField textField(int columns) {
        JTextField tf = new JTextField(columns);
        tf.setFont(UIConstants.FONT_BODY);
        return tf;
    }

    /** Wraps a component in a labelled form row (label on left, field on right). */
    public static JPanel formRow(String labelText, JComponent field) {
        JPanel row = new JPanel(new BorderLayout(8, 0));
        row.setOpaque(false);
        JLabel lbl = new JLabel(labelText);
        lbl.setFont(UIConstants.FONT_BODY);
        lbl.setPreferredSize(new Dimension(160, 28));
        row.add(lbl, BorderLayout.WEST);
        row.add(field, BorderLayout.CENTER);
        return row;
    }

    /** Shows an error dialog. */
    public static void showError(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "Error", JOptionPane.ERROR_MESSAGE);
    }

    /** Shows an info dialog. */
    public static void showInfo(Component parent, String message) {
        JOptionPane.showMessageDialog(parent, message, "ParkWise", JOptionPane.INFORMATION_MESSAGE);
    }

    /** Shows a yes/no confirmation dialog. Returns true if YES. */
    public static boolean confirm(Component parent, String message) {
        return JOptionPane.showConfirmDialog(parent, message, "Confirm",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
    }
}
