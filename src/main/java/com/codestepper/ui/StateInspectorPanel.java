package com.codestepper.ui;

import com.codestepper.interpreter.Value;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Vector;

public class StateInspectorPanel extends JPanel {

    private JTable variablesTable;
    private DefaultTableModel variablesModel;
    private Map<String, String> previousVariableValues = new HashMap<>();

    private JList<String> callStackList;
    private DefaultListModel<String> callStackModel;

    private JTextArea consoleArea;

    public StateInspectorPanel() {
        setLayout(new GridLayout(3, 1, 0, 5));
        setBackground(Theme.BORDER);
        setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        // 1. Variable Watcher
        JPanel varsPanel = createSectionPanel("Variable Watcher");
        variablesModel = new DefaultTableModel(new String[]{"Name", "Type", "Value", "Changed"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        variablesTable = new JTable(variablesModel);
        Theme.styleTable(variablesTable);
        variablesTable.setFont(Theme.FONT_CODE);
        
        // Custom renderer to highlight changed rows
        variablesTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                String changed = (String) table.getValueAt(row, 3);
                if ("âœ“".equals(changed)) {
                    c.setBackground(Theme.HIGHLIGHT_CURRENT_LINE_BG);
                    c.setForeground(Theme.HIGHLIGHT_GUTTER_ARROW); // yellow text
                } else {
                    c.setBackground(Theme.BG_PANEL);
                    c.setForeground(Theme.TEXT_NORMAL);
                }
                return c;
            }
        });
        
        varsPanel.add(new JScrollPane(variablesTable), BorderLayout.CENTER);
        add(varsPanel);

        // 2. Call Stack
        JPanel stackPanel = createSectionPanel("Call Stack");
        callStackModel = new DefaultListModel<>();
        callStackList = new JList<>(callStackModel);
        callStackList.setBackground(Theme.BG_PANEL);
        callStackList.setForeground(Theme.TEXT_NORMAL);
        callStackList.setFont(Theme.FONT_CODE);
        stackPanel.add(new JScrollPane(callStackList), BorderLayout.CENTER);
        add(stackPanel);

        // 3. Execution Log
        JPanel consolePanel = createSectionPanel("Execution Log");
        consoleArea = new JTextArea();
        consoleArea.setBackground(Theme.BG_PANEL);
        consoleArea.setForeground(Theme.TEXT_NORMAL);
        consoleArea.setFont(Theme.FONT_CODE);
        consoleArea.setEditable(false);
        consolePanel.add(new JScrollPane(consoleArea), BorderLayout.CENTER);
        add(consolePanel);
    }

    private JPanel createSectionPanel(String title) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(Theme.BG_MAIN);
        JLabel titleLabel = new JLabel(title);
        titleLabel.setForeground(Theme.ACCENT_BLUE);
        titleLabel.setFont(Theme.FONT_UI_BOLD);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        panel.add(titleLabel, BorderLayout.NORTH);
        return panel;
    }

    public void updateState(Map<String, Value> currentVariables, List<String> callStack, String consoleOutput) {
        // Update Variables
        variablesModel.setRowCount(0);
        Map<String, String> newPrevMap = new HashMap<>();
        
        for (Map.Entry<String, Value> entry : currentVariables.entrySet()) {
            String name = entry.getKey();
            Value val = entry.getValue();
            String type = val.getType();
            String valueStr = val.toString();
            
            boolean changed = false;
            if (previousVariableValues.containsKey(name)) {
                if (!previousVariableValues.get(name).equals(valueStr)) {
                    changed = true;
                }
            } else {
                changed = true; // new variable
            }
            
            newPrevMap.put(name, valueStr);
            variablesModel.addRow(new Object[]{name, type, valueStr, changed ? "âœ“" : ""});
        }
        previousVariableValues = newPrevMap;

        // Update Call Stack
        callStackModel.clear();
        for (String frame : callStack) {
            callStackModel.addElement(frame);
        }

        // Update Console
        consoleArea.setText(consoleOutput);
        consoleArea.setCaretPosition(consoleArea.getDocument().getLength()); // scroll to bottom
    }
    
    public void reset() {
        variablesModel.setRowCount(0);
        previousVariableValues.clear();
        callStackModel.clear();
        consoleArea.setText("");
    }
}
