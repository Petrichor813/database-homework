package com.volunteer.backend.dto;

public class SankeyLink {
    private String source;
    private String target;
    private Double value;

    public SankeyLink() {
    }

    public SankeyLink(String source, String target, Double value) {
        this.source = source;
        this.target = target;
        this.value = value;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getTarget() {
        return target;
    }

    public void setTarget(String target) {
        this.target = target;
    }

    public Double getValue() {
        return value;
    }

    public void setValue(Double value) {
        this.value = value;
    }
}
