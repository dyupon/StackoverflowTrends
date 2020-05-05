package com.csc;

public class TagClustering {
    private final String topic;
    private final double propensity;
    private final String tag;

    TagClustering(String topic, double propensity) {
        this.topic = topic;
        this.propensity = propensity;
        tag = null;
    }

    TagClustering(String topic, double propensity, String tag) {
        this.topic = topic;
        this.propensity = propensity;
        this.tag = tag;
    }

    TagClustering(String topic, String tag) {
        this.tag = tag;
        this.topic = topic;
        this.propensity = 0;
    }

    boolean isInformative() {
        return propensity > 0;
    }

    String getTopic() {
        return topic;
    }

    double getPropensity() {
        return propensity;
    }

}
