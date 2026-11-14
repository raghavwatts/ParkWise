package com.parkwise.ui;

import java.awt.*;

/**
 * Centralised UI constants for consistent styling across all Swing screens.
 */
public final class UIConstants {

    private UIConstants() {}

    // ── Colours ───────────────────────────────────────────────────────────────
    public static final Color PRIMARY       = new Color(0x1E3A5F);   // deep navy
    public static final Color ACCENT        = new Color(0x2E86AB);   // teal blue
    public static final Color SUCCESS       = new Color(0x27AE60);   // green
    public static final Color WARNING       = new Color(0xF39C12);   // amber
    public static final Color DANGER        = new Color(0xC0392B);   // red
    public static final Color LIGHT_BG      = new Color(0xF4F6F9);   // panel background
    public static final Color CARD_BG       = Color.WHITE;
    public static final Color TABLE_HEADER  = new Color(0x2C3E50);
    public static final Color TABLE_ALT_ROW = new Color(0xEBF5FB);

    // ── Fonts ─────────────────────────────────────────────────────────────────
    public static final Font FONT_TITLE    = new Font("Segoe UI", Font.BOLD,  22);
    public static final Font FONT_SUBTITLE = new Font("Segoe UI", Font.BOLD,  14);
    public static final Font FONT_BODY     = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FONT_SMALL    = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FONT_BUTTON   = new Font("Segoe UI", Font.BOLD,  13);

    // ── Sizes ─────────────────────────────────────────────────────────────────
    public static final int  BUTTON_HEIGHT = 36;
    public static final Dimension BUTTON_SIZE = new Dimension(160, BUTTON_HEIGHT);

    // ── Padding ───────────────────────────────────────────────────────────────
    public static final int PAD = 12;
}
