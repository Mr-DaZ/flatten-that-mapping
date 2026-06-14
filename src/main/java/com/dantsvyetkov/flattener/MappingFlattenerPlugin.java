package com.dantsvyetkov.flattener;

import org.elasticsearch.cluster.node.DiscoveryNodes;
import org.elasticsearch.features.NodeFeature;
import org.elasticsearch.plugins.ActionPlugin;
import org.elasticsearch.plugins.Plugin;
import org.elasticsearch.rest.RestHandler;

import java.util.Collection;
import java.util.List;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class MappingFlattenerPlugin extends Plugin implements ActionPlugin {

    @Override
    public Collection<RestHandler> getRestHandlers(
            ActionPlugin.RestHandlersServices services,
            Supplier<DiscoveryNodes> nodesInCluster,
            Predicate<NodeFeature> nodeFeatures) {
        return List.of(new RestFlattenMappingAction(), new RestFlattenIndexAction());
    }
}
