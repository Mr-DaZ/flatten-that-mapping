package com.dantsvyetkov.flattener;

import org.elasticsearch.action.ActionListener;
import org.elasticsearch.action.admin.indices.get.GetIndexAction;
import org.elasticsearch.action.admin.indices.get.GetIndexRequest;
import org.elasticsearch.action.admin.indices.get.GetIndexResponse;
import org.elasticsearch.client.internal.node.NodeClient;
import org.elasticsearch.common.Strings;
import org.elasticsearch.common.bytes.BytesReference;
import org.elasticsearch.common.xcontent.ChunkedToXContent;
import org.elasticsearch.core.TimeValue;
import org.elasticsearch.rest.BaseRestHandler;
import org.elasticsearch.rest.RestRequest;
import org.elasticsearch.rest.RestResponse;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.rest.RestUtils;
import org.elasticsearch.xcontent.ToXContent;
import org.elasticsearch.xcontent.XContentBuilder;
import org.elasticsearch.xcontent.XContentParser;
import org.elasticsearch.xcontent.XContentParserConfiguration;
import org.elasticsearch.xcontent.XContentType;
import org.elasticsearch.xcontent.json.JsonXContent;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.elasticsearch.rest.RestRequest.Method.GET;

public class RestFlattenIndexAction extends BaseRestHandler {

    @Override
    public String getName() {
        return Names.HANDLER_FLATTEN_INDEX;
    }

    @Override
    public List<Route> routes() {
        return List.of(new Route(GET, Names.ROUTE_FLATTEN_INDEX));
    }

    @Override
    protected RestChannelConsumer prepareRequest(RestRequest request, NodeClient client) throws IOException {
        String[] indices = Strings.splitStringByCommaToArray(request.param(Names.PARAM_INDEX));
        FlattenMode mode = FlattenMode.fromParam(request.param(Names.PARAM_FLATTEN));
        TimeValue masterTimeout = RestUtils.getMasterNodeTimeout(request);
        GetIndexRequest req = new GetIndexRequest(masterTimeout);
        req.indices(indices);
        return channel -> client.execute(GetIndexAction.INSTANCE, req, new ActionListener<GetIndexResponse>() {
            @Override
            public void onResponse(GetIndexResponse response) {
                try {
                    XContentBuilder serialized = JsonXContent.contentBuilder();
                    ChunkedToXContent.wrapAsToXContent(response).toXContent(serialized, ToXContent.EMPTY_PARAMS);
                    BytesReference bytes = BytesReference.bytes(serialized);
                    Map<String, Object> json;
                    try (XContentParser parser = XContentType.JSON.xContent().createParser(
                            XContentParserConfiguration.EMPTY, bytes.streamInput())) {
                        json = parser.map();
                    }
                    MappingFlattener.flattenInPlace(json, mode);
                    XContentBuilder out = JsonXContent.contentBuilder().map(json);
                    channel.sendResponse(new RestResponse(RestStatus.OK, out));
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
