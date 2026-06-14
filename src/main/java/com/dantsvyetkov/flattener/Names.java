package com.dantsvyetkov.flattener;

public final class Names {

    private Names() {}

    public static final String PARAM_FLATTEN = "flatten";
    public static final String PARAM_INDEX = "index";
    public static final String ALL_INDICES = "_all";

    public static final String FIELD_MAPPINGS = "mappings";
    public static final String FIELD_PROPERTIES = "properties";
    public static final String FIELD_TYPE = "type";
    public static final String TYPE_OBJECT = "object";

    public static final String ROUTE_FLATTEN_ALL_MAPPINGS = "/_flatten/_mapping";
    public static final String ROUTE_FLATTEN_INDEX_MAPPING = "/_flatten/{index}/_mapping";
    public static final String ROUTE_FLATTEN_INDEX = "/_flatten/{index}";

    public static final String HANDLER_FLATTEN_MAPPING = "flatten_mapping_action";
    public static final String HANDLER_FLATTEN_INDEX = "flatten_index_action";
}
