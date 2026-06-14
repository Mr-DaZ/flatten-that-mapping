package com.dantsvyetkov.flattener;

import java.util.LinkedHashMap;
import java.util.Map;

public final class MappingFlattener {

    private MappingFlattener() {}

    public static Map<String, Object> flattenedProperties(Map<String, Object> properties, FlattenMode mode) {
        Map<String, Object> out = new LinkedHashMap<>();
        flatten(properties, "", out, mode.includesInteriorNodes());
        return out;
    }

    public static void flattenInPlace(Map<String, Object> root, FlattenMode mode) {
        boolean includeInterior = mode.includesInteriorNodes();
        for (Map.Entry<String, Object> indexEntry : root.entrySet()) {
            if (!(indexEntry.getValue() instanceof Map<?, ?> indexRaw)) continue;
            Map<String, Object> indexMap = stringKeyed(indexRaw);
            Object mappingsObj = indexMap.get(Names.FIELD_MAPPINGS);
            if (mappingsObj instanceof Map<?, ?> mappingsRaw) {
                Map<String, Object> mappings = stringKeyed(mappingsRaw);
                Object propsObj = mappings.get(Names.FIELD_PROPERTIES);
                if (propsObj instanceof Map<?, ?> propsRaw) {
                    Map<String, Object> flat = new LinkedHashMap<>();
                    flatten(stringKeyed(propsRaw), "", flat, includeInterior);
                    mappings.put(Names.FIELD_PROPERTIES, flat);
                }
                indexMap.put(Names.FIELD_MAPPINGS, mappings);
            }
            indexEntry.setValue(indexMap);
        }
    }

    private static void flatten(Map<String, Object> properties, String prefix, Map<String, Object> out, boolean includeInterior) {
        for (Map.Entry<String, Object> e : properties.entrySet()) {
            String fullName = prefix + e.getKey();
            Object val = e.getValue();
            if (!(val instanceof Map<?, ?> valRaw)) {
                out.put(fullName, val);
                continue;
            }
            Map<String, Object> field = stringKeyed(valRaw);
            Object subProps = field.get(Names.FIELD_PROPERTIES);
            if (subProps instanceof Map<?, ?> subPropsRaw) {
                if (includeInterior) {
                    Map<String, Object> wrapper = new LinkedHashMap<>(field);
                    wrapper.remove(Names.FIELD_PROPERTIES);
                    wrapper.putIfAbsent(Names.FIELD_TYPE, Names.TYPE_OBJECT);
                    out.put(fullName, wrapper);
                }
                flatten(stringKeyed(subPropsRaw), fullName + ".", out, includeInterior);
            } else {
                out.put(fullName, field);
            }
        }
    }

    private static Map<String, Object> stringKeyed(Map<?, ?> raw) {
        Map<String, Object> typed = new LinkedHashMap<>(raw.size());
        for (Map.Entry<?, ?> e : raw.entrySet()) {
            if (!(e.getKey() instanceof String key)) {
                throw new IllegalStateException(
                    "Expected String map key, got " + (e.getKey() == null ? "null" : e.getKey().getClass().getName()));
            }
            typed.put(key, e.getValue());
        }
        return typed;
    }
}
