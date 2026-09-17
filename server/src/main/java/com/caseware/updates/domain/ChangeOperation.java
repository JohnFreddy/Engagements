package com.caseware.updates.domain;

import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * A single entry in a raw template diff.
 *
 * <p>The supplied diffs are not RFC 6902: {@code add} carries {@code value}, {@code replace}
 * carries {@code oldValue}/{@code newValue}, {@code remove} carries {@code oldValue}. This record
 * normalises all three onto the same two slots so they can be folded uniformly.
 *
 * <p>{@code value} is modelled as {@code Object} (scalar, {@code Map}, or {@code List}) rather than
 * a bound JSON type so the domain stays free of a parser dependency.
 */
public record ChangeOperation(Op op, String path, Object oldValue, Object newValue) {

    public enum Op { ADD, REPLACE, REMOVE }

    public ChangeOperation {
        Objects.requireNonNull(op, "op");
        Objects.requireNonNull(path, "path");
    }

    public static ChangeOperation add(String path, Object value) {
        return new ChangeOperation(Op.ADD, path, null, value);
    }

    public static ChangeOperation replace(String path, Object oldValue, Object newValue) {
        return new ChangeOperation(Op.REPLACE, path, oldValue, newValue);
    }

    public static ChangeOperation remove(String path, Object oldValue) {
        return new ChangeOperation(Op.REMOVE, path, oldValue, null);
    }

    /** Last path segment, e.g. {@code tolerance} for {@code /sections/analytics/.../tolerance}. */
    public String leaf() {
        return path.substring(path.lastIndexOf('/') + 1);
    }

    /**
     * Section key for paths of the form {@code /sections/<key>/...}.
     * Empty for template-level paths such as {@code /metadata/displayName}.
     */
    public Optional<String> sectionKey() {
        String[] parts = path.split("/");
        boolean isSectionPath = parts.length >= 3 && "sections".equals(parts[1]);
        return isSectionPath ? Optional.of(parts[2]) : Optional.empty();
    }

    /**
     * Author-supplied label on the changed node, when the value is an object.
     * Preferred over the path leaf because it is already written for practitioners.
     */
    public Optional<String> label() {
        Object value = newValue != null ? newValue : oldValue;
        if (value instanceof Map<?, ?> map && map.get("label") instanceof String label) {
            return Optional.of(label);
        }
        return Optional.empty();
    }
}
