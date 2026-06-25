package com.codestepper.interpreter;

import java.util.ArrayList;
import java.util.List;

/**
 * Holds the ordered list of execution steps.
 */
public class ExecutionTrace {
    private final List<ExecutionStep> steps = new ArrayList<>();
    
    public void addStep(ExecutionStep step) {
        steps.add(step);
    }
    
    public List<ExecutionStep> getSteps() {
        return steps;
    }
    
    public int size() {
        return steps.size();
    }
    
    public ExecutionStep getStep(int index) {
        if (index >= 0 && index < steps.size()) {
            return steps.get(index);
        }
        return null;
    }
}
