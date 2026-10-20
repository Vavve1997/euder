package com.vavedin.euder.feature.security.enums;

import java.util.List;

import static com.vavedin.euder.feature.security.enums.Feature.*;


public enum Role {
    ADMIN(List.of(ADD_ITEM, VIEW_USERS, UPDATE_ITEM)),
    MEMBER(List.of(ORDER_ITEM, VIEW_OWN_EURDERS));

    private final List<Feature> featureList;

    Role(List<Feature> featureList) {
        this.featureList = featureList;
    }

    public boolean containsFeature(Feature feature) {
        return featureList.contains(feature);
    }
}
