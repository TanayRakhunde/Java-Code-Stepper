package com.codestepper.ui;

import com.codestepper.interpreter.ExecutionStep;
import com.codestepper.interpreter.ExecutionTrace;
import com.codestepper.interpreter.JavaASTInterpreter;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class MainFrame extends JFrame {

    private CodeViewerPanel codeViewer;
    private StateInspectorPanel inspector;
    private ControlBarPanel controlBar;

    private ExecutionTrace currentTrace;
    private int currentStepIndex = -1;
    private Timer autoStepTimer;

    public MainFrame() {
        setTitle("Java Code Stepper");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 800);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Theme.BG_MAIN);
        
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // ignore
        }

        // Layout
        setLayout(new BorderLayout());

        // Split Pane for Code and Inspector
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setDividerLocation(0.6); // 60% left, 40% right
        splitPane.setResizeWeight(0.6);
        splitPane.setBorder(BorderFactory.createEmptyBorder());
        splitPane.setBackground(Theme.BG_MAIN);

        codeViewer = new CodeViewerPanel();
        inspector = new StateInspectorPanel();
        controlBar = new ControlBarPanel();

        splitPane.setLeftComponent(codeViewer);
        splitPane.setRightComponent(inspector);

        add(splitPane, BorderLayout.CENTER);
        add(controlBar, BorderLayout.SOUTH);

        setupListeners();
        
        // Initial setup
        controlBar.updateStepInfo(0, 0, "Paste code to begin");
        
        // load default example
        String defaultCode = CodeExamples.EXAMPLES.get("1. Hello World");
        codeViewer.setCode(defaultCode);
    }

    private void setupListeners() {
        controlBar.getLoadButton().addActionListener(e -> showLoadModal());
        
        controlBar.getStartButton().addActionListener(e -> startExecution());
        
        controlBar.getPrevButton().addActionListener(e -> stepBackward());
        
        controlBar.getNextButton().addActionListener(e -> stepForward());
        
        controlBar.getResetButton().addActionListener(e -> reset());
        
        controlBar.getRunToEndButton().addActionListener(e -> {
            if (currentTrace != null) {
                jumpToStep(currentTrace.size() - 1);
            }
        });
        
        controlBar.getPauseButton().addActionListener(e -> {
            controlBar.getAutoStepCheckBox().setSelected(false);
            if (autoStepTimer != null) autoStepTimer.stop();
        });

        controlBar.getAutoStepCheckBox().addActionListener(e -> {
            if (controlBar.getAutoStepCheckBox().isSelected()) {
                startAutoStep();
            } else {
                if (autoStepTimer != null) autoStepTimer.stop();
            }
        });
        
        controlBar.getSpeedSlider().addChangeListener(e -> {
            if (autoStepTimer != null) {
                autoStepTimer.setDelay(controlBar.getSpeedSlider().getValue());
            }
        });
    }
    
    private void startAutoStep() {
        if (currentTrace == null || currentStepIndex >= currentTrace.size() - 1) return;
        
        if (autoStepTimer != null) autoStepTimer.stop();
        autoStepTimer = new Timer(controlBar.getSpeedSlider().getValue(), e -> {
            if (!stepForward()) {
                ((Timer)e.getSource()).stop();
                controlBar.getAutoStepCheckBox().setSelected(false);
            }
        });
        autoStepTimer.start();
    }

    private void showLoadModal() {
        JDialog dialog = new JDialog(this, "Paste / Load Code", true);
        dialog.setSize(800, 600);
        dialog.setLocationRelativeTo(this);
        dialog.setLayout(new BorderLayout(10, 10));
        dialog.getContentPane().setBackground(Theme.BG_MAIN);
        
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setBackground(Theme.BG_MAIN);
        JLabel lbl = new JLabel("Mode: ");
        lbl.setForeground(Theme.TEXT_NORMAL);
        String[] modes = {"Full Class", "Main Method Only", "Code Snippet"};
        JComboBox<String> modeCombo = new JComboBox<>(modes);
        
        JLabel exampleLbl = new JLabel("  Or load example: ");
        exampleLbl.setForeground(Theme.TEXT_NORMAL);
        JComboBox<String> exampleCombo = new JComboBox<>(CodeExamples.EXAMPLES.keySet().toArray(new String[0]));
        exampleCombo.insertItemAt("-- Select Example --", 0);
        exampleCombo.setSelectedIndex(0);
        
        topPanel.add(lbl);
        topPanel.add(modeCombo);
        topPanel.add(exampleLbl);
        topPanel.add(exampleCombo);
        
        JTextArea textArea = new JTextArea(codeViewer.getCode());
        textArea.setFont(Theme.FONT_CODE);
        textArea.setBackground(Theme.BG_PANEL);
        textArea.setForeground(Theme.TEXT_NORMAL);
        textArea.setCaretColor(Color.WHITE);
        JScrollPane scroll = new JScrollPane(textArea);
        scroll.setBorder(BorderFactory.createLineBorder(Theme.BORDER));
        
        exampleCombo.addActionListener(e -> {
            if (exampleCombo.getSelectedIndex() > 0) {
                String exName = (String) exampleCombo.getSelectedItem();
                textArea.setText(CodeExamples.EXAMPLES.get(exName));
                modeCombo.setSelectedItem("Full Class");
            }
        });
        
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottomPanel.setBackground(Theme.BG_MAIN);
        JButton loadBtn = new JButton("Analyze & Load");
        loadBtn.setBackground(Theme.ACCENT_BLUE);
        loadBtn.setForeground(Color.WHITE);
        loadBtn.addActionListener(e -> {
            codeViewer.setCode(textArea.getText());
            reset();
            // Pre-parse to check syntax and build trace
            buildTrace((String) modeCombo.getSelectedItem());
            dialog.dispose();
        });
        bottomPanel.add(loadBtn);
        
        dialog.add(topPanel, BorderLayout.NORTH);
        dialog.add(scroll, BorderLayout.CENTER);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    private void buildTrace(String mode) {
        String code = codeViewer.getCode();
        JavaASTInterpreter interpreter = new JavaASTInterpreter();
        try {
            currentTrace = interpreter.parseAndExecute(code, mode);
            controlBar.setControlsEnabled(true);
            currentStepIndex = -1;
            controlBar.updateStepInfo(0, currentTrace.size(), "Ready to step");
            inspector.reset();
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error parsing or executing code:\n" + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            currentTrace = null;
            controlBar.setControlsEnabled(false);
        }
    }

    private void startExecution() {
        if (currentTrace == null) {
            buildTrace("Full Class"); // try default
        }
        if (currentTrace != null && currentTrace.size() > 0) {
            jumpToStep(0);
        }
    }

    private boolean stepForward() {
        if (currentTrace != null && currentStepIndex < currentTrace.size() - 1) {
            jumpToStep(currentStepIndex + 1);
            return true;
        }
        return false;
    }

    private void stepBackward() {
        if (currentTrace != null && currentStepIndex > 0) {
            jumpToStep(currentStepIndex - 1);
        }
    }
    
    private void jumpToStep(int index) {
        if (currentTrace == null || index < 0 || index >= currentTrace.size()) return;
        
        currentStepIndex = index;
        ExecutionStep step = currentTrace.getStep(index);
        
        codeViewer.setCurrentLine(step.getLineNumber());
        inspector.updateState(step.getVariableSnapshot(), step.getCallStackSnapshot(), step.getConsoleOutput());
        controlBar.updateStepInfo(index + 1, currentTrace.size(), step.getDescription());
        
        if (step.isTerminalError()) {
            controlBar.getAutoStepCheckBox().setSelected(false);
            if (autoStepTimer != null) autoStepTimer.stop();
        }
    }

    private void reset() {
        if (autoStepTimer != null) autoStepTimer.stop();
        controlBar.getAutoStepCheckBox().setSelected(false);
        currentStepIndex = -1;
        codeViewer.setCurrentLine(-1);
        inspector.reset();
        if (currentTrace != null) {
            controlBar.updateStepInfo(0, currentTrace.size(), "Ready to step");
        } else {
            controlBar.updateStepInfo(0, 0, "No trace loaded");
        }
    }
}
