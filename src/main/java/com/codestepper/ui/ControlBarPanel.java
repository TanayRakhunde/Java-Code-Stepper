package com.codestepper.ui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class ControlBarPanel extends JPanel {

    private JButton loadButton;
    private JButton startButton;
    private JButton prevButton;
    private JButton nextButton;
    private JButton runToEndButton;
    private JButton pauseButton;
    private JButton resetButton;

    private JLabel stepLabel;
    private JLabel descLabel;
    private JSlider speedSlider;
    private JCheckBox autoStepCheckBox;

    public ControlBarPanel() {
        setLayout(new BorderLayout());
        setBackground(Theme.BG_PANEL);
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setBackground(Theme.BG_PANEL);
        
        loadButton = createPrimaryButton("Paste / Load Code");
        leftPanel.add(loadButton);

        JPanel centerPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        centerPanel.setBackground(Theme.BG_PANEL);
        
        startButton = createPrimaryButton("â–¶ Start");
        prevButton = createButton("â—€ Prev Step");
        nextButton = createButton("Next Step â–¶");
        runToEndButton = createButton("â© Run to End");
        pauseButton = createButton("â¸ Pause");
        resetButton = createButton("â†º Reset");
        
        centerPanel.add(startButton);
        centerPanel.add(prevButton);
        centerPanel.add(nextButton);
        centerPanel.add(runToEndButton);
        centerPanel.add(pauseButton);
        centerPanel.add(resetButton);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(Theme.BG_PANEL);
        
        stepLabel = new JLabel("Step 0 of 0");
        stepLabel.setForeground(Theme.TEXT_NORMAL);
        stepLabel.setFont(Theme.FONT_UI_BOLD);
        
        descLabel = new JLabel(" Ready");
        descLabel.setForeground(Theme.TEXT_DIMMED);
        descLabel.setFont(Theme.FONT_UI);
        
        autoStepCheckBox = new JCheckBox("Auto-step");
        autoStepCheckBox.setBackground(Theme.BG_PANEL);
        autoStepCheckBox.setForeground(Theme.TEXT_NORMAL);
        autoStepCheckBox.setFont(Theme.FONT_UI);
        
        speedSlider = new JSlider(JSlider.HORIZONTAL, 10, 1000, 500); // ms delay, inverted conceptually
        speedSlider.setInverted(true); // Left=Slow (1000ms), Right=Fast (10ms)
        speedSlider.setBackground(Theme.BG_PANEL);
        speedSlider.setPreferredSize(new Dimension(100, 20));
        
        rightPanel.add(stepLabel);
        rightPanel.add(descLabel);
        rightPanel.add(new JLabel("  |  "));
        rightPanel.add(autoStepCheckBox);
        rightPanel.add(new JLabel("Slow"));
        rightPanel.add(speedSlider);
        rightPanel.add(new JLabel("Fast"));

        add(leftPanel, BorderLayout.WEST);
        add(centerPanel, BorderLayout.CENTER);
        add(rightPanel, BorderLayout.EAST);
        
        setControlsEnabled(false);
        loadButton.setEnabled(true);
    }

    private JButton createButton(String text) {
        JButton btn = new JButton(text);
        Theme.styleSecondaryButton(btn);
        return btn;
    }
    
    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text);
        Theme.stylePrimaryButton(btn);
        return btn;
    }

    public void setControlsEnabled(boolean running) {
        startButton.setEnabled(!running);
        prevButton.setEnabled(running);
        nextButton.setEnabled(running);
        runToEndButton.setEnabled(running);
        pauseButton.setEnabled(running);
        resetButton.setEnabled(running);
    }

    public void updateStepInfo(int current, int total, String description) {
        stepLabel.setText("Step " + current + " of " + total);
        descLabel.setText("  " + description);
    }

    // Getters for adding listeners
    public JButton getLoadButton() { return loadButton; }
    public JButton getStartButton() { return startButton; }
    public JButton getPrevButton() { return prevButton; }
    public JButton getNextButton() { return nextButton; }
    public JButton getRunToEndButton() { return runToEndButton; }
    public JButton getPauseButton() { return pauseButton; }
    public JButton getResetButton() { return resetButton; }
    public JCheckBox getAutoStepCheckBox() { return autoStepCheckBox; }
    public JSlider getSpeedSlider() { return speedSlider; }
}
