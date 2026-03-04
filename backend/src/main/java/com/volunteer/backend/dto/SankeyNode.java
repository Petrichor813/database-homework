package com.volunteer.backend.dto;

public class SankeyNode {
    private String name;

    public SankeyNode() {
    }

    public SankeyNode(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
