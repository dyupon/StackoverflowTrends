package com.csc;

public class TagClustering {
    private final String topic;
    private final double propensity;

    TagClustering(String topic, double propensity) {
        this.topic = topic;
        this.propensity = propensity;
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
