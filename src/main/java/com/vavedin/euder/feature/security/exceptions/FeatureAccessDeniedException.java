package com.vavedin.euder.feature.security.exceptions;


import com.vavedin.euder.common.exception.ForbiddenException;
import com.vavedin.euder.feature.security.enums.Feature;

public class FeatureAccessDeniedException extends ForbiddenException {
    public FeatureAccessDeniedException(String email, Feature feature) {
        super("User " + email + " has no access to " + feature);
    }
}
