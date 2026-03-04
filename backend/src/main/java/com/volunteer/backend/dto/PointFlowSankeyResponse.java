package com.volunteer.backend.dto;

import java.util.List;

public class PointFlowSankeyResponse {
    private List<SankeyNode> nodes;
    private List<SankeyLink> links;

    public PointFlowSankeyResponse() {
    }

    public PointFlowSankeyResponse(List<SankeyNode> nodes, List<SankeyLink> links) {
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
