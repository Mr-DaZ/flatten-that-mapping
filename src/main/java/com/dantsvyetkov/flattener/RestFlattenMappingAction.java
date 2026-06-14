package com.dantsvyetkov.flattener;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsAction;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.client.internal.node.NodeClient;
import org.elasticsearch.cluster.metadata.MappingMetadata;
import org.elasticsearch.common.Strings;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.rest.RestUtils;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.json.JsonXContent;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.rest.RestRequest.Method.GET;

public class RestFlattenMappingAction extends BaseRestHandler {

    @Override
    public String getName() {
        return Names.HANDLER_FLATTEN_MAPPING;
    }

    @Override
    public List<Route> routes() {
        return List.of(
            new Route(GET, Names.ROUTE_FLATTEN_ALL_MAPPINGS),
            new Route(GET, Names.ROUTE_FLATTEN_INDEX_MAPPING)
        );
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        String[] indices = Strings.splitStringByCommaToArray(request.param(Names.PARAM_INDEX, Names.ALL_INDICES));
        FlattenMode mode = FlattenMode.fromParam(request.param(Names.PARAM_FLATTEN));
        TimeValue masterTimeout = RestUtils.getMasterNodeTimeout(request);
        GetMappingsRequest req = new GetMappingsRequest(masterTimeout);
        req.indices(indices);
        return channel -> client.execute(GetMappingsAction.INSTANCE, req, new ActionListener<GetMappingsResponse>() {
            @Override
            public void onResponse(GetMappingsResponse response) {
                try {
                    Map<String, Object> output = new LinkedHashMap<>();
                    for (Map.Entry<String, MappingMetadata> entry : response.getMappings().entrySet()) {
                        Map<String, Object> source = new LinkedHashMap<>(entry.getValue().sourceAsMap());
                        Object props = source.get(Names.FIELD_PROPERTIES);
                        if (props instanceof Map<?, ?> propsMap) {
                            @SuppressWarnings("unchecked")
                            Map<String, Object> typedProps = (Map<String, Object>) propsMap;
                            source.put(Names.FIELD_PROPERTIES, MappingFlattener.flattenedProperties(typedProps, mode));
                        }
                        Map<String, Object> wrapper = new LinkedHashMap<>();
                        wrapper.put(Names.FIELD_MAPPINGS, source);
                        output.put(entry.getKey(), wrapper);
                    }
                    XContentBuilder builder = JsonXContent.contentBuilder().map(output);
                    channel.sendResponse(new RestResponse(RestStatus.OK, builder));
                } catch (IOException ex) {
                    onFailure(ex);
                }
            }

            @Override
            public void onFailure(Exception e) {
                try {
                    channel.sendResponse(new RestResponse(channel, e));
                } catch (IOException ignored) {
                }
            }
        });
    }
}
