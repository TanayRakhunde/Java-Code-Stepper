package com.codestepper.interpreter;

import java.util.HashMap;
import java.util.Map;

/**
 * Represents a lexical scope in the interpreter.
 */
public class Environment {
    private final Environment enclosing;
    private final Map<String, Value> values = new HashMap<>();

    public Environment() {
        this.enclosing = null;
    }

    public Environment(Environment enclosing) {
        this.enclosing = enclosing;
    }

    public void define(String name, Value value) {
        values.put(name, value);
    }

    public void assign(String name, Value value) {
        if (values.containsKey(name)) {
            values.put(name, value);
            return;
        }

        if (enclosing != null) {
            enclosing.assign(name, value);
            return;
        }

        throw new RuntimeException("Undefined variable '" + name + "'.");
    }

    public Value get(String name) {
        if (values.containsKey(name)) {
            return values.get(name);
        }

        if (enclosing != null) {
            return enclosing.get(name);
        }

        throw new RuntimeException("Undefined variable '" + name + "'.");
    }

    public boolean isDefined(String name) {
        if (values.containsKey(name)) return true;
        if (enclosing != null) return enclosing.isDefined(name);
        return false;
    }

    /**
     * Gets a snapshot of all currently visible variables.
     */
    public Map<String, Value> getSnapshot() {
        Map<String, Value> snapshot = new HashMap<>();
        if (enclosing != null) {
            snapshot.putAll(enclosing.getSnapshot());
        }
        snapshot.putAll(values);
        return snapshot;
    }
}
