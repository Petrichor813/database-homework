package com.volunteer.backend.dto.response;

import java.util.List;

import com.volunteer.backend.dto.SankeyLink;
import com.volunteer.backend.dto.SankeyNode;

public class PointFlowSankeyResponse {
    private List<SankeyNode> nodes;
    private List<SankeyLink> links;

    public PointFlowSankeyResponse() {
    }

    // @formatter:off
    public PointFlowSankeyResponse(
        List<SankeyNode> nodes,
        List<SankeyLink> links
    ) {
        // @formatter:on
        this.nodes = nodes;
        this.links = links;
    }

    public List<SankeyNode> getNodes() {
        return nodes;
    }

    public void setNodes(List<SankeyNode> nodes) {
        this.nodes = nodes;
    }

    public List<SankeyLink> getLinks() {
        return links;
    }

    public void setLinks(List<SankeyLink> links) {
        this.links = links;
    }
}
