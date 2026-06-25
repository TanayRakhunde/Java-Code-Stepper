package com.codestepper.ui;

import java.awt.Color;
import java.awt.Font;

public class Theme {
    // Colors from requirements
    public static final Color BG_MAIN = new Color(0x1E1E2E);
    public static final Color BG_PANEL = new Color(0x181825);
    public static final Color BORDER = new Color(0x313244);
    
    public static final Color ACCENT_BLUE = new Color(0x61AFEF);
    public static final Color TEXT_NORMAL = new Color(0xCDD6F4);
    public static final Color TEXT_DIMMED = new Color(0x808080); // roughly 50% opacity equivalent
    
    // Syntax highlighting colors
    public static final Color SYNTAX_KEYWORD = new Color(0xC678DD);
    public static final Color SYNTAX_STRING = new Color(0x98C379);
    public static final Color SYNTAX_NUMBER = new Color(0xD19A66);
    public static final Color SYNTAX_COMMENT = new Color(0x5C6370);
    public static final Color SYNTAX_METHOD = new Color(0x61AFEF);
    public static final Color SYNTAX_VARIABLE = new Color(0xE06C75);
    public static final Color SYNTAX_OPERATOR = new Color(0x56B6C2);
    
    public static final Color HIGHLIGHT_CURRENT_LINE_BG = new Color(0x3E4451);
    public static final Color HIGHLIGHT_GUTTER_ARROW = new Color(0xE5C07B);
    
    // Buttons
    public static final Color BUTTON_PRIMARY_BG = new Color(0x61AFEF); // Vibrant blue
    public static final Color BUTTON_PRIMARY_FG = new Color(0x1E1E2E); // Dark text for contrast
    public static final Color BUTTON_SECONDARY_BG = new Color(0x3E4451); // Darker blue-grey
    public static final Color BUTTON_SECONDARY_FG = new Color(0xCDD6F4); // Light text

    // Fonts
    public static final Font FONT_CODE = new Font(Font.MONOSPACED, Font.PLAIN, 14);
    public static final Font FONT_UI = new Font(Font.SANS_SERIF, Font.PLAIN, 13);
    public static final Font FONT_UI_BOLD = new Font(Font.SANS_SERIF, Font.BOLD, 13);

    public static void stylePrimaryButton(javax.swing.JButton button) {
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setBackground(BUTTON_PRIMARY_BG);
        button.setForeground(BUTTON_PRIMARY_FG);
        button.setFocusPainted(false);
        button.setFont(FONT_UI_BOLD);
        button.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        button.setOpaque(true);
    }

    public static void styleSecondaryButton(javax.swing.JButton button) {
        button.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        button.setBackground(BUTTON_SECONDARY_BG);
        button.setForeground(BUTTON_SECONDARY_FG);
        button.setFocusPainted(false);
        button.setFont(FONT_UI_BOLD);
        button.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 16, 8, 16));
        button.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));
        button.setOpaque(true);
    }

    public static void styleTable(javax.swing.JTable table) {
        table.setBackground(BG_PANEL);
        table.setForeground(TEXT_NORMAL);
        table.setGridColor(BORDER);
        table.setRowHeight(24);
        table.getTableHeader().setBackground(BUTTON_SECONDARY_BG);
        table.getTableHeader().setForeground(TEXT_NORMAL);
        table.getTableHeader().setFont(FONT_UI_BOLD);
        table.getTableHeader().setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, BORDER));
        table.setSelectionBackground(HIGHLIGHT_CURRENT_LINE_BG);
        table.setSelectionForeground(TEXT_NORMAL);
    }
}
