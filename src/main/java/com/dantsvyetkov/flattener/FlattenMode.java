package com.dantsvyetkov.flattener;

import java.util.Locale;

public enum FlattenMode {
    LEAVES,
    DOTTED;

    public static final FlattenMode DEFAULT = LEAVES;

    public static FlattenMode fromParam(String value) {
        if (value == null || value.isEmpty()) {
            return DEFAULT;
        }
        return switch (value.toLowerCase(Locale.ROOT)) {
            case "leaves" -> LEAVES;
            case "dotted" -> DOTTED;
            default -> throw new IllegalArgumentException(
                "Unknown " + Names.PARAM_FLATTEN + " value [" + value + "], expected one of [leaves, dotted]");
        };
    }

    public boolean includesInteriorNodes() {
        return this == DOTTED;
    }
}
