package com.codestepper.interpreter;

import java.util.List;
import java.util.Map;

/**
 * Represents a single execution step snapshot.
 */
public class ExecutionStep {
    public enum HighlightType {
        CURRENT, BRANCH_TAKEN, ERROR
    }

    private final int lineNumber;
    private final String description;
    private final Map<String, Value> variableSnapshot;
    private final List<String> callStackSnapshot;
    private final String consoleOutput;
    private final HighlightType highlightType;
    private final boolean isTerminalError;

    public ExecutionStep(int lineNumber, String description, Map<String, Value> variableSnapshot,
                         List<String> callStackSnapshot, String consoleOutput, HighlightType highlightType,
                         boolean isTerminalError) {
        this.lineNumber = lineNumber;
        this.description = description;
        this.variableSnapshot = variableSnapshot;
        this.callStackSnapshot = callStackSnapshot;
        this.consoleOutput = consoleOutput;
        this.highlightType = highlightType;
        this.isTerminalError = isTerminalError;
    }

    public int getLineNumber() {
        return lineNumber;
    }

    public String getDescription() {
        return description;
    }

    public Map<String, Value> getVariableSnapshot() {
        return variableSnapshot;
    }

    public List<String> getCallStackSnapshot() {
        return callStackSnapshot;
    }

    public String getConsoleOutput() {
        return consoleOutput;
    }

    public HighlightType getHighlightType() {
        return highlightType;
    }

    public boolean isTerminalError() {
        return isTerminalError;
    }
}
