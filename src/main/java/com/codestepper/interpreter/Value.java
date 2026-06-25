package com.codestepper.interpreter;

import java.util.Objects;

/**
 * Represents a value in the interpreter (primitive, array, object, or null).
 */
public class Value {
    public static final Value NULL = new Value("null", null);

    private final String type;
    private final Object underlying;

    public Value(String type, Object underlying) {
        this.type = type;
        this.underlying = underlying;
    }

    public String getType() {
        return type;
    }

    public Object getUnderlying() {
        return underlying;
    }

    public boolean isNull() {
        return this == NULL || underlying == null;
    }

    public boolean isNumber() {
        return underlying instanceof Number;
    }

    public boolean isBoolean() {
        return underlying instanceof Boolean;
    }

    public boolean isString() {
        return underlying instanceof String;
    }

    public Number asNumber() {
        return (Number) underlying;
    }

    public Boolean asBoolean() {
        return (Boolean) underlying;
    }

    public String asString() {
        return (String) underlying;
    }

    @Override
    public String toString() {
        if (isNull()) return "null";
        if (type.equals("String")) return "\"" + underlying.toString() + "\"";
        if (type.equals("char")) return "'" + underlying.toString() + "'";
        if (underlying instanceof Object[]) {
            Object[] arr = (Object[]) underlying;
            StringBuilder sb = new StringBuilder("[");
            for (int i = 0; i < arr.length; i++) {
                if (i > 0) sb.append(", ");
                sb.append(arr[i] != null ? arr[i].toString() : "null");
            }
            sb.append("]");
            return sb.toString();
        }
        if (type.contains("[]") && underlying != null) {
             // simplified array display for primitives
            if (underlying instanceof int[]) {
                int[] arr = (int[]) underlying;
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < arr.length; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(arr[i]);
                }
                sb.append("]");
                return sb.toString();
            }
            if (underlying instanceof double[]) {
                double[] arr = (double[]) underlying;
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < arr.length; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(arr[i]);
                }
                sb.append("]");
                return sb.toString();
            }
            if (underlying instanceof boolean[]) {
                boolean[] arr = (boolean[]) underlying;
                StringBuilder sb = new StringBuilder("[");
                for (int i = 0; i < arr.length; i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(arr[i]);
                }
                sb.append("]");
                return sb.toString();
            }
        }
        return underlying.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Value value = (Value) o;
        return Objects.equals(underlying, value.underlying);
    }

    @Override
    public int hashCode() {
        return Objects.hash(underlying);
    }
}
