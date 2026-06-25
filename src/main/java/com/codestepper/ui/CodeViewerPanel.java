package com.codestepper.ui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeViewerPanel extends JPanel {

    private JTextPane textPane;
    private GutterPanel gutter;
    private int currentLine = -1;
    
    public CodeViewerPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_MAIN);
        
        textPane = new JTextPane();
        textPane.setFont(Theme.FONT_CODE);
        textPane.setBackground(Theme.BG_MAIN);
        textPane.setForeground(Theme.TEXT_NORMAL);
        textPane.setCaretColor(Color.WHITE);
        textPane.setEditable(false); // only editable in the paste modal
        
        // Remove margin
        textPane.setMargin(new Insets(0, 5, 0, 0));

        JScrollPane scrollPane = new JScrollPane(textPane);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        gutter = new GutterPanel(textPane);
        scrollPane.setRowHeaderView(gutter);
        
        add(scrollPane, BorderLayout.CENTER);
    }
    
    public void setCode(String code) {
        textPane.setText(code);
        applySyntaxHighlighting();
        gutter.updatePreferredSize();
        repaint();
    }
    
    public String getCode() {
        return textPane.getText();
    }
    
    public void setCurrentLine(int line) {
        this.currentLine = line;
        highlightCurrentLine();
        gutter.setCurrentLine(line);
        gutter.repaint();
        
        // scroll to visible
        if (line > 0) {
            try {
                int y = textPane.modelToView(textPane.getDocument().getDefaultRootElement().getElement(line - 1).getStartOffset()).y;
                JViewport viewport = (JViewport) textPane.getParent();
                Rectangle viewRect = viewport.getViewRect();
                if (y < viewRect.y || y > viewRect.y + viewRect.height) {
                    viewport.setViewPosition(new Point(0, Math.max(0, y - viewRect.height / 2)));
                }
            } catch (BadLocationException e) {
                // ignore
            }
        }
    }
    
    private void highlightCurrentLine() {
        textPane.getHighlighter().removeAllHighlights();
        if (currentLine > 0) {
            try {
                Element root = textPane.getDocument().getDefaultRootElement();
                int start = root.getElement(currentLine - 1).getStartOffset();
                int end = root.getElement(currentLine - 1).getEndOffset();
                
                textPane.getHighlighter().addHighlight(start, end, new DefaultHighlighter.DefaultHighlightPainter(Theme.HIGHLIGHT_CURRENT_LINE_BG));
            } catch (BadLocationException e) {
                // ignore
            }
        }
    }
    
    private void applySyntaxHighlighting() {
        String text = textPane.getText();
        StyledDocument doc = textPane.getStyledDocument();
        
        // Clear old styles
        SimpleAttributeSet defaultAttr = new SimpleAttributeSet();
        StyleConstants.setForeground(defaultAttr, Theme.TEXT_NORMAL);
        doc.setCharacterAttributes(0, text.length(), defaultAttr, true);
        
        // Patterns
        String keywords = "\\b(class|public|static|void|int|double|boolean|char|float|long|short|byte|String|for|while|do|if|else|switch|case|default|return|new|true|false|null|import|package)\\b";
        String stringLiteral = "\"[^\"]*\"";
        String numbers = "\\b\\d+(\\.\\d+)?\\b";
        String comments = "//.*|/\\*[\\s\\S]*?\\*/";
        
        applyRegexStyle(doc, text, keywords, Theme.SYNTAX_KEYWORD);
        applyRegexStyle(doc, text, stringLiteral, Theme.SYNTAX_STRING);
        applyRegexStyle(doc, text, numbers, Theme.SYNTAX_NUMBER);
        applyRegexStyle(doc, text, comments, Theme.SYNTAX_COMMENT);
    }
    
    private void applyRegexStyle(StyledDocument doc, String text, String regex, Color color) {
        SimpleAttributeSet attr = new SimpleAttributeSet();
        StyleConstants.setForeground(attr, color);
        if (color == Theme.SYNTAX_COMMENT) {
            StyleConstants.setItalic(attr, true);
        }
        
        Matcher m = Pattern.compile(regex).matcher(text);
        while (m.find()) {
            doc.setCharacterAttributes(m.start(), m.end() - m.start(), attr, false);
        }
    }

    private class GutterPanel extends JPanel {
        private JTextPane textPane;
        private int currentLine = -1;
        
        public GutterPanel(JTextPane textPane) {
            this.textPane = textPane;
            setBackground(Theme.BG_PANEL);
            setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Theme.BORDER));
        }
        
        public void setCurrentLine(int line) {
            this.currentLine = line;
        }
        
        public void updatePreferredSize() {
            int lines = textPane.getDocument().getDefaultRootElement().getElementCount();
            FontMetrics fm = getFontMetrics(Theme.FONT_CODE);
            int width = fm.stringWidth(String.valueOf(lines)) + 30; // padding for arrow
            setPreferredSize(new Dimension(width, 0));
            revalidate();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            
            g2.setFont(Theme.FONT_CODE);
            FontMetrics fm = g2.getFontMetrics();
            int fontHeight = fm.getHeight();
            int fontAscent = fm.getAscent();
            
            Element root = textPane.getDocument().getDefaultRootElement();
            int numLines = root.getElementCount();
            
            Rectangle clip = g.getClipBounds();
            int startLine = root.getElementIndex(textPane.viewToModel(new Point(0, clip.y)));
            int endLine = root.getElementIndex(textPane.viewToModel(new Point(0, clip.y + clip.height))) + 1;
            
            for (int i = startLine; i < Math.min(endLine, numLines); i++) {
                try {
                    int y = textPane.modelToView(root.getElement(i).getStartOffset()).y;
                    int lineNum = i + 1;
                    
                    if (lineNum == currentLine) {
                        // draw background highlight block and arrow
                        g2.setColor(Theme.HIGHLIGHT_CURRENT_LINE_BG);
                        g2.fillRect(0, y, getWidth(), fontHeight);
                        
                        g2.setColor(Theme.HIGHLIGHT_GUTTER_ARROW);
                        g2.drawString("➤", 5, y + fontAscent);
                        
                        g2.setColor(Color.WHITE);
                    } else {
                        g2.setColor(Theme.TEXT_DIMMED);
                    }
                    
                    String lineStr = String.valueOf(lineNum);
                    int strWidth = fm.stringWidth(lineStr);
                    g2.drawString(lineStr, getWidth() - strWidth - 5, y + fontAscent);
                    
                } catch (BadLocationException e) {
                    // ignore
                }
            }
        }
    }
}
